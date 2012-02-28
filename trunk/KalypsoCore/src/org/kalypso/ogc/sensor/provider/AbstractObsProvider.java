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
package org.kalypso.ogc.sensor.provider;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.event.IObservationListener;
import org.kalypso.ogc.sensor.event.ObservationChangeType;
import org.kalypso.ogc.sensor.request.IRequest;

/**
 * @author Gernot Belger
 */
public abstract class AbstractObsProvider implements IObsProvider
{
  private final Set<IObsProviderListener> m_listeners = Collections.synchronizedSet( new LinkedHashSet<IObsProviderListener>() );

  private IRequest m_request;

  private IObservation m_observation;

  private final IObservationListener m_observationListener = new IObservationListener()
  {
    @Override
    public void observationChanged( final IObservation obs, final Object source, final ObservationChangeType type )
    {
      fireObservationChanged( source, type );
    }
  };

  protected final void setArguments( final IRequest args )
  {
    m_request = args;
  }

  @Override
  public void dispose( )
  {
    if( m_observation != null )
      m_observation.removeListener( m_observationListener );

    m_listeners.clear();
  }

  @Override
  public IRequest getArguments( )
  {
    return m_request;
  }

  protected final void setObservation( final IObservation obs )
  {
    if( m_observation != null )
      m_observation.removeListener( m_observationListener );

    m_observation = obs;

    if( m_observation != null )
      m_observation.addListener( m_observationListener );

    fireChanged();
  }

  @Override
  public IObservation getObservation( )
  {
    return m_observation;
  }

  @Override
  public final boolean isLoaded( )
  {
    return getObservation() != null;
  }

  @Override
  public final void addListener( final IObsProviderListener l )
  {
    m_listeners.add( l );
  }

  @Override
  public final void removeListener( final IObsProviderListener l )
  {
    m_listeners.remove( l );
  }

  protected final void fireObservationChanged( final Object source, final ObservationChangeType type )
  {
    final IObsProviderListener[] listeners = m_listeners.toArray( new IObsProviderListener[] {} );
    for( final IObsProviderListener listener : listeners )
    {
      if( type.isStructureChanged() )
        listener.observationReplaced();
      else if( type.isValuesChanged() )
        listener.observationChanged( source );
      else
        throw new UnsupportedOperationException();
    }
  }

  /**
   * ATM only triggered from setObservation
   */
  public void fireChanged( )
  {
    final Object[] listeners = m_listeners.toArray();
    for( final Object listener : listeners )
    {
      ((IObsProviderListener) listener).observationReplaced();
    }
  }
}
