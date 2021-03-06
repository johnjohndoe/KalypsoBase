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
package org.kalypso.ogc.gml.map.widgets.advanced.edit.delegates;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
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
import org.kalypso.ui.internal.i18n.Messages;
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
public class AdvancedEditModePointRemoveDelegate implements IAdvancedEditWidgetDelegate
{

  protected static final Color COLOR_WHITE = new Color( 0xFF, 0xFF, 0xFF );

  protected static final Color COLOR_VERTEX = new Color( 0xec, 0x44, 0x4a );

  static final IPointHighLighter REMOVABLE_VERTEX_POINT = new IPointHighLighter()
  {
    final int size = 14;

    @Override
    public void draw( final Graphics g, final java.awt.Point point )
    {
      final Color original = g.getColor();
      g.setColor( COLOR_VERTEX );
      g.fillOval( point.x - size / 2, point.y - size / 2, size, size );
      g.setColor( COLOR_WHITE );
      g.fillOval( point.x - size / 2 + 3, point.y - size / 2 + 3, size - 6, size - 6 );

      g.setColor( original );
    }
  };

  private final IAdvancedEditWidget m_widget;

  private final IAdvancedEditWidgetDataProvider m_provider;

  private IAdvancedEditWidgetResult[] m_lastPossibleVertexPoint;

  public AdvancedEditModePointRemoveDelegate( final IAdvancedEditWidget widget, final IAdvancedEditWidgetDataProvider provider )
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
      final Point jtsPoint = (Point)JTSAdapter.export( gmp );

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
        m_lastPossibleVertexPoint = null;
      }
      else
      {
        final AdvancedEditWidgetResult vertexPoint = new AdvancedEditWidgetResult( underlying.getFeature(), findVertexPoint( underlying ) );
        m_lastPossibleVertexPoint = findVertexPoints( mapGeometries, vertexPoint );

        if( !ArrayUtils.isEmpty( m_lastPossibleVertexPoint ) )
        {
          // drawing of one point is enough
          GeometryPainter.highlightPoints( g, m_widget.getIMapPanel(), new Geometry[] { m_lastPossibleVertexPoint[0].getGeometry() }, REMOVABLE_VERTEX_POINT );
        }
      }

    }
    catch( final GM_Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

  }

  private IAdvancedEditWidgetResult[] findVertexPoints( final Map<Geometry, Feature> map, final AdvancedEditWidgetResult vertexPoint )
  {
    if( vertexPoint.getGeometry() == null )
      return new IAdvancedEditWidgetResult[] {};

    final List<IAdvancedEditWidgetResult> results = new ArrayList<>();

    final Set<Entry<Geometry, Feature>> entries = map.entrySet();
    for( final Entry<Geometry, Feature> entry : entries )
    {
      final Geometry geometry = entry.getKey();
      if( geometry instanceof Polygon )
      {
        final Polygon polygon = (Polygon)geometry;
        final LineString ring = polygon.getExteriorRing();
        if( ring.contains( vertexPoint.getGeometry() ) )
        {
          results.add( new AdvancedEditWidgetResult( entry.getValue(), vertexPoint.getGeometry() ) );
        }
      }
    }

    return results.toArray( new IAdvancedEditWidgetResult[] {} );
  }

  private Point findVertexPoint( final IAdvancedEditWidgetGeometry underlying )
  {
    final Geometry geometry = underlying.getUnderlyingGeometry();
    if( geometry == null )
      return null;

    if( !(geometry instanceof Polygon) )
      throw new UnsupportedOperationException();

    final Polygon polygon = (Polygon)geometry;
    final LineString ring = polygon.getExteriorRing();
    final Point snapped = MapUtilities.snap( ring, underlying.getCurrentPoint(), SNAP_TYPE.SNAP_TO_POINT, getRange() );

    return snapped;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.aew.IAdvancedEditWidgetDelegate#getToolTip()
   */
  @Override
  public String getToolTip( )
  {
    return Messages.getString( "org.kalypso.ogc.gml.map.widgets.advanced.edit.delegates.AdvancedEditModePointRemoveDelegate.0" ); //$NON-NLS-1$
  }

  @Override
  public void leftReleased( final java.awt.Point p )
  {
    if( ArrayUtils.isEmpty( m_lastPossibleVertexPoint ) )
      return;

    final Set<FeatureChange> changes = new HashSet<>();

    for( final IAdvancedEditWidgetResult result : m_lastPossibleVertexPoint )
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
          final Polygon polygon = (Polygon)geometry;
          final Point pDelete = (Point)result.getGeometry();
          final Coordinate delete = pDelete.getCoordinate();

          final List<Coordinate> myCoordinates = new ArrayList<>();

          final LineString ring = polygon.getExteriorRing();
          final Coordinate[] coordinates = ring.getCoordinates();
          if( coordinates.length < 4 )
            return;

          boolean closeRing = false;
          for( int i = 0; i < coordinates.length; i++ )
          {
            final Coordinate coordinate = coordinates[i];
            if( i == 0 && coordinate.equals( delete ) )
            {
              closeRing = true;
              continue;
            }
            else if( coordinate.equals( delete ) )
            {
              continue;
            }

            myCoordinates.add( coordinate );
          }

          if( closeRing )
          {
            myCoordinates.add( myCoordinates.get( 0 ) );
          }

          final GeometryFactory factory = new GeometryFactory( polygon.getPrecisionModel(), polygon.getSRID() );
          final LinearRing updatedRing = factory.createLinearRing( myCoordinates.toArray( new Coordinate[] {} ) );
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

  @Override
  public double getRange( )
  {
    final double width = m_widget.getIMapPanel().getBoundingBox().getWidth();
    final double factor = width / 32;

    return factor;
  }

}
