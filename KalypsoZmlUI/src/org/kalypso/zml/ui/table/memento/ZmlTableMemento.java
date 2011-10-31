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
package org.kalypso.zml.ui.table.memento;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
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
public class ZmlTableMemento implements IZmlTableMemento
{
  private final Map<IPoolableObjectType, ILabeledObsProvider> m_elements = Collections.synchronizedMap( new HashMap<IPoolableObjectType, ILabeledObsProvider>() );

  private final IObsProviderListener m_providerListener;

  /**
   * @param obsProviderListener
   *          This listener will be added to all registered obs-provider of this memento.
   */
  public ZmlTableMemento( final IObsProviderListener obsProviderListener )
  {
    m_providerListener = obsProviderListener;
  }

  /**
   * @see org.kalypso.zml.ui.table.memento.IZmlTableMemento#dispose()
   */
  @Override
  public void dispose( )
  {
    cleanup();
  }

  @Override
  public synchronized void register( final IPoolableObjectType poolKey, final ILabeledObsProvider provider )
  {
    provider.addListener( m_providerListener );

    final ILabeledObsProvider oldObsProvider = m_elements.get( poolKey );
    if( oldObsProvider != null )
      oldObsProvider.dispose();

    m_elements.put( poolKey, provider );
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#saveData(boolean)
   */
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

    cleanup();
  }

  private synchronized void cleanup( )
  {
    for( final IObsProvider provider : m_elements.values() )
      provider.dispose();

    m_elements.clear();
  }

  @Override
  public synchronized ILabeledObsProvider[] findDirtyElements( )
  {
    final Collection<ILabeledObsProvider> result = new ArrayList<ILabeledObsProvider>();

    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    for( final ILabeledObsProvider provider : m_elements.values() )
    {
      final IObservation observation = provider.getObservation();
      if( observation != null )
      {
        final KeyInfo info = pool.getInfo( observation );
        if( info.isDirty() )
          result.add( provider );
      }
    }

    return result.toArray( new ILabeledObsProvider[result.size()] );
  }
}