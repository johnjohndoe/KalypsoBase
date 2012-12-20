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
package org.kalypso.ogc.gml.map.widgets;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypso.ogc.gml.widgets.IWidget;
import org.kalypsodeegree.graphics.transformation.GeoTransform;

/**
 * @author Thomas Jung
 */
public class AbstractDelegateWidget2 extends AbstractWidget
{
  private IWidget m_delegate = null;

  public AbstractDelegateWidget2( final String name, final String tooltip, final IWidget delegate )
  {
    super( name, tooltip );

    m_delegate = delegate;
  }

  protected IWidget getDelegate( )
  {
    return m_delegate;
  }

  protected void setDelegate( final IWidget delegate )
  {
    if( m_delegate != null )
      m_delegate.finish();

    m_delegate = delegate;

    if( m_delegate != null )
    {
      final ICommandTarget commandTarget = getCommandTarget();
      final IMapPanel mapPanel = getMapPanel();
      m_delegate.activate( commandTarget, mapPanel );
    }

    repaintMap();
  }

  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    if( m_delegate != null )
      m_delegate.activate( commandPoster, mapPanel );

    super.activate( commandPoster, mapPanel );
  }

  @Override
  public synchronized boolean canBeActivated( final ISelection selection, final IMapPanel mapPanel )
  {
    if( m_delegate != null )
      return m_delegate.canBeActivated( selection, mapPanel );
    else
      return super.canBeActivated( selection, mapPanel );
  }

  @Override
  public void finish( )
  {
    if( m_delegate != null )
      m_delegate.finish();

    super.finish();
  }

  @Override
  public void setSelection( final ISelection selection )
  {
    if( m_delegate != null )
      m_delegate.setSelection( selection );

    super.setSelection( selection );
  }

  @Override
  public String getName( )
  {
    if( m_delegate != null )
      return m_delegate.getName();
    else
      return super.getName();
  }

  @Override
  public String getToolTip( )
  {
    if( m_delegate != null )
      return m_delegate.getToolTip();
    else
      return super.getToolTip();
  }

  @Override
  public void paint( final Graphics g, final GeoTransform world2screen, final IProgressMonitor progressMonitor )
  {
    if( m_delegate != null )
      m_delegate.paint( g, world2screen, progressMonitor );
  }
  
  @Override
  public void paint( final Graphics g )
  {
    if( m_delegate != null )
      m_delegate.paint( g );
  }

  @Override
  public void keyPressed( final KeyEvent e )
  {
    if( m_delegate != null )
      m_delegate.keyPressed( e );
  }

  @Override
  public void keyReleased( final KeyEvent e )
  {
    if( m_delegate != null )
      m_delegate.keyReleased( e );
  }

  @Override
  public void keyTyped( final KeyEvent e )
  {
    if( m_delegate != null )
      m_delegate.keyTyped( e );
  }

  @Override
  public void mouseClicked( final MouseEvent e )
  {
    if( m_delegate != null )
      m_delegate.mouseClicked( e );
  }

  @Override
  public void mousePressed( final MouseEvent e )
  {
    if( m_delegate != null )
      m_delegate.mousePressed( e );
  }

  @Override
  public void mouseReleased( final MouseEvent e )
  {
    if( m_delegate != null )
      m_delegate.mouseReleased( e );
  }

  @Override
  public void mouseEntered( final MouseEvent e )
  {
    if( m_delegate != null )
      m_delegate.mouseEntered( e );
  }

  @Override
  public void mouseExited( final MouseEvent e )
  {
    if( m_delegate != null )
      m_delegate.mouseExited( e );
  }

  @Override
  public void mouseDragged( final MouseEvent e )
  {
    if( m_delegate != null )
      m_delegate.mouseDragged( e );
  }

  @Override
  public void mouseMoved( final MouseEvent e )
  {
    if( m_delegate != null )
      m_delegate.mouseMoved( e );
  }

  @Override
  public void mouseWheelMoved( final MouseWheelEvent e )
  {
    if( m_delegate != null )
      m_delegate.mouseWheelMoved( e );
  }
}
