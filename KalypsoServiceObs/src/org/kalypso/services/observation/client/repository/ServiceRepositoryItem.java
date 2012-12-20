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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.IRepositoryItemVisitor;
import org.kalypso.repository.RepositoryException;
import org.kalypso.repository.utils.RepositoryItems;
import org.kalypso.repository.utils.RepositoryVisitors;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.sei.ItemBean;
import org.kalypso.services.observation.sei.ObservationBean;

/**
 * @author schlienger
 */
public class ServiceRepositoryItem implements IRepositoryItem
{
  private final ItemBean m_bean;

  private IRepositoryItem m_parent;

  private final ObservationServiceRepository m_rep;

  public ServiceRepositoryItem( final ItemBean bean, final IRepositoryItem parent, final ObservationServiceRepository rep )
  {
    m_rep = rep;
    m_bean = bean;
    m_parent = parent;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof IRepositoryItem )
      return RepositoryItems.equals( this, (IRepositoryItem) obj );

    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( RepositoryItems.getPlainId( getIdentifier() ) );

    return builder.toHashCode();
  }

  @Override
  public final String getName( )
  {
    return m_bean.getName();
  }

  @Override
  public final IRepositoryItem getParent( )
  {
    try
    {
      if( Objects.isNotNull( m_parent ) )
        return m_parent;

      final String identifier = getIdentifier();
      final IRepositoryItem parent = m_rep.getParent( identifier );

      m_parent = parent;

      return parent;
    }
    catch( final RepositoryException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public final boolean hasChildren( ) throws RepositoryException
  {
    try
    {
      return m_rep.getService().hasChildren( m_bean );
    }
    catch( final RepositoryException e )
    {
      throw new RepositoryException( e );
    }
  }

  @Override
  public final IRepositoryItem[] getChildren( ) throws RepositoryException
  {
    try
    {
      final IObservationService service = m_rep.getService();

      final List<IRepositoryItem> items = new ArrayList<>();
      final ItemBean[] beans = service.getChildren( m_bean );

      for( final ItemBean bean : beans )
      {
        if( bean.getModifyable() )
        {
          items.add( new ModifyableServiceRepositoryItem( bean, this, m_rep ) );
        }
        else
        {
          items.add( new ServiceRepositoryItem( bean, this, m_rep ) );
        }
      }

      return items.toArray( new IRepositoryItem[] {} );
    }
    catch( final RepositoryException e )
    {
      throw new RepositoryException( e );
    }
  }

  @Override
  public Object getAdapter( final Class anotherClass )
  {
    if( anotherClass == IObservation.class )
    {
      try
      {
        final IObservationService service = m_rep.getService();
        final ObservationBean bean = service.adaptItem( m_bean );

        if( bean == null )
          return null;

        return new ServiceRepositoryObservation( service, bean );
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

  @Override
  public boolean isMultipleSourceItem( ) throws RepositoryException
  {
    return m_rep.getService().isMultipleSourceItem( getIdentifier() );
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#accept(org.kalypso.repository.IRepositoryItemVisitor)
   */
  @Override
  public void accept( final IRepositoryItemVisitor visitor ) throws RepositoryException
  {
    RepositoryVisitors.accept( this, visitor );
  }
}