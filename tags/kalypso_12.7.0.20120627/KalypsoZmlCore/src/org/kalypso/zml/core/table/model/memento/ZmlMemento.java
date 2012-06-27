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
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.zml.core.base.IZmlSourceElement;

/**
 * @author Dirk Kuch
 */
public class ZmlMemento implements IZmlMemento
{
  private final Map<IPoolableObjectType, List<IZmlSourceElement>> m_provider = new LinkedHashMap<IPoolableObjectType, List<IZmlSourceElement>>();

  protected final Set<IZmlMementoListener> m_listener = Collections.synchronizedSet( new LinkedHashSet<IZmlMementoListener>() );

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
  public synchronized void register( final IZmlSourceElement source )
  {
    final IPoolableObjectType poolKey = source.getPoolKey();
    List<IZmlSourceElement> sources = m_provider.get( poolKey );
    if( Objects.isNull( sources ) )
    {
      sources = new ArrayList<IZmlSourceElement>();
      m_provider.put( poolKey, sources );
    }

    sources.add( source );

    // lazy binding of IObsProvider!
    final RegisterObsProviderListenerJob job = new RegisterObsProviderListenerJob( source, m_obsListener );
    job.schedule( 50 );
  }

  @Override
  public synchronized void store( ) throws CoreException
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    final IZmlSourceElement[] dirtyElements = findDirtyElements();
    for( final IZmlSourceElement element : dirtyElements )
    {
      final IObsProvider provider = element.getObsProvider();
      final IObservation observation = provider.getObservation();
      if( Objects.isNotNull( observation ) )
        pool.saveObject( observation, new NullProgressMonitor() );
    }

    doCleanup();
  }

  private synchronized void doCleanup( )
  {
    synchronized( this )
    {
      final Set<Entry<IPoolableObjectType, List<IZmlSourceElement>>> entries = m_provider.entrySet();
      for( final Entry<IPoolableObjectType, List<IZmlSourceElement>> entry : entries )
      {
        final List<IZmlSourceElement> sources = entry.getValue();
        for( final IZmlSourceElement source : sources )
        {
          final IObsProvider provider = source.getObsProvider();
          provider.removeListener( m_obsListener );
          provider.dispose();
        }
      }

      m_provider.clear();

    }
  }

  @Override
  public synchronized IZmlSourceElement[] findDirtyElements( )
  {
    final Set<IZmlSourceElement> result = new LinkedHashSet<IZmlSourceElement>();

    final Set<Entry<IPoolableObjectType, List<IZmlSourceElement>>> entries = m_provider.entrySet();
    for( final Entry<IPoolableObjectType, List<IZmlSourceElement>> entry : entries )
    {
      final List<IZmlSourceElement> sources = entry.getValue();
      for( final IZmlSourceElement source : sources )
      {
        if( source.isDirty() )
        {
          result.add( source );
          break; // source points to IPoolableObjectType - so scan next entry!
        }
      }
    }

    return result.toArray( new IZmlSourceElement[] {} );
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
