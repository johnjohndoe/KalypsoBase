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
package org.kalypso.ogc.gml.map.widgets.advanced.selection.delegates;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidget;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDataProvider;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidget.EDIT_MODE;
import org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilderExtensionProvider;
import org.kalypso.ogc.gml.map.widgets.builders.PolygonGeometryBuilder;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Ring;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Dirk Kuch
 */
public class DrawingPolygonDelegate extends AbstractAdvancedSelectionWidgetDelegate implements IGeometryBuilderExtensionProvider
{
  private PolygonGeometryBuilder m_geoBuilder = null;

  public DrawingPolygonDelegate( final IAdvancedSelectionWidget widget, final IAdvancedSelectionWidgetDataProvider provider )
  {
    super( widget, provider );

    init();
  }

  private void init( )
  {
    final IMapPanel mapPanel = getWidget().getIMapPanel();
    if( mapPanel == null )
      return;

    final IMapModell mapModell = mapPanel.getMapModell();
    m_geoBuilder = new PolygonGeometryBuilder( 0, mapModell.getCoordinatesSystem(), this );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.AbstractAdvancedSelectionWidgetDelegate#leftPressed(java.awt.Point)
   */
  @Override
  public void leftPressed( final Point p )
  {
    super.leftPressed( p );

    if( m_geoBuilder == null )
      init();

    try
    {
      final IMapPanel mapPanel = getWidget().getIMapPanel();
      final GM_Point point = MapUtilities.transform( mapPanel, p );

      m_geoBuilder.addPoint( point );
    }
    catch( final Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.delegates.AbstractAdvancedSelectionWidgetDelegate#doubleClickedLeft(java.awt.Point)
   */
  @Override
  public void doubleClickedLeft( final Point p )
  {
    if( m_geoBuilder.size() >= 2 )
    {
      try
      {

        m_geoBuilder.addPoint( getWidget().getCurrentGmPoint() );
        final GM_Object gmo = m_geoBuilder.finish();
        m_geoBuilder.removeLastPoint();

        final Geometry jtsBase = JTSAdapter.export( gmo );

        final GM_Envelope envelope = gmo.getEnvelope();
        final List<Feature> myFeatures = new ArrayList<Feature>();
        
        final Feature[] features = getDataProvider().query( envelope );

        for( final Feature feature : features )
        {
          final Geometry jts = getDataProvider().resolveJtsGeometry( feature );
          if( jtsBase.intersects( jts ) )
            myFeatures.add( feature );
        }
        
        getDataProvider().post( myFeatures.toArray( new Feature[] {} ), EDIT_MODE.eDrawing );
      }
      catch( final Exception e )
      {
        KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }

      m_geoBuilder = null;
    }
    
    
  }
  
  
  /**
   * @see org.kalypso.ogc.gml.widgets.selection.AbstractAdvancedSelectionWidgetDelegate#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    final IMapPanel mapPanel = getWidget().getIMapPanel();
    if( mapPanel == null )
      return;

    if( m_geoBuilder == null )
    {
      init();
    }

    final Point point = getWidget().getCurrentPoint();
    if( point == null )
      return;

    m_geoBuilder.paint( g, mapPanel.getProjection(), point );

    if( m_geoBuilder.size() >= 2 )
    {
      try
      {

        m_geoBuilder.addPoint( getWidget().getCurrentGmPoint() );
        final GM_Object gmo = m_geoBuilder.finish();
        m_geoBuilder.removeLastPoint();

        final Geometry jtsBase = JTSAdapter.export( gmo );

        final GM_Envelope envelope = gmo.getEnvelope();
        final Feature[] features = getDataProvider().query( envelope );

        for( final Feature feature : features )
        {
          final Geometry jts = getDataProvider().resolveJtsGeometry( feature );
          if( jtsBase.intersects( jts ) )
            highlightUnderlying( feature, g );
        }
      }
      catch( final Exception e )
      {
        KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }
  }

  /**
   * @see org.kalypso.planer.client.ui.gui.widgets.measures.aw.AbstractAdvancedSelectionWidgetDelegate#highlightUnderlying(org.kalypsodeegree.model.feature.Feature,
   *      java.awt.Graphics)
   */
  @Override
  protected void highlightUnderlying( final Feature feature, final Graphics g )
  {
    final GM_Surface<GM_SurfacePatch> surface = (GM_Surface<GM_SurfacePatch>) getDataProvider().resolveGeometry( feature );

    final Color originalColor = g.getColor();
    g.setColor( new Color( 0, 255, 0, 128 ) );

    final GM_Ring ring = surface.getSurfaceBoundary().getExteriorRing();
    final GM_Position[] positions = ring.getPositions();

    int[] x_positions = new int[] {};
    int[] y_positions = new int[] {};

    for( final GM_Position position : positions )
    {
      final Point awt = MapUtilities.retransform( getWidget().getIMapPanel(), position );
      x_positions = ArrayUtils.add( x_positions, Double.valueOf( awt.getX() ).intValue() );
      y_positions = ArrayUtils.add( y_positions, Double.valueOf( awt.getY() ).intValue() );
    }

    Assert.isTrue( x_positions.length == y_positions.length );
    g.fillPolygon( x_positions, y_positions, x_positions.length );

    g.setColor( originalColor );

  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilderExtensionProvider#getTooltip()
   */
  @Override
  public String[] getTooltip( )
  {
    return new String[] { getWidget().getToolTip() };
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilderExtensionProvider#setCursor(java.awt.Cursor)
   */
  @Override
  public void setCursor( final Cursor cursor )
  {
    getWidget().setCursor( cursor );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.AbstractAdvancedSelectionWidgetDelegate#keyReleased(java.awt.event.KeyEvent)
   */
  @Override
  public void keyReleased( final KeyEvent e )
  {
    if( KeyEvent.VK_BACK_SPACE == e.getKeyCode() )
    {
      m_geoBuilder.removeLastPoint();
    }
  }
}
