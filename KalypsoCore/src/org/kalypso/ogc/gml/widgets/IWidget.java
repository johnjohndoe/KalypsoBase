/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.widgets;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypsodeegree.graphics.transformation.GeoTransform;

/**
 * @author bce
 */
public interface IWidget extends MouseListener, MouseMotionListener, MouseWheelListener
{
  // FIXME: nonsene; makes everything more difficult: instead: keep different lists for different types of widgets
  public enum WIDGET_TYPE
  {
    eBackground,
    eRadio,
    eToggle;
  }

  WIDGET_TYPE getType( );

  String getName( );

  // FIXME: this is the tooltip of the widget button, NOT intended to be used for the tooltip rendered inside the map!
  String getToolTip( );

  IMapPanel getMapPanel( );

  // KeyEvents
  void keyPressed( KeyEvent e );

  void keyReleased( KeyEvent e );

  void keyTyped( KeyEvent e );

  // Map graphics
  void paint( Graphics g, GeoTransform world2screen, IProgressMonitor progressMonitor );
  
  // widget graphics
  void paint( Graphics g);

  void finish( );

  void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel );

  /**
   * Will be called:
   * <ul>
   * <li>after activation</li>
   * <li>everytime the selection changes if active</li>
   * </ul>
   */
  void setSelection( final ISelection selection );

  /**
   * This function checks and returns if the widget may be activated. This may be used by the action delegates, to
   * determine if the action should be enabled.
   */
  boolean canBeActivated( final ISelection selection, final IMapPanel mapPanel );

  /**
   * This function sets the map of all parameter.
   * 
   * @param parameter
   *          The map of all parameter. May be null.
   */
  void setParameter( Map<String, String> parameter );
}