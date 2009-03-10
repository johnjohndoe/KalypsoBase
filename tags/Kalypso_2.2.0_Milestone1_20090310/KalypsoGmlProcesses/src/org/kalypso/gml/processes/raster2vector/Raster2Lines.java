package org.kalypso.gml.processes.raster2vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.kalypso.gml.processes.i18n.Messages;
import org.kalypso.gml.processes.raster2vector.collector.SegmentCollector;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.grid.IGeoGridWalker;

import com.vividsolutions.jts.geom.Coordinate;

public class Raster2Lines implements IGeoGridWalker
{
  private double m_volume;

  private static final double VAL_EPS = 0.00001;

  private Coordinate[] m_lastRowCoord = null;

  private Coordinate m_lastSE;

  private Coordinate m_lastNE;

  private LinkedCoordinate[][] m_lastRow;

  private LinkedCoordinate[] m_lastCell;

  private final double[] m_grenzen;

  private final SegmentCollector m_strategy;

  private double m_cellArea;

  private Coordinate m_offsetX;

  private Coordinate m_offsetY;

  private int m_sizeX;

  private IGeoGrid m_grid;

  public Raster2Lines( final SegmentCollector strategy, final double[] isolines )
  {
    m_strategy = strategy;
    m_grenzen = isolines;
  }

  /**
   * @see org.kalypso.gis.doubleraster.DoubleRasterWalker#start(org.kalypso.gis.doubleraster.DoubleRaster)
   */
  public final void start( final IGeoGrid grid ) throws GeoGridException
  {
    m_grid = grid;
    m_lastRowCoord = new Coordinate[grid.getSizeX() + 1];
    m_lastSE = null;
    m_offsetX = grid.getOffsetX();
    m_offsetY = grid.getOffsetY();
    m_lastRow = new LinkedCoordinate[m_grenzen.length][grid.getSizeX() + 1];
    m_lastCell = new LinkedCoordinate[m_grenzen.length];

    m_cellArea = GeoGridUtilities.calcCellArea( m_offsetX, m_offsetY );

    // reset volume
    m_volume = 0.0;

    m_sizeX = grid.getSizeX();
  }

  /**
   * @see org.kalypso.gis.doubleraster.DoubleRasterWalker#operate(int, int, com.vividsolutions.jts.geom.Coordinate)
   */
  public final void operate( final int x, final int y, final Coordinate crd ) throws GeoGridException
  {
    // Volumen aufaddieren
    final double cellVolume = m_cellArea * crd.z;
    if( crd.z > 0 )
      m_volume += cellVolume;

    // die Zellecken ausrechnen
    final Coordinate cSE = new Coordinate( crd );

    Coordinate cSW = m_lastSE;
    Coordinate cNE = m_lastRowCoord[x];
    Coordinate cNW = m_lastNE;

    if( cSW == null || x == 0 )
    {
      // cSW = new Coordinate( crd.x - m_rasterSize, crd.y, Double.NaN );
      cSW = GeoGridUtilities.calcCoordinate( m_grid, x - 1, y, null );
    }
    if( cNE == null )
    {
      // cNE = new Coordinate( crd.x, crd.y + m_rasterSize, Double.NaN );
      cNE = GeoGridUtilities.calcCoordinate( m_grid, x, y - 1, null );
    }
    if( cNW == null || x == 0 )
    {
      // cNW = new Coordinate( crd.x - m_rasterSize, crd.y + m_rasterSize, Double.NaN );
      cNW = GeoGridUtilities.calcCoordinate( m_grid, x - 1, y - 1, null );
    }

    m_lastSE = cSE;
    m_lastNE = cNE;
    m_lastRowCoord[x] = cSE;

    addSegment( x, y, cNW, cNE, cSW, cSE );

    // TODO: this implementation makes assumptions about the order of visited cells... dangerous!
    if( x == m_sizeX - 1 )
      afterLine( x, y );
  }

  /**
   * @see org.kalypso.gis.doubleraster.DoubleRasterWalker#afterLine(int)
   */
  private final void afterLine( final int x, final int y ) throws GeoGridException
  {
    // die Zellecken ausrechnen

    final Coordinate cSW = m_lastSE;
    final Coordinate cNW = m_lastNE;
    Coordinate cNE = m_lastRowCoord[x + 1];

    if( cNE == null )
      cNE = GeoGridUtilities.calcCoordinate( m_grid, x + 1, y - 1, null );

    final Coordinate cSE = GeoGridUtilities.calcCoordinate( m_grid, x + 1, y, null );

    m_lastSE = null;
    m_lastNE = null;
    m_lastRowCoord[x + 1] = cSE;

    addSegment( x + 1, y, cNW, cNE, cSW, cSE );
  }

  /**
   * Gibt ein Objekt vom Type LineString[] zurück
   * 
   * @see org.kalypso.gis.doubleraster.DoubleRasterWalker#getResult()
   */
  public Object finish( )
  {
    m_strategy.finish();

    return m_strategy;
  }

  private final void addSegment( final int x, final int y, final Coordinate cNW, final Coordinate cNE, final Coordinate cSW, final Coordinate cSE ) throws GeoGridException
  {
    final LinkedCoordinate[] tops = new LinkedCoordinate[m_grenzen.length];
    final LinkedCoordinate[] lefts = new LinkedCoordinate[m_grenzen.length];
    final LinkedCoordinate[] rights = new LinkedCoordinate[m_grenzen.length];
    final LinkedCoordinate[] bottoms = new LinkedCoordinate[m_grenzen.length];

    for( int i = 0; i < m_grenzen.length; i++ )
    {
      // check, ob die Koordinaten den Wert genau Treffen
      final double value = m_grenzen[i];
      if( Math.abs( cNW.z - value ) < VAL_EPS )
        cNW.z += VAL_EPS;
      if( Math.abs( cSW.z - value ) < VAL_EPS )
        cSW.z += VAL_EPS;
      if( Math.abs( cNE.z - value ) < VAL_EPS )
        cNE.z += VAL_EPS;
      if( Math.abs( cSE.z - value ) < VAL_EPS )
        cSE.z += VAL_EPS;

      // gibt es einen Schnitt mit der Isolinie?
      tops[i] = x >= 0 ? m_lastRow[i][x] : null;
      lefts[i] = x > 0 ? m_lastCell[i] : null;
      rights[i] = interpolate( cNE, cSE, i );
      bottoms[i] = interpolate( cSW, cSE, i );
    }

    // TODO: inner erzeugen

    // rights + bottoms
    generateInnerCrds( rights, cNE, cSE );
    generateInnerCrds( bottoms, cSW, cSE );

    for( int i = 0; i < m_grenzen.length; i++ )
    {
      try
      {
        addAtIndex( i, x, y, tops[i], lefts[i], rights[i], bottoms[i] );
      }
      catch( final LinkedCoordinateException lce )
      {
        throw new GeoGridException( Messages.getString( "org.kalypso.gml.processes.raster2vector.Raster2Lines.0" ), lce ); //$NON-NLS-1$
      }
    }
  }

  private void generateInnerCrds( final LinkedCoordinate[] linkedCrs, final Coordinate startCrd, final Coordinate endCrd )
  {
    /* Make sure, that we begin with the crd without NaN value (the case where both are NaN is irrelevant) */
    final Coordinate crdBegin = Double.isNaN( startCrd.z ) ? endCrd : startCrd;
    final Coordinate crdEnd = Double.isNaN( startCrd.z ) ? startCrd : endCrd;

    // Sort coordinates by distance to startCrd
    // REMARK: secondary we sort by the z-value: this is needed for the case where start or end have NaN value.
    // In this case all boundaries below (!) the other value get a linkedCrd here; so we must create the inner
    // coordinate for the biggest one.
    final Comparator< ? super Coordinate> comp = new Comparator<Coordinate>()
    {
      @Override
      public int compare( final Coordinate o1, final Coordinate o2 )
      {
        final double distance1 = distanceTo( o1 );
        final double distance2 = distanceTo( o2 );
        final int compare = Double.compare( distance1, distance2 );

        if( compare == 0 )
        {
          final double zDist1 = Math.abs( o1.z - crdBegin.z );
          final double zDist2 = Math.abs( o2.z - crdBegin.z );
          return Double.compare( zDist1, zDist2 );
        }

        return compare;
      }

      /**
       * Special distance to: didstance to first coordinate.<br>
       * Especially makes sure, that start and end coordinate are sorted to start and end.
       */
      private double distanceTo( final Coordinate o1 )
      {
        if( o1 == crdBegin )
          return -1;

        if( o1 == crdEnd )
          return o1.distance( crdBegin ) + 1;

        return o1.distance( crdBegin );
      }
    };

    final Map<Coordinate, LinkedCoordinate> lcs = new IdentityHashMap<Coordinate, LinkedCoordinate>();

    /*
     * Sort coordinate by distance to first coordinate; we do not use a SorteMap here, as in this case, coordinates on
     * the same place get lost
     */
    final List<Coordinate> coordinates = new ArrayList<Coordinate>();
    coordinates.add( crdBegin );
    for( int i = 0; i < linkedCrs.length; i++ )
    {
      final LinkedCoordinate value = linkedCrs[i];
      if( value != null )
      {
        final Coordinate crd = value.crd;
        coordinates.add( crd );
        lcs.put( crd, value );
      }
    }
    coordinates.add( crdEnd );

    Collections.sort( coordinates, comp );

    /* Find inner coordinates for every coordinate */
    Coordinate prevprev = null;
    Coordinate prev = null;
    for( final Coordinate crd : coordinates )
    {
      if( prev != null && prevprev != null )
      {
        final LinkedCoordinate currentLC = lcs.get( prev );

        if( currentLC == null )
          System.out.println();
        else
        {
          final Coordinate innerLeft = interpolateForInnerCrd( prevprev, prev );
          final Coordinate innerRight = interpolateForInnerCrd( prev, crd );
          if( innerLeft != null )
          {
            if( (innerLeft.x == 85937.5 && innerLeft.y == 407071.25) )
            {
              System.out.println();
            }
          }
          currentLC.setInnerCrds( new Coordinate[] { innerLeft, innerRight } );
        }
      }

      prevprev = prev;
      prev = crd;
    }

  }

  private Coordinate interpolateForInnerCrd( final Coordinate c1, final Coordinate c2 )
  {
    if( c1.distance( c2 ) < 0.1 )
      return null;

    final double x = (c1.x + c2.x) / 2.0;
    final double y = (c1.y + c2.y) / 2.0;
    final double z = (c1.z + c2.z) / 2.0;
    return new Coordinate( x, y, z );
  }

  private final void addAtIndex( final int index, final int x, final int y, final LinkedCoordinate topC, final LinkedCoordinate leftC, final LinkedCoordinate rightC, final LinkedCoordinate bottomC ) throws LinkedCoordinateException, GeoGridException
  {
    m_lastCell[index] = rightC;
    m_lastRow[index][x] = bottomC;

    // Schnittlinien in dieser Zelle ermitteln
    // es gibt 16 Fälle
    if( topC == null && leftC == null && rightC == null && bottomC == null )
    {
      // 0: nix tun
    }
    else if( topC != null && leftC != null && rightC == null && bottomC == null )
    {
      // 2:
      m_strategy.addSegment( index, leftC, topC );
    }
    else if( topC != null && leftC == null && rightC != null && bottomC == null )
    {
      // 3:
      m_strategy.addSegment( index, topC, rightC );
    }
    else if( topC != null && leftC == null && rightC == null && bottomC != null )
    {
      // 4:
      m_strategy.addSegment( index, topC, bottomC );
    }
    else if( topC == null && leftC != null && rightC == null && bottomC != null )
    {
      // 9:
      m_strategy.addSegment( index, leftC, bottomC );
    }
    else if( topC == null && leftC != null && rightC != null && bottomC == null )
    {
      // 10:
      m_strategy.addSegment( index, leftC, rightC );
    }
    else if( topC == null && leftC == null && rightC != null && bottomC != null )
    {
      // 13:
      m_strategy.addSegment( index, rightC, bottomC );
    }
    else if( topC != null && leftC != null && rightC != null && bottomC != null )
    {
      // 15:
      if( topC.crd.x < bottomC.crd.x )
      {
        // erst wie 2
        m_strategy.addSegment( index, leftC, topC );

        // dann wie 13
        m_strategy.addSegment( index, rightC, bottomC );
      }
      else
      {
        // erst 3
        m_strategy.addSegment( index, topC, rightC );

        // dann 9
        m_strategy.addSegment( index, leftC, bottomC );
      }
    }
    else
    {
      final String msg = String.format( Messages.getString( "org.kalypso.gml.processes.raster2vector.Raster2Lines.1" ), x, y, index ); //$NON-NLS-1$
      throw new GeoGridException( msg, null );
    }
  }

  /**
   * Hat die Verbindung c1 - c2 einen Zwischenwert bei value, interpoliere die Zwischencoordinate Voraussetzung ist,
   * dass keine der Koordinaten exakt den Wert annimmt!
   */
  private final LinkedCoordinate interpolate( final Coordinate c1, final Coordinate c2, final int index )
  {
    if( m_strategy == null )
      return null;

    return m_strategy.interpolate( c1, c2, index );
  }

  public double getSum( )
  {
    return m_volume;
  }
}
