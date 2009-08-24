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
package org.kalypso.ogc.gml.map.widgets.advanced.selection.delegates;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidget;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDataProvider;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetGeometryProvider;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidget.EDIT_MODE;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Exception;

/**
 * @author Dirk Kuch
 */
public class AddRemovePolygonDelegate extends AbstractAdvancedSelectionWidgetDelegate
{
  private static BufferedImage IMG_CURSOR_ADD;

  private static BufferedImage IMG_CURSOR_REMOVE;

  private static Cursor ADD_CURSOR;

  private static Cursor REMOVE_CURSOR;

  private EDIT_MODE m_lastMode = null;

  private boolean m_modeSwitched = false;

  public AddRemovePolygonDelegate( final IAdvancedSelectionWidget widget, final IAdvancedSelectionWidgetDataProvider provider, final IAdvancedSelectionWidgetGeometryProvider geometryProvider )
  {
    super( widget, provider, geometryProvider );
  }

  /**
   * @see org.kalypso.planer.client.ui.gui.widgets.measures.aw.IAdvancedSelectionWidgetDelegate#leftReleased(java.awt.Point)
   */
  @Override
  public void leftReleased( final Point p )
  {
    super.leftReleased( p );

    try
    {
      final Feature[] features = getDataProvider().query( getSurface( getWidget().getCurrentGmPoint() ), getEditMode() );

      getDataProvider().post( features, getEditMode() );
    }
    catch( final Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDelegate#getEditMode()
   */
  @Override
  public EDIT_MODE getEditMode( )
  {
    try
    {
      final Feature[] features = getDataProvider().query( getSurface( getWidget().getCurrentGmPoint() ), EDIT_MODE.eRemove );

      if( !ArrayUtils.isEmpty( features ) )
      {
        if( m_lastMode != EDIT_MODE.eRemove )
        {
          m_modeSwitched = true;
          m_lastMode = EDIT_MODE.eRemove;
        }

        return EDIT_MODE.eRemove;
      }
    }
    catch( final GM_Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    if( m_lastMode != EDIT_MODE.eAdd )
    {
      m_modeSwitched = true;
      m_lastMode = EDIT_MODE.eAdd;
    }

    return EDIT_MODE.eAdd;
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDelegate#getTooltip()
   */
  @Override
  public String[] getTooltip( )
  {
    return new String[] { Messages.getString("org.kalypso.ogc.gml.map.widgets.advanced.selection.delegates.AddRemovePolygonDelegate.0") }; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDelegate#getCursor()
   */
  @Override
  public Cursor getCursor( )
  {
    try
    {
      if( IMG_CURSOR_ADD == null )
        IMG_CURSOR_ADD = ImageIO.read( RemovePolygonDelegate.class.getResourceAsStream( "images/cursor_add.png" ) ); //$NON-NLS-1$

      if( IMG_CURSOR_REMOVE == null )
        IMG_CURSOR_REMOVE = ImageIO.read( RemovePolygonDelegate.class.getResourceAsStream( "images/cursor_remove.png" ) ); //$NON-NLS-1$

      final Toolkit toolkit = Toolkit.getDefaultToolkit();
      if( ADD_CURSOR == null )
        ADD_CURSOR = toolkit.createCustomCursor( IMG_CURSOR_ADD, new Point( 2, 1 ), "selection add cursor" ); //$NON-NLS-1$

      if( REMOVE_CURSOR == null )
        REMOVE_CURSOR = toolkit.createCustomCursor( IMG_CURSOR_REMOVE, new Point( 2, 1 ), "selection remove cursor" ); //$NON-NLS-1$

      return getCursor( getEditMode() );
    }
    catch( final IOException e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return null;
  }

  private Cursor getCursor( final EDIT_MODE mode )
  {
    if( EDIT_MODE.eAdd.equals( mode ) )
      return ADD_CURSOR;
    else if( EDIT_MODE.eRemove.equals( mode ) )
      return REMOVE_CURSOR;

    return ADD_CURSOR;
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.delegates.AbstractAdvancedSelectionWidgetDelegate#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    if( m_modeSwitched )
    {
      getWidget().setCursor( getCursor() );
      m_modeSwitched = false;
    }

    super.paint( g );
  }
}
