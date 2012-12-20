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

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * @author bce
 */
public abstract class AbstractWidget implements IWidget
{
  private IMapPanel m_mapPanel;

  private ICommandTarget m_commandPoster;

  private final String m_name;

  private final String m_toolTip;

  /**
   * The map of all parameter. May be null.
   */
  private Map<String, String> m_parameter;

  public AbstractWidget( final String commandId )
  {
    final ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService( ICommandService.class );
    final Command command = cs.getCommand( commandId );
    if( !command.isDefined() )
    {
      m_name = "undefined"; //$NON-NLS-1$
      m_toolTip = "undefined"; //$NON-NLS-1$
    }
    else
    {
      try
      {
        m_name = command.getName();
        m_toolTip = command.getDescription();
      }
      catch( final NotDefinedException e )
      {
        throw new IllegalStateException( e );
      }
    }
    m_mapPanel = null;
    m_commandPoster = null;
    m_parameter = null;
  }

  public AbstractWidget( final String name, final String toolTip )
  {
    m_mapPanel = null;
    m_commandPoster = null;
    m_name = name;
    m_toolTip = toolTip;
    m_parameter = null;
  }

  @Override
  public WIDGET_TYPE getType( )
  {
    // radio is default activation type
    return WIDGET_TYPE.eRadio;
  }

  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    m_commandPoster = commandPoster;
    m_mapPanel = mapPanel;
  }

  public void setCursor( final Cursor cursor )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel != null )
      mapPanel.setCursor( cursor );
  }

  @Override
  public void setSelection( final ISelection selection )
  {
    // does nothing on default
  }

  /**
   * @param selection
   *          The selection of the part, here the selection of the map which is the selection of the active theme TODO:
   *          maybe it is better to give the whole selection
   * @see org.kalypso.ogc.gml.widgets.IWidget#isActive()
   */
  @Override
  public synchronized boolean canBeActivated( final ISelection selection, final IMapPanel mapPanel )
  {
    return true;
  }

  protected final void postViewCommand( final ICommand command, final Runnable runAfterCommand )
  {
    m_commandPoster.postCommand( command, runAfterCommand );
  }

  protected final GM_Position getPosition( final Point pixelPoint )
  {
    final GeoTransform transform = m_mapPanel.getProjection();
    final GM_Position pixelPos = GeometryFactory.createGM_Position( pixelPoint.getX(), pixelPoint.getY() );
    return transform.getSourcePoint( pixelPos );
  }

  // Helper
  protected final GM_Envelope getDragbox( final int mx, final int my, final int dx )
  {
    if( m_mapPanel == null )
      return null;

    final double ratio = getRatio();

    final GeoTransform transform = m_mapPanel.getProjection();
    final double gisMX = transform.getSourceX( mx );
    final double gisMY = transform.getSourceY( my );

    final double gisX1 = transform.getSourceX( mx - dx );
    final double gisDX = gisMX - gisX1;

    final double gisDY = gisDX * ratio;

    final double gisX2 = gisMX + gisDX;
    final double gisY1 = gisMY - gisDY;
    final double gisY2 = gisMY + gisDY;

    return GeometryFactory.createGM_Envelope( gisX1, gisY1, gisX2, gisY2, m_mapPanel.getMapModell().getCoordinatesSystem() );
  }

  protected final double getRatio( )
  {
    final GM_Envelope boundingBox = m_mapPanel.getBoundingBox();

    final double ratio = boundingBox.getHeight() / boundingBox.getWidth();
    return ratio;
  }

  /*
   * returns GM_Envelope for the pixel xmin, ymin, xmax, ymax.
   */
  protected final GM_Envelope getBox( final double x, final double y, final double x2, final double y2 )
  {
    if( m_mapPanel == null )
      return null;

    final GeoTransform gt = m_mapPanel.getProjection();
    if( gt == null )
      return null;

    return GeometryFactory.createGM_Envelope( gt.getSourceX( x ), gt.getSourceY( y ), gt.getSourceX( x2 ), gt.getSourceY( y2 ), m_mapPanel.getMapModell().getCoordinatesSystem() );
  }

  /**
   * This function returns the parameter value for the given paramater key.
   * 
   * @param key
   *          The parameter key.
   * @return The parameter value, or null, if the parameter does not exist.
   */
  protected final String getParameter( final String key )
  {
    if( m_parameter == null )
      return null;

    return m_parameter.get( key );
  }

  @Override
  public void finish( )
  {
  }

  @Override
  public void paint( final Graphics g, final GeoTransform world2screen, final IProgressMonitor progressMonitor )
  {
    // not implemented by default
  }

  @Override
  public void paint( final Graphics g )
  {
    // not implemented by default
  }

  /**
   * Causes the map to be repainted. The {@link IWidget#paint(Graphics)} method will be called soon.<br>
   * Does not invalidate the map.<br>
   * Use this method, if the state of the widget changes.
   */
  protected void repaintMap( )
  {
    final IMapPanel panel = getMapPanel();
    if( panel != null )
    {
      panel.repaintMap();
    }
  }

  @Override
  public final IMapPanel getMapPanel( )
  {
    return m_mapPanel;
  }

  public ICommandTarget getCommandTarget( )
  {
    return m_commandPoster;
  }

  public IKalypsoTheme getActiveTheme( )
  {
    try
    {
      return m_mapPanel.getMapModell().getActiveTheme();
    }
    catch( final Exception e )
    {
      return null;
    }
  }

  @Override
  public String getName( )
  {
    return m_name;
  }

  @Override
  public String getToolTip( )
  {
    return m_toolTip;
  }

  @Override
  public void keyPressed( final KeyEvent event )
  {
    // not implemented by default
  }

  @Override
  public void keyReleased( final KeyEvent event )
  {
    // not implemented by default
  }

  @Override
  public void keyTyped( final KeyEvent event )
  {
    // not implemented by default
  }

  @Override
  public void setParameter( final Map<String, String> parameter )
  {
    m_parameter = parameter;
  }

  @Override
  public void mouseClicked( final MouseEvent event )
  {
    // not implemented by default
  }

  @Override
  public void mouseDragged( final MouseEvent event )
  {
    // not implemented by default
  }

  @Override
  public void mouseEntered( final MouseEvent event )
  {
    // not implemented by default
  }

  @Override
  public void mouseExited( final MouseEvent event )
  {
    // not implemented by default
  }

  @Override
  public void mouseMoved( final MouseEvent event )
  {
    // not implemented by default
  }

  @Override
  public void mousePressed( final MouseEvent event )
  {
    // not implemented by default
  }

  @Override
  public void mouseReleased( final MouseEvent event )
  {
    // not implemented by default
  }

  @Override
  public void mouseWheelMoved( final MouseWheelEvent event )
  {
    // not implemented by default
  }
}