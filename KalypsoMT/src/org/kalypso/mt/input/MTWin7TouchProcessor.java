/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.util.HashMap;

import org.mt4j.AbstractMTApplication;
import org.mt4j.input.inputData.ActiveCursorPool;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputData.MTWin7TouchInputEvt;
import org.mt4j.input.inputSources.AbstractInputSource;
import org.mt4j.input.inputSources.IWin7NativeTouchSourceProvider.Native_WM_TOUCH_Event;

/**
 * @author cybernixadm
 */
public class MTWin7TouchProcessor extends AbstractInputSource
{
  private AbstractMTApplication app;

  private MTWin7TouchInput parent;

  private Native_WM_TOUCH_Event wmTouchEvent;

  private HashMap<Integer, Long> touchToCursorID;

  private MTMouseInput m_mouse;

  /**
   * Instantiates a new win7 native touch source.
   * 
   * @param mtApp
   *          the mt app
   */
  public MTWin7TouchProcessor( AbstractMTApplication mtApp, MTWin7TouchInput parent, MTMouseInput mouseInput )
  {
    super( mtApp );
    this.app = mtApp;
    this.parent = parent;
    this.m_mouse = mouseInput;

    wmTouchEvent = new Native_WM_TOUCH_Event();
    wmTouchEvent.id = -1;
    wmTouchEvent.type = -1;
    wmTouchEvent.x = -1;
    wmTouchEvent.y = -1;

    touchToCursorID = new HashMap<Integer, Long>();
  }

  @Override
  public void pre( )
  { // we dont have to call registerPre() again (already in superclass and called there)
    if( parent.initialized )
    { // Only poll events if native c++ core was initialized successfully
      while( parent.pollMTEvent( wmTouchEvent ) )
      {
        /*
         * //FIXME TEST, make a artifical TOUCH_DOWN event REMOVE LATER! if (!addedArtificalTouchDown){
         * addedArtificalTouchDown = true; wmTouchEvent.type = Native_WM_TOUCH_Event.TOUCH_DOWN; }
         */

        switch( wmTouchEvent.type )
        {
          case Native_WM_TOUCH_Event.TOUCH_DOWN:
          {
// System.out.println("TOUCH_DOWN ==> ID:" + wmTouchEvent.id + " x:" + wmTouchEvent.x + " y:" + wmTouchEvent.y);

            m_mouse.setMouseBlocked( true );

            InputCursor c = new InputCursor();
            long cursorID = c.getId();
            MTWin7TouchInputEvt touchEvt = new MTWin7TouchInputEvt( this, wmTouchEvent.x, wmTouchEvent.y, wmTouchEvent.contactSizeX, wmTouchEvent.contactSizeY, MTFingerInputEvt.INPUT_STARTED, c );
            int touchID = wmTouchEvent.id;
            ActiveCursorPool.getInstance().putActiveCursor( cursorID, c );
            touchToCursorID.put( touchID, cursorID );
            this.enqueueInputEvent( touchEvt );

            break;
          }
          case Native_WM_TOUCH_Event.TOUCH_MOVE:
          {
// System.out.println("TOUCH_MOVE ==> ID:" + wmTouchEvent.id + " x:" + wmTouchEvent.x + " y:" + wmTouchEvent.y);
// System.out.println("Contact area X:" + wmTouchEvent.contactSizeX + " Y:" + wmTouchEvent.contactSizeY);

            Long cursorID = touchToCursorID.get( wmTouchEvent.id );
            if( cursorID != null )
            {
              InputCursor c = ActiveCursorPool.getInstance().getActiveCursorByID( cursorID );
              if( c != null )
              {
                MTWin7TouchInputEvt te = new MTWin7TouchInputEvt( this, wmTouchEvent.x, wmTouchEvent.y, wmTouchEvent.contactSizeX, wmTouchEvent.contactSizeY, MTFingerInputEvt.INPUT_UPDATED, c );
                this.enqueueInputEvent( te );
              }
            }

            break;
          }
          case Native_WM_TOUCH_Event.TOUCH_UP:
          {
// System.out.println("TOUCH_UP ==> ID:" + wmTouchEvent.id + " x:" + wmTouchEvent.x + " y:" + wmTouchEvent.y);

            m_mouse.setMouseBlocked( false );

            Long cursorID = touchToCursorID.get( wmTouchEvent.id );
            if( cursorID != null )
            {
              InputCursor c = ActiveCursorPool.getInstance().getActiveCursorByID( cursorID );
              if( c != null )
              {
                MTWin7TouchInputEvt te = new MTWin7TouchInputEvt( this, wmTouchEvent.x, wmTouchEvent.y, wmTouchEvent.contactSizeX, wmTouchEvent.contactSizeY, MTFingerInputEvt.INPUT_ENDED, c );
                this.enqueueInputEvent( te );
              }
              ActiveCursorPool.getInstance().removeCursor( cursorID );
              touchToCursorID.remove( wmTouchEvent.id );
            }

            break;
          }
          default:
            break;
        }
      }
    }

    super.pre();
  }
}