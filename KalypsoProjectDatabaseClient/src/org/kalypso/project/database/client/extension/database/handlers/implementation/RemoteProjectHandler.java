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
package org.kalypso.project.database.client.extension.database.handlers.implementation;

import org.kalypso.project.database.client.extension.database.handlers.IRemoteProject;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * @author Dirk Kuch
 */
public class RemoteProjectHandler extends AbstractProjectHandler implements IRemoteProject
{

  private final KalypsoProjectBean m_bean;

  public RemoteProjectHandler( final KalypsoProjectBean bean )
  {
    m_bean = bean;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IProjectHandler#getName()
   */
  @Override
  public String getName( )
  {
    return m_bean.getName();
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IProjectHandler#getUniqueName()
   */
  @Override
  public String getUniqueName( )
  {
    return m_bean.getUnixName();
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IProjectHandler#isLocal()
   */
  @Override
  public boolean isLocal( )
  {
    return false;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IProjectHandler#isRemote()
   */
  @Override
  public boolean isRemote( )
  {
    return true;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.refactoring.handlers.IRemoteProjectHandler#getBean()
   */
  @Override
  public KalypsoProjectBean getBean( )
  {
    return m_bean;
  }

  /**
   * @see org.kalypso.project.database.client.extension.database.handlers.IProjectHandler#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return m_bean.getDescription();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return String.format( "Remote Project: %s", getName() );
  }

}