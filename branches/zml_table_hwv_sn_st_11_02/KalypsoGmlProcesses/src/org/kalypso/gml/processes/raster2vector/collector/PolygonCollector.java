package org.kalypso.gml.processes.raster2vector.collector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypso.gml.processes.raster2vector.LinkedCoordinate;
import org.kalypso.gml.processes.raster2vector.LinkedCoordinateException;
import org.kalypso.gml.processes.raster2vector.ringtree.RingTree;
import org.kalypso.gml.processes.raster2vector.ringtree.RingTreeElement;
import org.kalypso.gml.processes.raster2vector.ringtree.RingTreeWalker;
import org.kalypso.jts.CoordOrientation;
import org.kalypso.jts.CoordOrientationException;
import org.kalypso.jts.JTSUtilities;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.SimplePointInRing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Sammelt LineStrings und kombiniert Sie als Polygone
 * 
 * @author belger
 */
public class PolygonCollector implements SegmentCollector, RingTreeWalker
{
  private final List<CollectorDataProvider> m_dataList = new ArrayList<CollectorDataProvider>();

  private final GeometryFactory m_gf;

  private final Interval[] m_intervals;

  private final boolean m_bSimple;

  private final RingTree m_tree = new RingTree();

  private final double[] m_grenzen;

  private int m_internalId = 0;

  public PolygonCollector( final GeometryFactory gf, final double[] grenzen, final boolean bSimple )
  {
    m_gf = gf;
    m_grenzen = grenzen;
    m_bSimple = bSimple;

    m_intervals = new Interval[grenzen.length + 1];
    m_intervals[0] = new Interval( Double.MIN_VALUE, grenzen[0] );
    for( int i = 0; i < grenzen.length - 1; i++ )
      m_intervals[i + 1] = new Interval( grenzen[i], grenzen[i + 1] );
    m_intervals[grenzen.length] = new Interval( grenzen[grenzen.length - 1], Double.MAX_VALUE );

  }

  /**
   * @see org.kalypso.gml.processes.raster2vector.collector.SegmentCollector#addSegment(int,
   *      org.kalypso.gml.processes.raster2vector.LinkedCoordinate,
   *      org.kalypso.gml.processes.raster2vector.LinkedCoordinate)
   */
  @Override
  public void addSegment( final int index, final LinkedCoordinate lc0, final LinkedCoordinate lc1 ) throws LinkedCoordinateException
  {
    lc0.link( lc1 );

    if( !lc0.isCircle() )
      return;

    try
    {
      final Coordinate[] crds = lc0.getAsRing();

      if( m_bSimple )
      {
        final LinearRing lr = m_gf.createLinearRing( crds );
        final Polygon poly = m_gf.createPolygon( lr, new LinearRing[] {} );
        appendFeature( m_intervals[index], poly, Integer.toString( m_internalId++ ) );
      }
      else
      {
        final PointInRing pir = new SimplePointInRing( m_gf.createLinearRing( crds ) );

        final Coordinate innerCrd = lc0.findInnerCrd( pir );
//
// if( pir.isInside( nearC0 ) )
// innerCrd = nearC0;
// else if( pir.isInside( nearC1 ) )
// innerCrd = nearC1;
// else

        if( innerCrd == null )
        {
          System.out.println( Messages.getString( "org.kalypso.gml.processes.raster2vector.collector.PolygonCollector.0" ) ); //$NON-NLS-1$
          return;
        }

        CoordOrientation.orient( crds, CoordOrientation.TYPE.NEGATIV );

        // REMARK: We remove z because a) it has no meaning for the polygons (they represent a range) and b) we get
        // strange
        // problems (ring not closed, as first and last coordinates has different z's)
        final Coordinate[] crdsNoZ = new Coordinate[crds.length];
        for( int i = 0; i < crdsNoZ.length; i++ )
          crdsNoZ[i] = new Coordinate( crds[i].x, crds[i].y );

        final LinearRing lr = m_gf.createLinearRing( crdsNoZ );
        final Polygon polygon = m_gf.createPolygon( lr, null );

// if( innerCrd.z < m_intervals[index + 1].m_min || innerCrd.z > m_intervals[index + 1].m_max )
// System.out.println( "Achtung: Klasse passt nicht zu Wert!" );

        // Here we handle only well-formed polygons
        if( polygon.getArea() > 0 )
          m_tree.insertElement( new RingTreeElement( lr, polygon, m_grenzen[index], innerCrd, Integer.toString( m_internalId++ ) ) );
      }
    }
    catch( final CoordOrientationException coe )
    {
      coe.printStackTrace();
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }
  }

  /**
   * @param index
   * @param poly
   * @throws GM_Exception
   */
  private void appendFeature( final Interval interval, Polygon poly, final String internalId ) throws GM_Exception
  {
    // first and last intervals will not added
    if( ArrayUtils.indexOf( m_intervals, interval ) == 0 || ArrayUtils.indexOf( m_intervals, interval ) == m_intervals.length - 1 )
      return;

    final double area = poly.getArea();
    if( area < 0.1 )
      return;

    poly = JTSUtilities.cleanPolygonInteriorRings( poly );

    final GM_Object gmGeo = JTSAdapter.wrap( poly );

    final Double id = new Double( m_dataList.size() );
    final String name = interval.toString();

    final Double von = new Double( Math.max( -9999.99, interval.getMin() ) );
    final Double bis = new Double( Math.min( 9999.99, interval.getMax() ) );
    final Double volumen = Double.NaN;

    final CollectorDataProvider dataProvider = new CollectorDataProvider( gmGeo, new Double[] { von, bis, volumen }, id, new String[] { name, internalId } );
    m_dataList.add( dataProvider );

  }

  /**
   * @see com.bce.gis.operation.raster2vector.collector.ringtree.RingTreeWalker#operate(com.bce.gis.operation.raster2vector.collector.ringtree.RingTreeElement)
   */
  @Override
  public void operate( final RingTreeElement element )
  {
    final Polygon[] p = element.getAsPolygon();
    final String internalId = element.getId();

    if( p != null )
    {
      try
      {
        final Interval interval = element.getPolygonIntervall();
        if( interval == null )
          return;

        for( final Polygon polygon : p )
          appendFeature( interval, polygon, internalId );
      }
      catch( final GM_Exception gme )
      {
        gme.printStackTrace();
      }
    }
  }

// private int getIndexForElement( final RingTreeElement element )
// {
// final int index = element.index;
// if( element.hasChildren() )
// {
// final RingTreeElement child = element.getFirstChild();
//
// // check if the first child has a neighboring index
// if( child.index == index - 1 || child.index == index + 1 )
// {
// final double grenze0 = m_grenzen[index];
// final double grenze1 = m_grenzen[child.index];
//
// for( int i = 0; i < m_intervals.length; i++ )
// {
// if( (m_intervals[i].getMin() == grenze0 && m_intervals[i].getMax() == grenze1) || (m_intervals[i].getMin() == grenze1
// && m_intervals[i].getMax() == grenze0) )
// return i;
// }
// }
// else
// {
// final int childIndex = getIndexForElement( child );
// if( childIndex == -1 )
// return index + 1;
//
// if( m_grenzen[index] == m_intervals[childIndex].getMax() )
// return index + 1;
//
// // return index;
// return index + 1;
// }
// }
// else
// {
// // anhand der inneren Coordinate rausfinden, zu welcher Klasse es gehört
// // geht das überhaupt?? ja, wenn es keine Kinder hat
// final double value = element.innerCrd.z;
// if( Double.isNaN( value ) )
// return -1;
//
// for( int i = 0; i < m_intervals.length; i++ )
// {
// if( m_intervals[i].contains( value ) )
// return i;
// }
// }
//
// return -1;
// }

  public static class Interval
  {
    private final double m_min;

    private final double m_max;

    public double getMin( )
    {
      return m_min;
    }

    public double getMax( )
    {
      return m_max;
    }

    public Interval( final double min, final double max )
    {
      m_min = min;
      m_max = max;
    }

    public boolean contains( final double value )
    {
      // TODO: check this is most certainly a bug and should be Double.MAX_VALUE
      if( m_min == Double.MIN_VALUE )
        return value < m_max;
      else if( m_max == Double.MAX_VALUE )
        return m_min <= value;
      else
        return m_min <= value && value < m_max;
    }

    @Override
    public String toString( )
    {
      final StringBuffer str = new StringBuffer();
      if( m_min == Double.MIN_VALUE )
        str.append( "-Inf" ); //$NON-NLS-1$
      else
        str.append( m_min );

      str.append( " - " ); //$NON-NLS-1$

      if( m_max == Double.MAX_VALUE )
        str.append( "Inf" ); //$NON-NLS-1$
      else
        str.append( m_max );

      return str.toString();
    }
  }

  /**
   * @see org.kalypso.grid.processes.raster2vector.SegmentCollector#getData()
   */
  @Override
  public CollectorDataProvider[] getData( )
  {
    if( !m_bSimple )
      m_tree.walk( this );

    return m_dataList.toArray( new CollectorDataProvider[m_dataList.size()] );
  }

  /**
   * @see org.kalypso.grid.processes.raster2vector.collector.ringtree.RingTreeWalker#getResult()
   */
  @Override
  public Object getResult( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.gml.processes.raster2vector.collector.SegmentCollector#finish()
   */
  @Override
  public void finish( )
  {
    m_tree.root.initIntervals( m_intervals );

  }

  /**
   * @see org.kalypso.gml.processes.raster2vector.collector.SegmentCollector#interpolate(com.vividsolutions.jts.geom.Coordinate,
   *      com.vividsolutions.jts.geom.Coordinate, int)
   */
  @Override
  public LinkedCoordinate interpolate( final Coordinate c1, final Coordinate c2, final int index )
  {
    double zFaktor = -1.0;
    final double z1 = c1.z;
    final double z2 = c2.z;

    final double value = m_grenzen[index];

// HACK
    if( Double.isNaN( c1.z ) && Double.isNaN( c2.z ) )
      return null;

    if( (Double.isNaN( c1.z ) && value < c2.z) || (Double.isNaN( c2.z ) && value < c1.z) )
    {
      zFaktor = 0.5;
    }
    else
    {
      if( (z1 <= value && value < z2) || (z2 <= value && value < z1) )
        zFaktor = (value - z1) / (z2 - z1);
      if( zFaktor < 0 || zFaktor >= 1 )
        return null;
    }

    final double x = zFaktor * (c2.x - c1.x) + c1.x;
    final double y = zFaktor * (c2.y - c1.y) + c1.y;
    return new LinkedCoordinate( new Coordinate( x, y, value ), false );
  }
}
