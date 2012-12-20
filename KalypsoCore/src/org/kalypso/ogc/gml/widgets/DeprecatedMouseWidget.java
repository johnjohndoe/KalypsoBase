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
package org.kalypso.ogc.gml.widgets;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * @author Dirk Kuch
 * @deprecated Use AbstractWidget instead.
 */
@Deprecated
public abstract class DeprecatedMouseWidget extends AbstractWidget implements IDeprecatedMouseWidget
{
  public DeprecatedMouseWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
  }

  @Override
  public void mouseClicked( final MouseEvent e )
  {
    if( e.isPopupTrigger() )
      clickPopup( e.getPoint() );
    else
    {
      switch( e.getButton() )
      {
        case MouseEvent.BUTTON1:
          if( e.getClickCount() == 1 )
            leftClicked( e.getPoint() );
          else if( e.getClickCount() == 2 )
            doubleClickedLeft( e.getPoint() );
          break;

        case MouseEvent.BUTTON2:
          leftClicked( e.getPoint() );
          break;

        case MouseEvent.BUTTON3:
          if( e.getClickCount() == 1 )
            rightClicked( e.getPoint() );
          else if( e.getClickCount() == 2 )
            doubleClickedRight( e.getPoint() );
          break;
      }
    }

  }

  @Override
  public void mousePressed( final MouseEvent e )
  {
    if( e.isPopupTrigger() )
      clickPopup( e.getPoint() );
    else
    {
      switch( e.getButton() )
      {
        case MouseEvent.BUTTON1:
          leftPressed( e.getPoint() );
          break;

        case MouseEvent.BUTTON3:
          rightPressed( e.getPoint() );
          break;

        default:
          break;
      }
    }
  }

  @Override
  public void mouseReleased( final MouseEvent e )
  {
    if( e.isPopupTrigger() )
      clickPopup( e.getPoint() );
    else
    {
      switch( e.getButton() )
      {
        case MouseEvent.BUTTON1:
          leftReleased( e.getPoint() );
          break;

        case MouseEvent.BUTTON3:
          rightReleased( e.getPoint() );
          break;

        default:
          break;
      }
    }
  }

  @Override
  public void mouseEntered( final MouseEvent e )
  {
    // not implemented by default
  }

  @Override
  public void mouseExited( final MouseEvent e )
  {
    // not implemented by default
  }

  @Override
  public void mouseDragged( final MouseEvent e )
  {
    dragged( e.getPoint() );
  }

  @Override
  public void mouseMoved( final MouseEvent e )
  {
    moved( e.getPoint() );
  }

  @Override
  public void mouseWheelMoved( final MouseWheelEvent e )
  {
    // not implemented by default
  }

  @Override
  @Deprecated
  public void clickPopup( final Point p )
  {
    // not implemented by default
  }

  @Override
  public void dragged( final Point p )
  {
    // not implemented by default
  }

  @Override
  public void leftClicked( final Point p )
  {
    // not implemented by default
  }

  @Override
  public void leftPressed( final Point p )
  {
    // not implemented by default
  }

  @Override
  public void leftReleased( final Point p )
  {
    // not implemented by default
  }

  @Override
  public void moved( final Point p )
  {
    // not implemented by default
  }

  @Override
  public void rightClicked( final Point p )
  {
    // not implemented by default
  }

  @Override
  public void rightPressed( final Point p )
  {
    // not implemented by default
  }

  @Override
  public void rightReleased( final Point p )
  {
    // not implemented by default
  }

  @Override
  public void doubleClickedLeft( final Point p )
  {
    // not implemented by default
  }

  @Override
  public void doubleClickedRight( final Point p )
  {
    // not implemented by default

  }
}
