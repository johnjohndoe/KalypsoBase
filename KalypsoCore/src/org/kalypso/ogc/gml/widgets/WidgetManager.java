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
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.gml.command.ChangeExtentCommand;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.MapPanelUtilities;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ogc.gml.selection.IFeatureSelectionListener;
import org.kalypso.ogc.gml.widgets.IWidget.WIDGET_TYPE;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * widget controller of map view
 * 
 * @author vdoemming
 * @author Dirk Kuch
 */
public class WidgetManager implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, IWidgetManager
{
  private final Set<IWidgetChangeListener> m_widgetChangeListener = new HashSet<IWidgetChangeListener>();

  private final IFeatureSelectionListener m_featureSelectionListener = new IFeatureSelectionListener()
  {
    @Override
    public void selectionChanged( final Object source, final IFeatureSelection selection )
    {
      onSelectionChanged( selection );
    }
  };

  private final IMapPanel m_mapPanel;

  private final ICommandTarget m_commandTarget;

  private final Set<IWidget> m_widgets = Collections.synchronizedSet( new LinkedHashSet<IWidget>() );

  /** Widget used for middle mouse actions */
  private final IWidget m_middleWidget = new PanToWidget();

  /** If middle was pressed down; prohibits dragging any other widget */
  // FIXME: bad: use mouse-event states to determine this
  private boolean m_middleDown = false;

  public WidgetManager( final ICommandTarget commandTarget, final IMapPanel mapPanel )
  {
    m_mapPanel = mapPanel;
    m_commandTarget = commandTarget;

    m_mapPanel.getSelectionManager().addSelectionListener( m_featureSelectionListener );

    m_middleWidget.activate( commandTarget, mapPanel );
  }

  @Override
  public void dispose( )
  {
    addWidget( null );

    m_mapPanel.getSelectionManager().removeSelectionListener( m_featureSelectionListener );
  }

  @Override
  public ICommandTarget getCommandTarget( )
  {
    return m_commandTarget;
  }

  @Override
  public void mouseClicked( final MouseEvent e )
  {
    // FIXME don't iterate over all widgets - look for event.done flag (e.isConsumed)

    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      if( widget instanceof MouseListener )
      {
        ((MouseListener) widget).mouseClicked( e );
        if( e.isConsumed() )
          return;
      }

      /* Prevents handling of middle mouse if no widget is active */
      if( widget == null )
        return;

      if( e.isPopupTrigger() )
        widget.clickPopup( e.getPoint() );
      else
      {
        switch( e.getButton() )
        {
          case MouseEvent.BUTTON1:
          {
            if( e.getClickCount() == 1 )
              widget.leftClicked( e.getPoint() );
            else if( e.getClickCount() == 2 )
              widget.doubleClickedLeft( e.getPoint() );
          }
            break;

          case MouseEvent.BUTTON2:
            m_middleWidget.leftClicked( e.getPoint() );
            break;

          case MouseEvent.BUTTON3:
          {
            if( e.getClickCount() == 1 )
              widget.rightClicked( e.getPoint() );
            else if( e.getClickCount() == 2 )
              widget.doubleClickedRight( e.getPoint() );
          }
            break;

          default:
            break;
        }
      }
    }

  }

  @Override
  public void mouseMoved( final MouseEvent e )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      if( widget instanceof MouseMotionListener )
        ((MouseMotionListener) widget).mouseMoved( e );

      if( !e.isConsumed() && widget != null )
        widget.moved( e.getPoint() );
    }

    m_mapPanel.fireMouseMouveEvent( e.getX(), e.getY() );
  }

  // MouseMotionAdapter:
  @Override
  public void mouseDragged( final MouseEvent e )
  {
    // FIXME don't iterate over all widgets - look for event.done flag (e.isConsumed)

    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      if( widget instanceof MouseMotionListener )
      {
        ((MouseMotionListener) widget).mouseDragged( e );
        if( e.isConsumed() )
          return;
      }

      /* Prevent handling of middle mouse if no widget is active */
      if( widget == null )
        return;

      if( m_middleDown )
      {
        m_middleWidget.dragged( e.getPoint() );
        return;
      }

      widget.dragged( e.getPoint() );
    }

  }

  @Override
  public void mouseEntered( final MouseEvent e )
  {
    // FIXME don't iterate over all widgets - look for event.done flag (e.isConsumed)

    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      if( widget instanceof MouseListener )
        ((MouseListener) widget).mouseEntered( e );
    }
  }

  @Override
  public void mouseExited( final MouseEvent e )
  {
    // FIXME don't iterate over all widgets - look for event.done flag (e.isConsumed)

    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      if( widget instanceof MouseListener )
        ((MouseListener) widget).mouseExited( e );
    }
  }

  @Override
  public void mousePressed( final MouseEvent e )
  {
    // FIXME don't iterate over all widgets - look for event.done flag (e.isConsumed)

    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      if( widget instanceof MouseListener )
      {
        ((MouseListener) widget).mousePressed( e );
        if( e.isConsumed() )
          return;
      }

      /* Prevent handling of middle mouse if no widget is active */
      if( widget == null )
        continue;

      if( e.isPopupTrigger() && widget != null )
        widget.clickPopup( e.getPoint() );
      else
      {
        switch( e.getButton() )
        {
          case MouseEvent.BUTTON1:
            if( widget != null )
              widget.leftPressed( e.getPoint() );
            break;

          case MouseEvent.BUTTON2:
            m_middleDown = true;
            m_middleWidget.leftPressed( e.getPoint() );
            break;

          case MouseEvent.BUTTON3:
            if( widget != null )
              widget.rightPressed( e.getPoint() );
            break;

          default:
            break;
        }
      }
    }

  }

  @Override
  public void mouseReleased( final MouseEvent e )
  {
    // FIXME don't iterate over all widgets - look for event.done flag (e.isConsumed)
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      if( widget instanceof MouseListener )
      {
        ((MouseListener) widget).mouseReleased( e );
        if( e.isConsumed() )
          return;
      }

      /* Prevent handling of middle mouse if no widget is active */
      if( widget == null )
        return;

      if( e.isPopupTrigger() && getWidgets() != null )
        widget.clickPopup( e.getPoint() );
      else
      {
        switch( e.getButton() )
        {
          case MouseEvent.BUTTON1: // Left
            if( getWidgets() != null )
              widget.leftReleased( e.getPoint() );
            break;

          case MouseEvent.BUTTON2:
            m_middleWidget.leftReleased( e.getPoint() );
            break;

          case MouseEvent.BUTTON3: // Right
            if( getWidgets() != null )
              widget.rightReleased( e.getPoint() );
            break;

          default:
            break;
        }
      }
    }

    m_middleDown = false;
  }

  @Override
  public void mouseWheelMoved( final MouseWheelEvent e )
  {
    // FIXME don't iterate over all widgets - look for event.done flag (e.isConsumed)

    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      if( widget instanceof MouseWheelListener )
      {
        ((MouseWheelListener) widget).mouseWheelMoved( e );
        if( e.isConsumed() )
          return;
      }

      /* Prevent handling of middle mouse if no widget is active */
      if( widget == null )
        continue;

      e.consume();

      final int wheelRotation = e.getWheelRotation();

      final boolean in = wheelRotation > 0 ? false : true;

      GM_Envelope boundingBox = m_mapPanel.getBoundingBox();
      for( int i = 0; i < Math.abs( wheelRotation ); i++ )
        boundingBox = MapPanelUtilities.calcZoomInBoundingBox( boundingBox, in );

      if( boundingBox != null )
        getCommandTarget().postCommand( new ChangeExtentCommand( m_mapPanel, boundingBox ), null );
    }

  }

  public void paintWidget( final Graphics g )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      try
      {
        widget.paint( g );
      }
      catch( final Throwable t )
      {
        t.printStackTrace();
      }
    }
  }

  @Override
  public IWidget[] getWidgets( )
  {
    return m_widgets.toArray( new IWidget[] {} );
  }

  @Override
  public void addWidget( final IWidget widget )
  {
    if( Objects.isNull( widget ) )
    {
      doClean();
      fireWidgetChangeEvent( null );

      return;
    }

    if( WIDGET_TYPE.eRadio.equals( widget.getType() ) )
      doClean();

    m_widgets.add( widget );

    widget.activate( m_commandTarget, m_mapPanel );
    widget.setSelection( m_mapPanel.getSelectionManager() );

    fireWidgetChangeEvent( widget );

    if( Objects.isNotNull( m_mapPanel ) )
      m_mapPanel.repaintMap();
  }

  private void doClean( )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      if( WIDGET_TYPE.eRadio.equals( widget.getType() ) )
      {
        widget.finish();
        m_widgets.remove( widget );
      }
    }
  }

  /**
   * Adds a listener to this manager.
   * <p>
   * Has no effect, if the same listener was already registered.
   */
  @Override
  public void addWidgetChangeListener( final IWidgetChangeListener listener )
  {
    m_widgetChangeListener.add( listener );
  }

  /**
   * Removes a listener from this manager.
   * <p>
   * Has no effect, if this listener was not added to this manager before.
   */
  @Override
  public void removeWidgetChangeListener( final IWidgetChangeListener listener )
  {
    m_widgetChangeListener.remove( listener );
  }

  private void fireWidgetChangeEvent( final IWidget newWidget )
  {
    final IWidgetChangeListener[] listener = m_widgetChangeListener.toArray( new IWidgetChangeListener[m_widgetChangeListener.size()] );
    for( final IWidgetChangeListener element : listener )
      element.widgetChanged( newWidget );
  }

  @Override
  public void keyTyped( final KeyEvent e )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      widget.keyTyped( e );
    }
  }

  @Override
  public void keyPressed( final KeyEvent e )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      widget.keyPressed( e );
    }
  }

  @Override
  public void keyReleased( final KeyEvent e )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      widget.keyReleased( e );
    }
  }

  protected void onSelectionChanged( final IFeatureSelection selection )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      widget.setSelection( selection );
    }
  }

  @Override
  public void removeWidget( final IWidget widget )
  {
    m_widgets.remove( widget );

    fireWidgetChangeEvent( null );
  }

}