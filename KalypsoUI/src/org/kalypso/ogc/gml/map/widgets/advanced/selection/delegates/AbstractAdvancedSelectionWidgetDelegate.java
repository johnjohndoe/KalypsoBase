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
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidget;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDataProvider;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDelegate;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;


/**
 * @author kuch
 *
 */
public abstract class AbstractAdvancedSelectionWidgetDelegate implements IAdvancedSelectionWidgetDelegate
{
  private final IAdvancedSelectionWidget m_widget;

  private final IAdvancedSelectionWidgetDataProvider m_provider;

  private Point m_pressed = null;

  public AbstractAdvancedSelectionWidgetDelegate( final IAdvancedSelectionWidget widget, final IAdvancedSelectionWidgetDataProvider provider )
  {
    m_widget = widget;
    m_provider = provider;
  }

  protected IAdvancedSelectionWidget getWidget( )
  {
    return m_widget;
  }

  protected IAdvancedSelectionWidgetDataProvider getDataProvider( )
  {
    return m_provider;
  }
  
  /**
   * @see org.kalypso.planer.client.ui.gui.widgets.measures.aw.IAdvancedSelectionWidgetDelegate#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    try
    {
      // underlying features
      final GM_Point point = m_widget.getCurrentGmPoint();
      final Feature[] features = m_provider.query( point, 0.1, getWidget().getEditMode() );
      
      // highlight these features
      for( final Feature feature : features )
      {
        highlightUnderlying(feature, g);
      }
    }
    catch( final GM_Exception e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  protected abstract void highlightUnderlying( final Feature feature, final Graphics g );

  /**
   * @see org.kalypso.ogc.gml.widgets.selection.IAdvancedSelectionWidgetDelegate#leftPressed(java.awt.Point)
   */
  @Override
  public void leftPressed( final Point p )
  {
    m_pressed = p;
  }
  
  /**
   * @see org.kalypso.ogc.gml.widgets.selection.IAdvancedSelectionWidgetDelegate#leftReleased(java.awt.Point)
   */
  @Override
  public void leftReleased( final Point p )
  {
   m_pressed = null; 
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.selection.IAdvancedSelectionWidgetDelegate#isMouseButtonPressed()
   */
  @Override
  public boolean isMouseButtonPressed( )
  {
    if( m_pressed == null )
      return false;

    return true;
  }
  
  /**
   * @see org.kalypso.ogc.gml.widgets.selection.IAdvancedSelectionWidgetDelegate#getMousePressed()
   */
  @Override
  public Point getMousePressed( )
  {
    return m_pressed;
  }
  
  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDelegate#keyReleased(java.awt.event.KeyEvent)
   */
  @Override
  public void keyReleased( final KeyEvent e )
  {
    // nothing to do
  }
  
  /**
   * @see org.kalypso.ogc.gml.map.widgets.advanced.selection.IAdvancedSelectionWidgetDelegate#doubleClickedLeft(java.awt.Point)
   */
  @Override
  public void doubleClickedLeft( final Point p )
  {
    // nothing to do
  }

  public Cursor getCursor( final Image imgCursor )
  {
    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    
    return toolkit.createCustomCursor( imgCursor, new Point( 2, 1 ), "selection cursor" );
  }
}
