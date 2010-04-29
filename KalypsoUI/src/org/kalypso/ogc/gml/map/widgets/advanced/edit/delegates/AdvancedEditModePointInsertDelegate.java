/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.ogc.gml.map.widgets.advanced.edit.delegates;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.i18n.Messages;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.jts.SnapUtilities.SNAP_TYPE;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.widgets.advanced.edit.AdvancedEditWidgetResult;
import org.kalypso.ogc.gml.map.widgets.advanced.edit.IAdvancedEditWidget;
import org.kalypso.ogc.gml.map.widgets.advanced.edit.IAdvancedEditWidgetDataProvider;
import org.kalypso.ogc.gml.map.widgets.advanced.edit.IAdvancedEditWidgetDelegate;
import org.kalypso.ogc.gml.map.widgets.advanced.edit.IAdvancedEditWidgetGeometry;
import org.kalypso.ogc.gml.map.widgets.advanced.edit.IAdvancedEditWidgetResult;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.GeometryPainter;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.IPointHighLighter;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Dirk Kuch
 */
public class AdvancedEditModePointInsertDelegate implements IAdvancedEditWidgetDelegate
{

  private static final Color COLOR_VERTEX = new Color( 0x36, 0x7c, 0xc7 );

  static final IPointHighLighter POSSIBLE_VERTEX_POINT = new IPointHighLighter()
  {
    Color cVertex = COLOR_VERTEX;

    final int size = 14;

    @Override
    public void draw( final Graphics g, final java.awt.Point point )
    {
      final Color original = g.getColor();
      g.setColor( cVertex );
      g.fillOval( point.x - size / 2, point.y - size / 2, size, size );
      g.setColor( original );
    }
  };

  private final IAdvancedEditWidget m_widget;

  private final IAdvancedEditWidgetDataProvider m_provider;

  private IAdvancedEditWidgetResult[] m_lastPossibleVertexPoints;

  public AdvancedEditModePointInsertDelegate( final IAdvancedEditWidget widget, final IAdvancedEditWidgetDataProvider provider )
  {
    m_widget = widget;
    m_provider = provider;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.aew.IAdvancedEditWidgetDelegate#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    final GM_Point gmp = m_widget.getCurrentGmPoint();
    if( gmp == null )
      return;

    try
    {
      // highligth existing points
      final Point jtsPoint = (Point) JTSAdapter.export( gmp );

      final Feature[] features = m_provider.query( gmp, getRange() );
      if( ArrayUtils.isEmpty( features ) )
        return;

      /* find existing points */
      final Map<Geometry, Feature> mapGeometries = m_provider.resolveJtsGeometries( features );
      GeometryPainter.highlightPoints( g, m_widget.getIMapPanel(), mapGeometries.keySet().toArray( new Geometry[] {} ), VERTEX );

      /* find underlying geometry */
      final IAdvancedEditWidgetGeometry underlying = DelegateHelper.findUnderlyingGeometry( mapGeometries, jtsPoint );
      if( underlying == null )
      {
        m_lastPossibleVertexPoints = null;
      }
      else
      {
        final AdvancedEditWidgetResult possible = new AdvancedEditWidgetResult( underlying.getFeature(), findPossibleVertexPointOnEdge( underlying ) );
        m_lastPossibleVertexPoints = findPossibleVertexPointsOnEdges( mapGeometries, possible );

        if( !ArrayUtils.isEmpty( m_lastPossibleVertexPoints ) )
        {
          // drawing of one point is enough
          GeometryPainter.highlightPoints( g, m_widget.getIMapPanel(), new Geometry[] { m_lastPossibleVertexPoints[0].getGeometry() }, POSSIBLE_VERTEX_POINT );
        }

// /* debug */
// for( final IAdvancedEditWidgetResult result : m_lastPossibleVertexPoints )
// {
// final Polygon geometry = (Polygon) m_provider.resolveJtsGeometry( result.getFeature());
// GeometryPainter.drawPolygon( m_widget.getIMapPanel(), g, geometry, new Color( 255, 255, 255 ), new Color( 0xa3, 0xc3,
        // 0xc9, 0x80 ) );
// }
      }

    }
    catch( final GM_Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

  }

  private IAdvancedEditWidgetResult[] findPossibleVertexPointsOnEdges( final Map<Geometry, Feature> map, final IAdvancedEditWidgetResult possible )
  {
    if( !(possible.getGeometry() instanceof Point) )
      return new IAdvancedEditWidgetResult[] {};

    final Point snapped = (Point) possible.getGeometry();

    final Set<IAdvancedEditWidgetResult> results = new HashSet<IAdvancedEditWidgetResult>();

    final Set<Entry<Geometry, Feature>> entries = map.entrySet();
    for( final Entry<Geometry, Feature> entry : entries )
    {
      final Geometry geometry = entry.getKey();
      if( !(geometry instanceof Polygon) )
      {
        continue;
      }


      final Polygon polygon = (Polygon) geometry;
      final LineString ring = polygon.getExteriorRing();

      final Point resnapped = MapUtilities.snap( ring, snapped, SNAP_TYPE.SNAP_TO_LINE, getRange() );
      if( resnapped != null )
      {
        // System.out.println( String.format( "distance %f", resnapped.distance( snapped ) ) );
        
        // add old snap point!
        results.add( new AdvancedEditWidgetResult( entry.getValue(), snapped ) );
      }
    }

    return results.toArray( new IAdvancedEditWidgetResult[] {} );
  }

  private Point findPossibleVertexPointOnEdge( final IAdvancedEditWidgetGeometry underlying )
  {
    final Geometry geometry = underlying.getUnderlyingGeometry();
    if( geometry == null )
      return null;

    if( !(geometry instanceof Polygon) )
      throw new NotImplementedException();

    final Polygon polygon = (Polygon) geometry;
    final LineString ring = polygon.getExteriorRing();

    final Point snapped = MapUtilities.snap( ring, underlying.getCurrentPoint(), SNAP_TYPE.SNAP_TO_LINE, getRange() );

    return snapped;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.aew.IAdvancedEditWidgetDelegate#getToolTip()
   */
  @Override
  public String getToolTip( )
  {
    return Messages.getString("org.kalypso.ogc.gml.map.widgets.advanced.edit.delegates.AdvancedEditModePointInsertDelegate.0"); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.aew.IAdvancedEditWidgetDelegate#leftReleased(java.awt.Point)
   */
  @Override
  public void leftReleased( final java.awt.Point p )
  {
    if( ArrayUtils.isEmpty( m_lastPossibleVertexPoints ) )
      return;

    final Set<FeatureChange> changes = new HashSet<FeatureChange>();

    for( final IAdvancedEditWidgetResult result : m_lastPossibleVertexPoints )
    {
      if( result.getGeometry() == null )
      {
        continue;
      }

      final Geometry geometry = m_provider.resolveJtsGeometry( result.getFeature() );
      if( geometry instanceof Polygon )
      {
        try
        {
          final Polygon polygon = (Polygon) geometry;
          final LineString ring = polygon.getExteriorRing();

          final Point add = (Point) result.getGeometry();
          final List<Point> points = new ArrayList<Point>();
          points.add( add );

          final GeometryFactory factory = new GeometryFactory( polygon.getPrecisionModel(), polygon.getSRID() );

          Coordinate[] coordinates = ring.getCoordinates();
          /* linear ring is closed | line string is not */
// coordinates = (Coordinate[]) ArrayUtils.remove( coordinates, coordinates.length - 1 );
          final LineString lineString = factory.createLineString( coordinates );

          final LineString resultLineString = JTSUtilities.addPointsToLine( lineString, points );
          coordinates = resultLineString.getCoordinates();
// coordinates = (Coordinate[]) ArrayUtils.add( coordinates, coordinates[0] ); // close linear ring

          final LinearRing updatedRing = factory.createLinearRing( coordinates );
          final Polygon updated = factory.createPolygon( updatedRing, new LinearRing[] {} );

          final FeatureChange[] myChanges = m_provider.getAsFeatureChanges( new AdvancedEditWidgetResult( result.getFeature(), updated ) );
          for( final FeatureChange change : myChanges )
          {
            changes.add( change );
          }
        }
        catch( final Exception e )
        {
          KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
        }
      }
    }

    try
    {
      m_provider.post( changes.toArray( new FeatureChange[] {} ) );
    }
    catch( final Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  public double getRange( )
  {
    final double width = m_widget.getIMapPanel().getBoundingBox().getWidth();
    final double factor = width / 10;

    return factor;
  }
}
