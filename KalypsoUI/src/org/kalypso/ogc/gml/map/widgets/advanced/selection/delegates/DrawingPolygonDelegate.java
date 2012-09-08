/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidget;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidget.EDIT_MODE;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDataProvider;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetGeometryProvider;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.WidgetCursors;
import org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilderExtensionProvider;
import org.kalypso.ogc.gml.map.widgets.builders.PolygonGeometryBuilder;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Dirk Kuch
 */
public class DrawingPolygonDelegate extends AbstractAdvancedSelectionWidgetDelegate implements IGeometryBuilderExtensionProvider
{
  private PolygonGeometryBuilder m_geoBuilder = null;

  private Cursor m_cursor;

  public DrawingPolygonDelegate( final IAdvancedSelectionWidget widget, final IAdvancedSelectionWidgetDataProvider provider, final IAdvancedSelectionWidgetGeometryProvider geometryProvider )
  {
    super( widget, provider, geometryProvider );

    init();
  }

  private void init( )
  {
    final IMapPanel mapPanel = getWidget().getIMapPanel();
    if( mapPanel == null )
      return;

    final IMapModell mapModell = mapPanel.getMapModell();
    m_geoBuilder = new PolygonGeometryBuilder( 0, mapModell.getCoordinatesSystem(), this, false );
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
        final List<Feature> myFeatures = new ArrayList<>();

        final Feature[] features = getDataProvider().query( (GM_Surface< ? >) gmo, getEditMode() );

        for( final Feature feature : features )
        {
          final Geometry jts = getGeometryProvider().resolveJtsGeometry( feature );
          if( jtsBase.intersects( jts ) )
            myFeatures.add( feature );
        }

        getDataProvider().post( myFeatures.toArray( new Feature[] {} ), getEditMode() );
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

        final Feature[] features = getDataProvider().query( (GM_Surface< ? >) gmo, getEditMode() );

        final List<Feature> highlight = new ArrayList<>();

        for( final Feature feature : features )
        {
          final Geometry jts = getGeometryProvider().resolveJtsGeometry( feature );
          if( jtsBase.intersects( jts ) )
            highlight.add( feature );
        }

        highlightUnderlyingGeometries( highlight.toArray( new Feature[] {} ), g, EDIT_MODE.eAdd );
      }
      catch( final Exception e )
      {
        KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }
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

  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDelegate#getEditMode()
   */
  @Override
  public EDIT_MODE getEditMode( )
  {
    return EDIT_MODE.eAdd;
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilderExtensionProvider#getTooltip()
   */
  @Override
  public String[] getTooltip( )
  {
    return new String[] { Messages.getString( "org.kalypso.ogc.gml.map.widgets.advanced.selection.delegates.DrawingPolygonDelegate.0" ) }; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDelegate#getCursor()
   */
  @Override
  public Cursor getCursor( )
  {
    if( m_cursor == null )
      m_cursor = WidgetCursors.createAddDrawingCursor();
    return m_cursor;
  }
}
