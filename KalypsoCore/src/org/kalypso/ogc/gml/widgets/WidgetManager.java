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
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ogc.gml.selection.IFeatureSelectionListener;
import org.kalypso.ogc.gml.widgets.IWidget.WIDGET_TYPE;
import org.kalypso.ogc.gml.widgets.base.BackgroundPanToWidget;
import org.kalypso.ogc.gml.widgets.base.MouseWheelZoomWidget;

/**
 * widget controller of map view
 * 
 * @author vdoemming
 * @author Dirk Kuch
 */
public class WidgetManager implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, IWidgetManager
{
  private final Set<IWidgetChangeListener> m_widgetChangeListener = new LinkedHashSet<>();

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

  // handle widgets as tree set because radio widgets should be processed first
  private final Set<IWidget> m_widgets = Collections.synchronizedSet( new TreeSet<>( new Comparator<IWidget>()
  {
    @Override
    public int compare( final IWidget w1, final IWidget w2 )
    {
      // FIXME: does not work: same widget may be in toolbar twice (with different parameters)
      final String n1 = w1.getClass().getName();
      final String n2 = w2.getClass().getName();

      if( WIDGET_TYPE.eRadio.equals( w1 ) && WIDGET_TYPE.eRadio.equals( w2 ) )
      {
        return n1.compareTo( n2 );
      }
      else if( WIDGET_TYPE.eRadio.equals( w1 ) )
      {
        return 1;
      }
      else if( WIDGET_TYPE.eRadio.equals( w2 ) )
      {
        return -1;
      }

      return n1.compareTo( n2 );
    }

  } ) );

  public WidgetManager( final ICommandTarget commandTarget, final IMapPanel mapPanel )
  {
    m_mapPanel = mapPanel;
    m_commandTarget = commandTarget;

    m_mapPanel.getSelectionManager().addSelectionListener( m_featureSelectionListener );

    addWidget( new BackgroundPanToWidget() );
    addWidget( new MouseWheelZoomWidget() );
  }

  @Override
  public void dispose( )
  {
    final IWidget[] widgets = getWidgets();
    m_widgets.clear();

    for( final IWidget widget : widgets )
    {
      widget.finish();
    }

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
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      widget.mouseClicked( e );

      if( e.isConsumed() )
        return;
    }
  }

  @Override
  public void mouseMoved( final MouseEvent e )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      widget.mouseMoved( e );

      if( e.isConsumed() )
        return;
    }

    m_mapPanel.fireMouseMouveEvent( e.getX(), e.getY() );
  }

  // MouseMotionAdapter:
  @Override
  public void mouseDragged( final MouseEvent e )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      widget.mouseDragged( e );

      if( e.isConsumed() )
        return;
    }
  }

  @Override
  public void mouseEntered( final MouseEvent e )
  {
    final IWidget[] widgets = getWidgets();

    for( final IWidget widget : widgets )
    {
      // so, if the mouse enter event happens already somewhere inside the map panel frame - it's actually a finger tap
      // event
      // if( isInsideMapFrame( e.getPoint() ) )
      // {
      // FIXME: leftPressed does not exist any more
      // if( widget != null )
      // widget.leftPressed( e.getPoint() );
      // }
      // else
      widget.mouseEntered( e );

      if( e.isConsumed() )
        return;
    }
  }

  @Override
  public void mouseExited( final MouseEvent e )
  {
    final IWidget[] widgets = getWidgets();

    for( final IWidget widget : widgets )
    {
      // so, if the mouse enter event happens already somewhere inside the map panel frame - it's actually a finger tap
      // event
      // if( isInsideMapFrame( e.getPoint() ) )
      // {
      // FIXME: leftReleased does not exist any more
      // if( widget != null )
      // widget.leftReleased( e.getPoint() );
      // }
      // else
      widget.mouseExited( e );

      if( e.isConsumed() )
        return;
    }
  }

  @Override
  public void mousePressed( final MouseEvent e )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      widget.mousePressed( e );

      if( e.isConsumed() )
        return;
    }
  }

  @Override
  public void mouseReleased( final MouseEvent e )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      widget.mouseReleased( e );

      if( e.isConsumed() )
        return;
    }
  }

  @Override
  public void mouseWheelMoved( final MouseWheelEvent e )
  {
    final IWidget[] widgets = getWidgets();
    for( final IWidget widget : widgets )
    {
      widget.mouseWheelMoved( e );

      if( e.isConsumed() )
        return;
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
        removeWidget( widget );
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
    final String clazz = widget.getClass().getName();

    final IWidget[] widgets = getWidgets();
    for( final IWidget w : widgets )
    {
      final String c = w.getClass().getName();
      if( StringUtils.equals( clazz, c ) )
      {
        m_widgets.remove( w );

        w.finish();
      }
    }

    fireWidgetChangeEvent( null );
  }

  protected boolean isInsideMapFrame( final Point p )
  {
    // FIXME: at least comment: where does this THRESHOLD come from?
    final int THRESHOLD = 35; // pixel

    if( p.getX() < THRESHOLD || p.getX() > m_mapPanel.getWidth() - THRESHOLD || p.getY() < THRESHOLD || p.getY() > m_mapPanel.getHeight() - THRESHOLD )
      return false;

    return true;
  }

  public void paintWidget( final Graphics g )
  {
    final IWidget[] widgets = getWidgets();
    ArrayUtils.reverse( widgets ); // paint background and toggle widgets first

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
}