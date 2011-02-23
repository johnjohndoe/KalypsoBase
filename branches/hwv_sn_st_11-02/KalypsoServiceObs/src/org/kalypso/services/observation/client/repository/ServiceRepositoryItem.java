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

import java.util.ArrayList;
import java.util.List;

import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.IRepositoryItemVisitor;
import org.kalypso.repository.RepositoryException;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.sei.ItemBean;
import org.kalypso.services.observation.sei.ObservationBean;

/**
 * @author schlienger
 */
public class ServiceRepositoryItem implements IRepositoryItem
{
  private final ItemBean m_bean;

  private final ServiceRepositoryItem m_parent;

  private final IObservationService m_srv;

  private final IRepository m_rep;

  public ServiceRepositoryItem( final IObservationService srv, final ItemBean bean, final ServiceRepositoryItem parent, final IRepository rep )
  {
    m_rep = rep;
    m_srv = srv;
    m_bean = bean;
    m_parent = parent;
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#getName()
   */
  @Override
  public final String getName( )
  {
    return m_bean.getName();
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#getParent()
   */
  @Override
  public final IRepositoryItem getParent( )
  {
    return m_parent;
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#hasChildren()
   */
  @Override
  public final boolean hasChildren( ) throws RepositoryException
  {
    try
    {
      return m_srv.hasChildren( m_bean );
    }
    catch( final RepositoryException e )
    {
      throw new RepositoryException( e );
    }
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#getChildren()
   */
  @Override
  public final IRepositoryItem[] getChildren( ) throws RepositoryException
  {
    try
    {
      final List<IRepositoryItem> items = new ArrayList<IRepositoryItem>();
      final ItemBean[] beans = m_srv.getChildren( m_bean );

      for( final ItemBean bean : beans )
      {
        if( bean.getModifyable() )
        {
          items.add( new ModifyableServiceRepositoryItem( m_srv, bean, this, m_rep ) );
        }
        else
        {
          items.add( new ServiceRepositoryItem( m_srv, bean, this, m_rep ) );
        }
      }

      return items.toArray( new IRepositoryItem[] {} );
    }
    catch( final RepositoryException e )
    {
      throw new RepositoryException( e );
    }
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class anotherClass )
  {
    if( anotherClass == IObservation.class )
    {
      try
      {
        final ObservationBean bean = m_srv.adaptItem( m_bean );

        if( bean == null )
          return null;

        return new ServiceRepositoryObservation( m_srv, bean );
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }
    }

    return null;
  }

  @Override
  public final String toString( )
  {
    return getName();
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#getIdentifier()
   */
  @Override
  public final String getIdentifier( )
  {
    return m_bean.getId();
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#getRepository()
   */
  @Override
  public final IRepository getRepository( )
  {
    return m_rep;
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#hasAdapter(java.lang.Class)
   */
  @Override
  public final boolean hasAdapter( final Class< ? > adapter )
  {
    final Object object = getAdapter( adapter );
    if( object == null )
      return false;

    return true;
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#isMultipleSourceItem()
   */
  @Override
  public boolean isMultipleSourceItem( ) throws RepositoryException
  {
    return m_srv.isMultipleSourceItem( getIdentifier() );
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#accept(org.kalypso.repository.IRepositoryItemVisitor)
   */
  @Override
  public void accept( final IRepositoryItemVisitor visitor ) throws RepositoryException
  {
    final IRepositoryItem[] children = getChildren();
    for( final IRepositoryItem child : children )
    {
      visitor.visit( child );
    }

  }

}