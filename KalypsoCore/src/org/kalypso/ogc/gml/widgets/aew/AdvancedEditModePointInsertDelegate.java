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
package org.kalypso.ogc.gml.widgets.aew;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.jts.SnapUtilities.SNAP_TYPE;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.widgets.tools.GeometryPainter;
import org.kalypso.ogc.gml.widgets.tools.IPointHighLighter;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * @author Dirk Kuch
 */
public class AdvancedEditModePointInsertDelegate implements IAdvancedEditWidgetDelegate
{

  static final IPointHighLighter POSSIBLE_VERTEX_POINT = new IPointHighLighter()
  {
    Color cVertex = new Color( 0x36, 0x7c, 0xc7 );

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

  private Point m_lastPossibleVertexPoint;

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
      final IAdvancedEditWidgetGeometry underlying = findUnderlyingGeometry( mapGeometries, jtsPoint );
      if( underlying == null )
      {
        m_lastPossibleVertexPoint = null;
      }
      else
      {
        m_lastPossibleVertexPoint = findPossibleVertexPointOnEdge( underlying );
        if( m_lastPossibleVertexPoint != null )
        {
          GeometryPainter.highlightPoints( g, m_widget.getIMapPanel(), new Geometry[] { m_lastPossibleVertexPoint }, POSSIBLE_VERTEX_POINT );
        }
      }

    }
    catch( final GM_Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

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
    final double range = getRange();

    final Point snapped = MapUtilities.snap( ring, underlying.getBasePoint(), SNAP_TYPE.SNAP_TO_LINE, range / 32 );

    return snapped;
  }

  private IAdvancedEditWidgetGeometry findUnderlyingGeometry( final Map<Geometry, Feature> geometries, final Point point )
  {  
    final Set<Entry<Geometry, Feature>> entries = geometries.entrySet();
    for( final Entry<Geometry, Feature> entry : entries )
    {
      try
      {
        final Geometry geometry = entry.getKey();
        final Geometry intersection = geometry.intersection( point );

        if( !intersection.isEmpty() )
          return new IAdvancedEditWidgetGeometry()
          {
            @Override
            public Point getBasePoint( )
            {
              return point;
            }

            @Override
            public Feature getFeature( )
            {
              return entry.getValue();
            }

            @Override
            public Geometry getUnderlyingGeometry( )
            {
              return geometry;
            }
          };
      }
      catch( final TopologyException e )
      {
        // nothing to do
        // System.out.println( "JTS TopologyException" );
      }
    }

    return null;
  }

  public double getRange( )
  {
    final IMapPanel mapPanel = m_widget.getIMapPanel();
    return mapPanel.getCurrentScale() * 4;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.aew.IAdvancedEditWidgetDelegate#getToolTip()
   */
  @Override
  public String getToolTip( )
  {
    return "Editiermodus: Punkte einfügen";
  }

}
