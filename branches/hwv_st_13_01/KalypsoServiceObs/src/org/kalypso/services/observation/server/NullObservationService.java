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
package org.kalypso.services.observation.server;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.services.observation.KalypsoServiceObs;
import org.kalypso.services.observation.sei.DataBean;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.sei.ItemBean;
import org.kalypso.services.observation.sei.ObservationBean;
import org.kalypso.services.observation.sei.StatusBean;

/**
 * @author Holger Albert
 */
public class NullObservationService implements IObservationService
{
  public NullObservationService( )
  {
  }

  @Override
  public StatusBean getStatus( final String type )
  {
    return new StatusBean( IStatus.INFO, KalypsoServiceObs.ID, "Nicht initialisiert." );
  }

  @Override
  public boolean hasChildren( final ItemBean parent )
  {
    return false;
  }

  @Override
  public ItemBean[] getChildren( final ItemBean parent )
  {
    return new ItemBean[] {};
  }

  @Override
  public ItemBean findItem( final String id )
  {
    return null;
  }

  @Override
  public ItemBean getParent( final String identifier )
  {
    return null;
  }

  @Override
  public void makeItem( final String identifier )
  {
  }

  @Override
  public void deleteItem( final String identifier )
  {
  }

  @Override
  public void setItemData( final String identifier, final Object serializable )
  {
  }

  @Override
  public void setItemName( final String identifier, final String name )
  {
  }

  @Override
  public void reload( )
  {
  }

  @Override
  public boolean isMultipleSourceItem( final String identifier )
  {
    return false;
  }

  @Override
  public int getServiceVersion( )
  {
    return 0;
  }

  @Override
  public ObservationBean adaptItem( final ItemBean ib )
  {
    return null;
  }

  @Override
  public DataBean readData( final String href )
  {
    return null;
  }

  @Override
  public void clearTempData( final String dataId )
  {
  }
}