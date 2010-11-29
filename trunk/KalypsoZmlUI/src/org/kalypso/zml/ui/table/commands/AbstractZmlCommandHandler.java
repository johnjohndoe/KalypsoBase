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
package org.kalypso.zml.ui.table.commands;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractZmlCommandHandler implements IHandler
{
  Set<IHandlerListener> m_listeners = new LinkedHashSet<IHandlerListener>();

  /**
   * @see org.eclipse.core.commands.IHandler#addHandlerListener(org.eclipse.core.commands.IHandlerListener)
   */
  @Override
  public void addHandlerListener( final IHandlerListener handlerListener )
  {
    m_listeners.add( handlerListener );
  }

  /**
   * @see org.eclipse.core.commands.IHandler#dispose()
   */
  @Override
  public void dispose( )
  {
    m_listeners.clear();
  }

  /**
   * @see org.eclipse.core.commands.IHandler#isEnabled()
   */
  @Override
  public boolean isEnabled( )
  {
    return true;
  }

  /**
   * @see org.eclipse.core.commands.IHandler#isHandled()
   */
  @Override
  public boolean isHandled( )
  {
    return true;
  }

  /**
   * @see org.eclipse.core.commands.IHandler#removeHandlerListener(org.eclipse.core.commands.IHandlerListener)
   */
  @Override
  public void removeHandlerListener( final IHandlerListener handlerListener )
  {
    m_listeners.remove( handlerListener );
  }

}
