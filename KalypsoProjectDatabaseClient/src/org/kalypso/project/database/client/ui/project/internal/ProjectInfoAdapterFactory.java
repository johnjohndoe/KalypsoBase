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
package org.kalypso.project.database.client.ui.project.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdapterFactory;
import org.kalypso.project.database.client.ui.project.IRemoteProjectInfo;
import org.kalypso.project.database.client.ui.project.LocalRemoteProjectInfo;

/**
 * @author Gernot Belger
 */
public class ProjectInfoAdapterFactory implements IAdapterFactory
{
  private static final Class< ? >[] ADAPTER = new Class[] { IRemoteProjectInfo.class };

  /**
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
   */
  @Override
  public Object getAdapter( final Object adaptableObject, @SuppressWarnings("rawtypes") final Class adapterType )
  {
    if( !(adaptableObject instanceof IProject) )
      return null;

// final IProject project = (IProject) adaptableObject;
    // FIXME: implement remote stuff
    if( adapterType == IRemoteProjectInfo.class )
      return new LocalRemoteProjectInfo();

    return null;
  }

  /**
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
   */
  @Override
  public Class< ? >[] getAdapterList( )
  {
    return ADAPTER;
  }

}