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
package org.kalypso.ogc.gml.widgets.base;

import java.awt.Point;
import java.awt.event.MouseEvent;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.ogc.gml.command.ChangeExtentCommand;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * @author Andreas von D�mming
 */
public class PanToWidget extends AbstractWidget
{
  private GeoTransform m_world2screen = null;

  private GM_Position m_startPoint = null;

  private GM_Position m_endPoint = null;

  private final int[] m_mouseButtons;

  public PanToWidget( final String name, final String toolTip )
  {
    super( name, toolTip );

    m_mouseButtons = new int[] { MouseEvent.BUTTON1, MouseEvent.BUTTON2 };
  }

  public PanToWidget( )
  {
    super( "pan to", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    m_mouseButtons = new int[] { MouseEvent.BUTTON1, MouseEvent.BUTTON2 };
  }

  public PanToWidget( final int[] mouseButtons )
  {
    super( "pan to", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    m_mouseButtons = mouseButtons;
  }

  @Override
  public void mouseDragged( final MouseEvent e )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel == null )
      return;

    final Point point = e.getPoint();

    if( m_world2screen != null && m_startPoint != null )
    {
      final GM_Position pixelPos = GeometryFactory.createGM_Position( point.getX(), point.getY() );
      m_endPoint = m_world2screen.getSourcePoint( pixelPos );

      final GM_Envelope panExtent = calcPanExtent();
      mapPanel.setBoundingBox( panExtent, false, false );
    }
  }

  private GM_Envelope calcPanExtent( )
  {
    final double[] vector = new double[] { m_startPoint.getX() - m_endPoint.getX(), m_startPoint.getY() - m_endPoint.getY() };

    final GM_Envelope startExtent = m_world2screen.getSourceRect();

    final GM_Position panMin = GeometryFactory.createGM_Position( startExtent.getMinX(), startExtent.getMinY() );
    final GM_Position panMax = GeometryFactory.createGM_Position( startExtent.getMaxX(), startExtent.getMaxY() );

    panMin.translate( vector );
    panMax.translate( vector );

    return GeometryFactory.createGM_Envelope( panMin, panMax, startExtent.getCoordinateSystem() );
  }

  @Override
  public void finish( )
  {
  }

  @Override
  public void mousePressed( final MouseEvent e )
  {
    if( !ArrayUtils.contains( m_mouseButtons, e.getButton() ) )
      return;

    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel == null )
      return;

    final Point point = e.getPoint();

    m_world2screen = mapPanel.getProjection();
    if( m_world2screen == null )
      return;

    final GM_Position pixelPos = GeometryFactory.createGM_Position( point.getX(), point.getY() );
    m_startPoint = m_world2screen.getSourcePoint( pixelPos );

    m_endPoint = null;
  }

  @Override
  public void mouseReleased( final MouseEvent e )
  {
    if( !ArrayUtils.contains( m_mouseButtons, e.getButton() ) )
      return;

    if( m_world2screen == null )
      return;

    final Point point = e.getPoint();

    final GM_Position pixelPos = GeometryFactory.createGM_Position( point.getX(), point.getY() );
    m_endPoint = m_world2screen.getSourcePoint( pixelPos );

    perform();
  }

  public void perform( )
  {
    final IMapPanel mapPanel = getMapPanel();

    if( m_world2screen != null && m_startPoint != null && m_endPoint != null && !m_startPoint.equals( m_endPoint ) )
    {
      final GM_Envelope panExtent = calcPanExtent();

      m_startPoint = null;
      m_world2screen = null;
      m_endPoint = null;

      final ChangeExtentCommand command = new ChangeExtentCommand( mapPanel, panExtent );
      postViewCommand( command, null );
    }
  }

  @Override
  public WIDGET_TYPE getType( )
  {
    return WIDGET_TYPE.eRadio;
  }
}