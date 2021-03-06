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
package org.kalypso.ogc.gml.map.widgets;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.gml.command.ChangeExtentCommand;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.widgets.AbstractWidget;

/**
 * This class performs a zoomin event. It will be performed by setting the map boundaries to the rectangle selected by
 * the client or centering the map onto the point the user had mouse-clicked to.
 * 
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @author doemming
 */
public class ZoomInByRectWidget extends AbstractWidget
{
  public ZoomInByRectWidget( final String name, final String tooltip )
  {
    super( name, tooltip );
  }

  public ZoomInByRectWidget( )
  {
    super( "zoom in", "" ); //$NON-NLS-1$ //$NON-NLS-2$

  }

  private Point m_endPoint = null;

  private Point m_startPoint = null;

  private static final double MIN_PIXEL_ZOOM_BOX = 20;

  @Override
  public void mouseDragged( final MouseEvent e )
  {
    // e.getButton() won't work for mouseDragged()
    if( Objects.isNull( m_startPoint ) )
      return;

    m_endPoint = e.getPoint();

    final IMapPanel panel = getMapPanel();
    if( Objects.isNotNull( panel ) )
      panel.repaintMap();

    e.consume();
  }

  @Override
  public void mousePressed( final MouseEvent e )
  {
    if( MouseEvent.BUTTON1 != e.getButton() )
      return;

    m_startPoint = e.getPoint();
    m_endPoint = null;

    e.consume();
  }

  @Override
  public void mouseReleased( final MouseEvent e )
  {
    if( MouseEvent.BUTTON1 != e.getButton() )
      return;

    m_endPoint = e.getPoint();
    perform();

    e.consume();
  }

  /**
   * paints the dragged rectangle defined by the start and end point of the drag box
   */
  @Override
  public void paint( final Graphics g )
  {
    if( m_startPoint != null && m_endPoint != null )
    {
      final int x1 = (int)m_startPoint.getX();
      final int y1 = (int)m_startPoint.getY();
      final int x2 = (int)m_endPoint.getX();
      final int y2 = (int)m_endPoint.getY();
      g.drawRect( x1 < x2 ? x1 : x2, y1 < y2 ? y1 : y2, Math.abs( x2 - x1 ), Math.abs( y2 - y1 ) );
    }
  }

  public void perform( )
  {
    final IMapPanel panel = getMapPanel();
    if( panel == null )
      return;

    if( m_startPoint != null && m_endPoint != null )
    {
      final double x1 = m_startPoint.getX();
      final double y1 = m_startPoint.getY();
      final double x2 = m_endPoint.getX();
      final double y2 = m_endPoint.getY();
      m_startPoint = null;
      m_endPoint = null;

      if( Math.abs( x1 - x2 ) > MIN_PIXEL_ZOOM_BOX && Math.abs( y1 - y2 ) > MIN_PIXEL_ZOOM_BOX )
      {
        final ChangeExtentCommand command = new ChangeExtentCommand( panel, getBox( x1, y1, x2, y2 ) );
        postViewCommand( command, null );
      }
      else
        panel.repaintMap();
    }
  }

}