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
package org.kalypso.services.observation.client.repository;

import java.io.Serializable;

import org.kalypso.repository.IModifyableRepositoryItem;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.RepositoryException;
import org.kalypso.services.observation.KalypsoServiceObsActivator;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.sei.ItemBean;

/**
 * @author Dirk Kuch
 */
public class ModifyableServiceRepositoryItem extends ServiceRepositoryItem implements IModifyableRepositoryItem
{

  public ModifyableServiceRepositoryItem( final IObservationService srv, final ItemBean bean, final ServiceRepositoryItem parent, final IRepository rep )
  {
    super( srv, bean, parent, rep );
  }

  /**
   * @see org.kalypso.repository.IModifyableRepositoryItem#setData(java.lang.Object)
   */
  @Override
  public void setData( final Serializable data ) throws RepositoryException
  {
    final IObservationService srv = KalypsoServiceObsActivator.getDefault().getObservationServiceProxy();
    srv.setItemData( getIdentifier(), data );
  }

  /**
   * @see org.kalypso.repository.IModifyableRepositoryItem#setName(java.lang.String)
   */
  @Override
  public void setName( final String name ) throws RepositoryException
  {
    final IObservationService srv = KalypsoServiceObsActivator.getDefault().getObservationServiceProxy();
    srv.setItemName( getIdentifier(), name );
  }

}
