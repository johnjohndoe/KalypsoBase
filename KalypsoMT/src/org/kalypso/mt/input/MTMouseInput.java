/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.mt.input;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.kalypso.ogc.gml.widgets.WidgetManager;
import org.mt4j.AbstractMTApplication;
import org.mt4j.input.inputSources.AbstractInputSource;
import org.mt4j.sceneManagement.IPreDrawAction;
import org.mt4j.util.ArrayDeque;
import org.mt4j.util.math.Vector3D;

/**
 * @author cybernixadm
 */
public class MTMouseInput extends AbstractInputSource implements MouseMotionListener, MouseListener, IPreDrawAction
{
  private class MTMouseEventData
  {
    public MTMouseEventData( )
    {
    }

    public MouseEvent event;

    public long timestamp;
  }

  private static final double MT_FAKE_MOUSE_CLICK_DISTANCE = 20;

  private ArrayDeque<MTMouseEventData> eventQueue = new ArrayDeque<MTMouseInput.MTMouseEventData>();

  private WidgetManager m_mgr;

  int m_mouseBlocks = 0;

  int invocCounter = 0;

  private AbstractMTApplication mtApp;

  boolean m_fakeMousePressed = false;

  Point m_fakeMousePressPos = null;

  private boolean m_fakeDrag;

  // make singleton
  /**
   * Instantiates a new mouse input source.
   * 
   * @param pa
   *          the pa
   */
  public MTMouseInput( AbstractMTApplication pa, WidgetManager mgr )
  {
    super( pa );
    mtApp = pa;

    m_mgr = mgr;
    pa.registerMouseEvent( this );
    // when touch input is in progress, block win7 mouse events
    setMouseBlocked( false );

    pa.registerPreDrawAction( this );
  }

  /**
   * Mouse event.
   * 
   * @param event
   *          the event
   */
  public void mouseEvent( MouseEvent event )
  {
// /*
    if( isMouseBlocked() )
    {
      return;
    }

    MTMouseEventData qEv = new MTMouseEventData();
    qEv.event = event;
    qEv.timestamp = System.currentTimeMillis();

    eventQueue.addLast( qEv );
  }

  @Override
  public void processAction( )
  {
    if( invocCounter++ % 6 == 0 || eventQueue.isEmpty() )
      return;

    // purge all events waiting if mouse is blocked
    if( m_mouseBlocks > 0 )
    {
      eventQueue.clear();
      return;
    }

    // otherwise process events
    long time = System.currentTimeMillis();
    while( !eventQueue.isEmpty() )
    {
      long dist = time - eventQueue.peekFirst().timestamp;

      if( dist > 200 )
      {
        // ok, process this one
        MTMouseEventData e = eventQueue.pollFirst();
        switch( e.event.getID() )
        {
          case MouseEvent.MOUSE_PRESSED:
            this.mousePressed( e.event );
            break;
          case MouseEvent.MOUSE_RELEASED:
            this.mouseReleased( e.event );
            break;
          case MouseEvent.MOUSE_CLICKED:
            this.mouseClicked( e.event );
            break;
          case MouseEvent.MOUSE_DRAGGED:
            this.mouseDragged( e.event );
            break;
          case MouseEvent.MOUSE_MOVED:
            this.mouseMoved( e.event );
            break;
        }

      }
      else
      {
        break;
      }

    }

  }

// experimental !
  /**
   * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseDragged( MouseEvent e )
  {
    m_mgr.mouseDragged( e );
  }

  /**
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved( MouseEvent e )
  {
    m_mgr.mouseMoved( e );
  }

  /**
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked( MouseEvent e )
  {
    m_mgr.mouseClicked( e );
  }

  /**
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered( MouseEvent e )
  {
    m_mgr.mouseEntered( e );
  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited( MouseEvent e )
  {
    m_mgr.mouseExited( e );
  }

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed( MouseEvent e )
  {
    m_mgr.mousePressed( e );
  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased( MouseEvent e )
  {
    m_mgr.mouseReleased( e );
  }

  public void setMouseBlocked( boolean blockMouse )
  {
    if( blockMouse )
      m_mouseBlocks++;
    else
      m_mouseBlocks--;

    // impossible though
    if( m_mouseBlocks < 0 )
      m_mouseBlocks = 0;
  }

  public boolean isMouseBlocked( )
  {
    if( m_mouseBlocks > 0 )
      return true;
    return false;
  }

  public void fakeMousePress( Vector3D position )
  {
    m_fakeMousePressed = true;

    if( m_mgr.getActualWidget() != null )
    {
      Point p = new Point( (int) position.x, (int) position.y );
      m_fakeMousePressPos = p;
      m_mgr.getActualWidget().leftPressed( p );
    }
    else
    {
//      System.out.println( "No widget selected" );
    }
  }

  public void fakeMouseMove( Vector3D position )
  {
    if( m_mgr.getActualWidget() != null )
    {
      if (m_fakeDrag)
          m_mgr.getActualWidget().dragged( new Point( (int) position.x, (int) position.y ) );
        else 
          m_mgr.getActualWidget().moved( new Point( (int) position.x, (int) position.y ) );
    }
  }

  public void fakeMouseRelease( Vector3D position )
  {
    setFakeDrag( false );
    
    if( !m_fakeMousePressed )
      return;

    if( m_mgr.getActualWidget() != null )
    {
      Point p = new Point( (int) position.x, (int) position.y );
      m_mgr.getActualWidget().leftReleased( p );

      if ( m_fakeMousePressPos != null ) {
        double dist = m_fakeMousePressPos.distance( p );
        if( dist < MT_FAKE_MOUSE_CLICK_DISTANCE )
        {
          m_mgr.getActualWidget().leftClicked( p );
        }
      }
    }
  }

  public void fakeMouseCancel( )
  {
    m_fakeMousePressed = false;

    if( m_mgr.getActualWidget() != null )
    {
      try
      {
        m_mgr.getActualWidget().leftPressed( null );
        m_mgr.getActualWidget().leftReleased( null );

      }
      catch( Exception e )
      {
//        System.out.println( "WIDGET CANCELATION FAILED " + m_mgr.getActualWidget() );
      }
    }
  }

  public void fakeKeyReleased( KeyEvent keyEvent )
  {
    if( m_mgr.getActualWidget() != null )
    {
      try
      {
        m_mgr.getActualWidget().keyReleased( keyEvent );
        m_mgr.getActualWidget().keyPressed( keyEvent );
      }
      catch( Exception e )
      {
//        System.out.println( "WIDGET FAKE KEY PRESS FAILED " + m_mgr.getActualWidget() );
      }
    }
    else
    {
    }

  }

  public void setFakeDrag( boolean b )
  {
    m_fakeDrag = b;
  }

}
