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
package org.kalypso.contribs.eclipse.ui.browser.commandable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author kuepfer
 */
public abstract class AbstractCommandURLAction implements ICommandURLAction
{
  final HashSet<Listener> m_listener = new HashSet<Listener>();

  /**
   * @see org.kalypso.contribs.eclipse.ui.browser.commandable.ICommandURLAction#run(java.util.Properties)
   */
  public void run( Properties keyValuePair )
  {
    // to be implemented from super class
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.browser.commandable.ICommandURLAction#addListener(org.eclipse.swt.widgets.Listener)
   */
  public void addListener( Listener listener )
  {
    m_listener.add( listener );
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.browser.commandable.ICommandURLAction#removeListener(org.eclipse.swt.widgets.Listener)
   */
  public void removeListener( Listener listner )
  {
    m_listener.remove( listner );
  }

  protected void fireEvent( final int type, final Object data )
  {
    Event event = new Event();
    event.type = type;
    event.data = data;
    Iterator it = m_listener.iterator();
    while( it.hasNext() )
    {
      Listener listener = (Listener) it.next();
      listener.handleEvent( event );
    }

  }
}
