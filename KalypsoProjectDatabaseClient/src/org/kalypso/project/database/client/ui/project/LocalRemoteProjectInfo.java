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
package org.kalypso.project.database.client.ui.project;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.client.i18n.Messages;

/**
 * {@link IRemoteProjectInfo} for a project that is not attached to the database.
 * 
 * @author Gernot Belger
 */
public class LocalRemoteProjectInfo implements IRemoteProjectInfo
{
  /**
   * @see org.kalypso.project.database.client.ui.project.IRemoteProjectInfo#isEditable()
   */
  @Override
  public boolean isEditable( )
  {
    return true;
  }

  /**
   * @see org.kalypso.project.database.client.ui.project.IRemoteProjectInfo#isLocked()
   */
  @Override
  public boolean isLocked( )
  {
    return false;
  }

  /**
   * @see org.kalypso.project.database.client.ui.project.IRemoteProjectInfo#releaseProjectLock()
   */
  @Override
  public IStatus releaseProjectLock( )
  {
    return new Status( IStatus.WARNING, KalypsoProjectDatabaseClient.PLUGIN_ID, Messages.getString("LocalRemoteProjectInfo_0") ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.project.database.client.ui.project.IRemoteProjectInfo#acquireProjectLock()
   */
  @Override
  public IStatus acquireProjectLock( )
  {
    return new Status( IStatus.WARNING, KalypsoProjectDatabaseClient.PLUGIN_ID, Messages.getString("LocalRemoteProjectInfo_1") ); //$NON-NLS-1$
  }
}
