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
package org.kalypso.ogc.gml.map.widgets.advanced.selection;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.delegates.AddRemovePolygonDelegate;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.delegates.DrawingPolygonDelegate;
import org.kalypso.ogc.gml.map.widgets.advanced.selection.delegates.RectanglePolygonDelegate;
import org.kalypso.ogc.gml.widgets.AbstractKeyListenerWidget;

/**
 * @author Dirk Kuch
 */
public class AdvancedPolygonSelectionWidget extends AbstractKeyListenerWidget implements IAdvancedSelectionWidget
{
  List<IAdvancedSelectionWidgetDelegate> m_delegates = new ArrayList<IAdvancedSelectionWidgetDelegate>();

  IAdvancedSelectionWidgetDelegate m_current = null;
  
  public AdvancedPolygonSelectionWidget( final IAdvancedSelectionWidgetDataProvider provider )
  {
    super( "AdvancedPolygonSelectionWidget" );

    m_delegates.add( new AddRemovePolygonDelegate( this, provider ) );
    m_delegates.add( new RectanglePolygonDelegate( this, provider ) );
    m_delegates.add( new DrawingPolygonDelegate( this, provider ) );
    
    m_current = m_delegates.get( 0 );
    
    new UIJob( "" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        setCursor( getCurrentDelegate().getCursor() );

        return Status.OK_STATUS;
      }
    }.schedule( 500 );

  }


  protected IAdvancedSelectionWidgetDelegate getCurrentDelegate( )
  {
    return m_current;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    paintToolTip( g );

    getCurrentDelegate().paint( g );
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#leftPressed(java.awt.Point)
   */
  @Override
  public void leftPressed( final java.awt.Point p )
  {
    getCurrentDelegate().leftPressed( p );
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#leftReleased(java.awt.Point)
   */
  @Override
  public void leftReleased( final java.awt.Point p )
  {
    getCurrentDelegate().leftReleased( p );
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#doubleClickedLeft(java.awt.Point)
   */
  @Override
  public void doubleClickedLeft( final Point p )
  {
    getCurrentDelegate().doubleClickedLeft( p );
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#getToolTip()
   */
  @Override
  public String getToolTip( )
  {
    final String[] tip = m_current.getTooltip();

    return String.format( "%s <SPACE>", tip[0] );
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.IAdvancedEditWidget#getIMapPanel()
   */
  @Override
  public IMapPanel getIMapPanel( )
  {
    return getMapPanel();
  }

  /**
   * Escape Key pressed? -> reset / deactivate widget
   * 
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#keyReleased(java.awt.event.KeyEvent)
   */
  @Override
  public void keyReleased( final KeyEvent e )
  {
    final int keyCode = e.getKeyCode();
    if( KeyEvent.VK_SPACE == keyCode )
    {
      switchMode();
    }

    super.keyReleased( e );

    getCurrentDelegate().keyReleased( e );
  }

  private void switchMode( )
  {
    int index = m_delegates.indexOf( m_current );
    index++;

    if( index == m_delegates.size() )
      index = 0;
    
    m_current = m_delegates.get( index );
    setCursor( getCurrentDelegate().getCursor() );

    getMapPanel().repaintMap();
  }

}
