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
package org.kalypso.contribs.eclipse.core.runtime;

import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * Implementors of this interface colllect {@link org.eclipse.core.runtime.IStatus} objects. This is typically used in
 * an operation that generate multiple stati when run.
 * 
 * @author Gernot Belger
 */
public interface IStatusCollector extends Collection<IStatus>
{
  /**
   * Convienience method that adds an error status to this collection.
   */
  void add( int severity, String msg );

  /**
   * Convienience method that adds an error status to this collection, using {@link String#format(String, Object...)} to
   * format the message.
   */
  void add( int severity, String msgFormat, Throwable exception, Object... formatParameters );

  /**
   * Convienience method that adds an error status to this collection.
   */
  void add( int severity, String msg, Throwable exception );

  /**
   * Returns all collected stati.
   */
  IStatus[] getAllStati( );

  /**
   * Returns the currently collected stati as one {@link MultiStatus}.
   */
  MultiStatus asMultiStatus( String msg );

  /**
   * Similar to {@link #asMultiStatus(String)}, but simply returns {@link org.eclipse.core.runtime.Status#OK_STATUS} if
   * this collector is empty or contains only Ok-stati.
   */
  IStatus asMultiStatusOrOK( String msg );
}
