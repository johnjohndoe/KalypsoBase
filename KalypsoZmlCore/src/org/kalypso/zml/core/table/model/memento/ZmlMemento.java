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
package org.kalypso.zml.core.table.model.memento;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;

/**
 * @author Dirk Kuch
 */
public class ZmlMemento implements IZmlMemento
{
  private final Map<IPoolableObjectType, List<ILabeledObsProvider>> m_provider = new LinkedHashMap<IPoolableObjectType, List<ILabeledObsProvider>>();

  Set<IZmlMementoListener> m_listener = Collections.synchronizedSet( new LinkedHashSet<IZmlMementoListener>() );

  private final IObsProviderListener m_obsListener = new IObsProviderListener()
  {
    @Override
    public void observationReplaced( )
    {
      handleObservationChanged();
    }

    @Override
    public void observationChanged( final Object source )
    {
      handleObservationChanged();
    }
  };

  @Override
  public void dispose( )
  {
    doCleanup();
  }

  @Override
  public synchronized void register( final IPoolableObjectType poolKey, final ILabeledObsProvider provider )
  {
    List<ILabeledObsProvider> providers = m_provider.get( poolKey );
    if( Objects.isNull( providers ) )
    {
      providers = new ArrayList<ILabeledObsProvider>();
      m_provider.put( poolKey, providers );
    }

    providers.add( provider );
    provider.addListener( m_obsListener );
  }

  @Override
  public synchronized void store( ) throws CoreException
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    final IObsProvider[] dirtyElements = findDirtyElements();
    for( final IObsProvider provider : dirtyElements )
    {
      final IObservation observation = provider.getObservation();
      if( observation != null )
        pool.saveObject( observation, new NullProgressMonitor() );
    }

    doCleanup();
  }

  private synchronized void doCleanup( )
  {
    synchronized( this )
    {

      final Set<Entry<IPoolableObjectType, List<ILabeledObsProvider>>> entries = m_provider.entrySet();
      for( final Entry<IPoolableObjectType, List<ILabeledObsProvider>> entry : entries )
      {
        final List<ILabeledObsProvider> providers = entry.getValue();
        for( final ILabeledObsProvider provider : providers )
        {
          provider.removeListener( m_obsListener );
          provider.dispose();
        }
      }

      m_provider.clear();

    }
  }

  @Override
  public synchronized ILabeledObsProvider[] findDirtyElements( )
  {
    final Collection<ILabeledObsProvider> result = new ArrayList<ILabeledObsProvider>();
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    final Set<Entry<IPoolableObjectType, List<ILabeledObsProvider>>> entries = m_provider.entrySet();
    for( final Entry<IPoolableObjectType, List<ILabeledObsProvider>> entry : entries )
    {
      final List<ILabeledObsProvider> providers = entry.getValue();
      for( final ILabeledObsProvider provider : providers )
      {
        final IObservation observation = provider.getObservation();
        if( observation != null )
        {
          final KeyInfo info = pool.getInfo( observation );
          if( Objects.isNotNull( info ) )
            if( info.isDirty() )
            {
              result.add( provider );
              break;
            }
        }
      }

    }

    return result.toArray( new ILabeledObsProvider[result.size()] );
  }

  protected void handleObservationChanged( )
  {
    fireMementoChanged();

  }

  private void fireMementoChanged( )
  {
    final IZmlMementoListener[] listeners = m_listener.toArray( new IZmlMementoListener[] {} );
    for( final IZmlMementoListener listener : listeners )
    {
      listener.mementoChanged();
    }
  }

  @Override
  public void addListener( final IZmlMementoListener listener )
  {
    m_listener.add( listener );
  }

  @Override
  public void removeListener( final IZmlMementoListener listener )
  {
    m_listener.remove( listener );
  }
}
