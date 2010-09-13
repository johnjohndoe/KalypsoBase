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
package org.kalypso.loader;

import java.net.MalformedURLException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.core.util.pool.IPoolableObjectType;

/**
 * ILoader is intended to be subclassed by clients who wish to integrate a loading solution for specific file types into
 * the ResourcePool mechanism.
 * 
 * @author schlienger
 */
public interface ILoader
{
  /** Return a description for this loader */
  public String getDescription( );

  /**
   * Load an object from somewhere
   * 
   * @param monitor
   *            monitors the progress of loading
   * @return object
   * @throws LoaderException
   */
  public Object load( final IPoolableObjectType key, final IProgressMonitor monitor ) throws LoaderException;

  /**
   * Save an object to the given location
   */
  public void save( final IPoolableObjectType key, final IProgressMonitor monitor, final Object data ) throws LoaderException;

  /**
   * Release resources or whatsoever is associated to the given object
   */
  public void release( final Object object );

  public IStatus getStatus( );

  /** Returns the resources this key describes in respect to this loader. */
  public IResource[] getResources( final IPoolableObjectType key ) throws MalformedURLException;
}
