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
package org.kalypso.services.observation.client.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.kalypso.repository.AbstractRepository;
import org.kalypso.repository.IModifyableRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.IWriteableRepository;
import org.kalypso.repository.RepositoryException;
import org.kalypso.services.observation.KalypsoServiceObsActivator;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.sei.ItemBean;

/**
 * Repository of the Observation Service.
 * 
 * @author Schlienger
 */
public class ObservationServiceRepository extends AbstractRepository implements IModifyableRepository, IWriteableRepository
{
  /** URL-Scheme that identifies the observation service */
  public static String ID = "kalypso-ocs"; //$NON-NLS-1$

  private static String ID_COLON = ID + ":"; //$NON-NLS-1$

  /** root item is identified by the null bean */
  private static final ItemBean ROOT_ITEM = null;

  /**
   * @throws ServiceException
   *           when the underlying service is not available
   */
  public ObservationServiceRepository( final String name, final String label, final String factory, final boolean readOnly, final boolean cached ) throws RepositoryException
  {
    super( name, label, factory, "", readOnly, cached, ID ); //$NON-NLS-1$

    if( getService() == null )
      throw new RepositoryException( "Could not find Repository Service" ); //$NON-NLS-1$
  }

  private String getServiceId( final String id ) throws RepositoryException
  {
    if( !id.startsWith( ID_COLON ) )
      throw new RepositoryException( String.format( "Unknown repository item id '%s'", id ) );

    return id.substring( ID_COLON.length() );
  }

  private IObservationService getService( )
  {
    final IObservationService srv = KalypsoServiceObsActivator.getDefault().getObservationService( getName() );
    if( srv != null )
      return srv;

    return KalypsoServiceObsActivator.getDefault().getDefaultObservationService();
  }

  @Override
  public final String getDescription( )
  {
    return ""; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#hasChildren()
   */
  public final boolean hasChildren( ) throws RepositoryException
  {
    try
    {
      return getService().hasChildren( ROOT_ITEM );
    }
    catch( final RepositoryException e )
    {
      throw new RepositoryException( e );
    }
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#getChildren()
   */
  public final IRepositoryItem[] getChildren( ) throws RepositoryException
  {
    try
    {
      final IObservationService service = getService();
      final List<IRepositoryItem> items = new ArrayList<IRepositoryItem>();

      final ItemBean[] beans = service.getChildren( ROOT_ITEM );
      for( final ItemBean bean : beans )
        items.add( beanToItem( service, bean ) );

      /** @hack single repository? skip one hierarchy level and return children of repository item */
      if( items.size() == 1 )
        return items.get( 0 ).getChildren();

      return items.toArray( new IRepositoryItem[] {} );
    }
    catch( final RepositoryException e )
    {
      throw new RepositoryException( e );
    }
  }

  private IRepositoryItem beanToItem( final IObservationService service, final ItemBean bean )
  {
    if( bean.getModifyable() )
      return new ModifyableServiceRepositoryItem( service, bean, null, this );

    return new ServiceRepositoryItem( service, bean, null, this );
  }

  /**
   * @see org.kalypso.repository.IRepository#reload()
   */
  public final void reload( ) throws RepositoryException
  {
    try
    {
      getService().reload();
    }
    catch( final RepositoryException e )
    {
      throw new RepositoryException( e );
    }
  }

  /**
   * @see org.kalypso.repository.IRepository#findItem(java.lang.String)
   */
  public final IRepositoryItem findItem( final String identifier ) throws RepositoryException
  {
    final ItemBean bean = getService().findItem( getServiceId( identifier ) );
    if( bean == null )
      return null;

    return beanToItem( getService(), bean );
  }

  /**
   * @see org.kalypso.repository.IModifyableRepository#createItem(java.lang.String)
   */
  @Override
  public final void makeItem( final String identifier ) throws RepositoryException
  {
    getService().makeItem( getServiceId( identifier ) );
  }

  /**
   * @see org.kalypso.repository.IModifyableRepository#createItem(java.lang.String)
   */
  @Override
  public final void deleteItem( final String identifier ) throws RepositoryException
  {
    getService().deleteItem( getServiceId( identifier ) );
  }

  /**
   * @see org.kalypso.repository.IModifyableRepositoryItem#setData(java.lang.Object)
   */
  @Override
  public final void setData( final Serializable observation )
  {
    throw new IllegalStateException( "This should never happen" );
  }

  /**
   * @see org.kalypso.repository.IModifyableRepositoryItem#setName(java.lang.String)
   */
  @Override
  public final void setName( final String itemName )
  {
    throw new IllegalStateException( "This should never happen" );
  }

  /**
   * @see org.kalypso.repository.IWriteableRepository#setData(java.lang.String, java.io.Serializable)
   */
  @Override
  public void setData( final String identifier, final Serializable data ) throws RepositoryException
  {
    getService().setItemData( getServiceId( identifier ), data );
  }

}