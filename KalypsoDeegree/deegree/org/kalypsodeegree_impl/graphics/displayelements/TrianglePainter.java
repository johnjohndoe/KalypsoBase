/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypsodeegree_impl.graphics.displayelements;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.graphics.sld.awt.FillPainter;
import org.kalypsodeegree_impl.graphics.sld.awt.SldAwtUtilities;
import org.kalypsodeegree_impl.graphics.sld.awt.StrokePainter;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * Helper to split up triangles by color classes
 * 
 * @author Gernot Belger
 */
final class TrianglePainter
{
  private static final double VAL_EPS = 0.0000001;

  private final Graphics2D m_gc;

  private enum RELATION
  {
    INSIDE,
    OUTSIDE,
    BETWEEN;
  }

  public TrianglePainter( final Graphics2D gc )
  {
    m_gc = gc;
  }

  public void paint( final GM_Position[] positions, final IElevationColorModel colorModel )
  {
    final ElevationColorEntry[] entries = colorModel.getColorEntries();

    /* loop over all classes */
    for( final ElevationColorEntry entry : entries )
      paint( positions, entry );

    // DEBUG:
// try
// {
// final Stroke black = StyleFactory.createStroke( new Color( 0, 0, 0 ), 2 );
// final StrokePainter strokePainter = new StrokePainter( black, null, null, null );
//
// final FillPainter fillPainter = m_colorModel.getFillPolygonPainter( 0 );
// final GeoTransform world2Screen = fillPainter.getWorld2Screen();
// m_painter.paintTriangle( positions, strokePainter, fillPainter, world2Screen );
// }
// catch( final FilterEvaluationException e )
// {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }

  }

//  /**
//   * For debug pruposes: paint the whole triangle
//   */
//  private void paintTriangle( final GM_Position[] positions, final StrokePainter strokePainter, final FillPainter fillPainter, final GeoTransform world2Screen )
//  {
//    try
//    {
//      SldAwtUtilities.paintRing( m_gc, positions, world2Screen, fillPainter, strokePainter );
//    }
//    catch( final Exception ex )
//    {
//      ex.printStackTrace();
//    }
//  }

  private void paint( final GM_Position[] positions, final ElevationColorEntry entry )
  {
    final double startValue = entry.getFrom();
    final double endValue = entry.getTo();

    final StrokePainter strokePainter = entry.getLinePainter();
    final FillPainter fillPainter = entry.getPolygonPainter();

    final GeoTransform world2Screen = fillPainter.getWorld2Screen();

    final GM_Position[] figure = calculateFigure( positions, startValue, endValue );
    if( figure == null )
      return;

// final String join = StringUtils.join( figure, " - " );
// System.out.println( join );

    // TODO: check if current surface is too small (i.e. < stroke.width) and if this is the case
    // only draw a point with that color or something similar

    // TODO: different strategy? -> paint pixel by pixel -> detect triangle value and paint with right class

    paintSurface( figure, strokePainter, fillPainter, world2Screen );
  }

  private void paintSurface( final GM_Position[] figure, final StrokePainter strokePainter, final FillPainter fillPainter, final GeoTransform world2Screen )
  {
    try
    {
      final Polygon shape = SldAwtUtilities.polygonFromRing( figure, strokePainter.getWidth(), world2Screen );
      if( shape != null )
        SldAwtUtilities.paintShape( m_gc, shape, fillPainter, strokePainter );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private GM_Position[] calculateFigure( final GM_Position[] positions, final double startValue, final double endValue )
  {
    final RELATION r = relateTriangle( positions, startValue, endValue );
    switch( r )
    {
      case OUTSIDE:
        return null;

      case INSIDE:
        return positions;

      case BETWEEN:
        return calculateSplitFigure( positions, startValue, endValue );
    }

    throw new UnsupportedOperationException();
  }

  private GM_Position[] calculateSplitFigure( final GM_Position[] positions, final double startValue, final double endValue )
  {
    /* get the intersection points */
    final List<GM_Position> posList = new ArrayList<>( positions.length * 2 );

    /* loop over all arcs */
    for( int j = 0; j < positions.length - 1; j++ )
    {
      final GM_Position pos1 = positions[j];
      final GM_Position pos2 = positions[j + 1];

      final double x1 = pos1.getX();
      final double y1 = pos1.getY();
      final double z1 = pos1.getZ();

      final double x2 = pos2.getX();
      final double y2 = pos2.getY();
      final double z2 = pos2.getZ();

      /* ====================== Fallunterscheidungen ========================= */

      /*
       * Knoten 1 liegt innerhalb, Knoten 2 liegt innerhalb Knoten 1.z = Knoten 2.z des aktuell betrachteten Intervalls
       */
      if( z1 == z2 && z1 >= startValue && z1 <= endValue )
      {
        posList.add( GeometryFactory.createGM_Position( x1, y1, z1 ) );

        posList.add( GeometryFactory.createGM_Position( x2, y2, z2 ) );
      }

      /*
       * Knoten 1 liegt innnerhalb, Knoten 2 liegt oberhalb Knoten 1.z < Knoten 2.z des aktuell betrachteten Intervalls
       */
      if( startValue <= z1 && z1 <= endValue && endValue <= z2 )
      {
        posList.add( GeometryFactory.createGM_Position( x1, y1, z1 ) );

        final double x = x1 + (x2 - x1) * (endValue - z1) / (z2 - z1);
        final double y = y1 + (y2 - y1) * (endValue - z1) / (z2 - z1);
        final double z = endValue;
        posList.add( GeometryFactory.createGM_Position( x, y, z ) );
      }

      /*
       * Knoten 1 liegt unterhalb, Knoten 2 liegt oberhalb Knoten 1.z < Knoten 2.z des aktuell betrachteten Intervalls
       */
      if( z1 <= startValue && startValue < z2 && z1 <= endValue && endValue <= z2 )
      {
        final double xA = x1 + (x2 - x1) * (startValue - z1) / (z2 - z1);
        final double yA = y1 + (y2 - y1) * (startValue - z1) / (z2 - z1);
        final double zA = startValue;
        posList.add( GeometryFactory.createGM_Position( xA, yA, zA ) );

        final double xB = x1 + (x2 - x1) * (endValue - z1) / (z2 - z1);
        final double yB = y1 + (y2 - y1) * (endValue - z1) / (z2 - z1);
        final double zB = endValue;
        posList.add( GeometryFactory.createGM_Position( xB, yB, zB ) );
      }

      /*
       * Knoten 1 liegt unterhalb, Knoten 2 liegt innerhalb Knoten 1.z < Knoten 2.z des aktuell betrachteten Intervalls
       */
      if( z1 <= startValue && startValue <= z2 && z2 <= endValue )
      {
        final double x = x1 + (x2 - x1) * (startValue - z1) / (z2 - z1);
        final double y = y1 + (y2 - y1) * (startValue - z1) / (z2 - z1);
        final double z = startValue;
        posList.add( GeometryFactory.createGM_Position( x, y, z ) );

        posList.add( GeometryFactory.createGM_Position( x2, y2, z2 ) );
      }

      /*
       * Knoten 1 liegt innerhalb, Knoten 2 liegt innerhalb Knoten 1.z < Knoten 2.z des aktuell betrachteten Intervalls
       */
      if( startValue <= z1 && z2 <= endValue && z1 < z2 )
      {
        posList.add( GeometryFactory.createGM_Position( x1, y1, z1 ) );

        posList.add( GeometryFactory.createGM_Position( x2, y2, z2 ) );
      }

      /*
       * Knoten 1 liegt oberhalb, Knoten 2 liegt innerhalb Knoten 1.z < Knoten 2.z des aktuell betrachteten Intervalls
       */
      if( startValue <= z2 && z2 <= endValue && endValue <= z1 )
      {
        final double x = x1 + (x2 - x1) * (endValue - z1) / (z2 - z1);
        final double y = y1 + (y2 - y1) * (endValue - z1) / (z2 - z1);
        final double z = endValue;
        posList.add( GeometryFactory.createGM_Position( x, y, z ) );

        posList.add( GeometryFactory.createGM_Position( x2, y2, z2 ) );
      }

      /*
       * Knoten 1 liegt innerhalb, Knoten 2 liegt unterhalb Knoten 1.z > Knoten 2.z des aktuell betrachteten Intervalls
       */
      if( z2 <= startValue && startValue <= z1 && z2 <= endValue && endValue <= z1 )
      {
        final double xA = x1 + (x2 - x1) * (endValue - z1) / (z2 - z1);
        final double yA = y1 + (y2 - y1) * (endValue - z1) / (z2 - z1);
        final double zA = endValue;
        posList.add( GeometryFactory.createGM_Position( xA, yA, zA ) );

        final double xB = x1 + (x2 - x1) * (startValue - z1) / (z2 - z1);
        final double yB = y1 + (y2 - y1) * (startValue - z1) / (z2 - z1);
        final double zB = startValue;
        posList.add( GeometryFactory.createGM_Position( xB, yB, zB ) );
      }

      /*
       * Knoten 1 liegt innerhalb, Knoten 2 liegt unterhalb Knoten 1.z > Knoten 2.z des aktuell betrachteten Intervalls
       */
      if( z2 <= startValue && startValue <= z1 && z1 <= endValue )
      {
        posList.add( GeometryFactory.createGM_Position( x1, y1, z1 ) );

        final double x = x1 + (x2 - x1) * (startValue - z1) / (z2 - z1);
        final double y = y1 + (y2 - y1) * (startValue - z1) / (z2 - z1);
        final double z = startValue;
        posList.add( GeometryFactory.createGM_Position( x, y, z ) );
      }

      /*
       * Knoten 1 liegt innerhalb, Knoten 2 liegt innerhalb Knoten 2.z > Knoten 1.z des aktuell betrachteten Intervalls
       */
      if( startValue <= z2 && z1 <= endValue && z2 < z1 )
      {
        posList.add( GeometryFactory.createGM_Position( x1, y1, z1 ) );

        posList.add( GeometryFactory.createGM_Position( x2, y2, z2 ) );
      }
    }

    final int size = posList.size();
    if( size < 3 )
      return null;

    /*
     * Markieren doppelt erzeugter Punkte und gleichzeitiges Löschen (Nicht-kopieren in 2te Liste aufgrund der
     * Orientierung der triangulierten Ausgangsdreiecke sind erzeugten Farbflächen-Polygone ebenfalls automatisch
     * orientiert.
     */

    final List<GM_Position> posList2 = new ArrayList<>( size );
    posList2.add( posList.get( 0 ) );

    int numDoublePoints = 0; // Anzahl nicht doppelter Punkte in Farbklasse (bäh, unschön gelöst!)

    /* Schleife über alle erzeugte Knoten */
    for( int k = 0; k < size; k++ )
    {
      final int index = (k + 1) % size;

      /* Abstand zweier Punkte berechnen */
      final double distance = posList.get( index ).getDistance( posList.get( numDoublePoints ) );

      /*
       * Wenn Abstand groß genug ist, Punkt übernehmen und zu dicht gelegene Punkte ignorieren
       */
      // TODO: Sollte sich diese Grösse nicht an der Strichstärke orientieren?
      if( distance > VAL_EPS )
      {
        posList2.add( posList.get( index ) );
        numDoublePoints = numDoublePoints + 1;
      }
    }

    if( posList2.size() < 3 )
      return null;

    return posList2.toArray( new GM_Position[posList2.size()] );
  }

  private RELATION relateTriangle( final GM_Position[] positions, final double startValue, final double endValue )
  {
    boolean inside = true;
    boolean upper = true;
    boolean lower = true;

    for( int i = 0; i < positions.length - 1; i++ )
    {
      final GM_Position position = positions[i];

      final double z = position.getZ();

      inside &= startValue < z && z <= endValue;
      lower &= z <= startValue;
      upper &= z > endValue;
    }

    if( inside )
      return RELATION.INSIDE;

    if( upper || lower )
      return RELATION.OUTSIDE;

    return RELATION.BETWEEN;
  }
}
