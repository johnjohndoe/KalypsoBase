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

import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.IObservationListener;
import org.kalypso.ogc.sensor.request.IRequest;

/**
 * @author Gernot Belger
 */
public abstract class AbstractObsProvider implements IObsProvider
{
  private final Set<IObsProviderListener> m_listeners = new LinkedHashSet<IObsProviderListener>();

  private IRequest m_request;

  private IObservation m_observation;

  private final IObservationListener m_observationListeer = new IObservationListener()
  {
    @Override
    public void observationChanged( final IObservation obs, final Object source )
    {
      fireObservationChanged( source );
    }
  };

  protected final void setArguments( final IRequest args )
  {
    m_request = args;
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_observation != null )
      m_observation.removeListener( m_observationListeer );
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProvider#getArguments()
   */
  @Override
  public IRequest getArguments( )
  {
    return m_request;
  }

  protected final void setObservation( final IObservation obs )
  {
    if( m_observation != null )
      m_observation.removeListener( m_observationListeer );

    m_observation = obs;

    if( m_observation != null )
      m_observation.addListener( m_observationListeer );

    synchronized( this )
    {
      fireChanged();
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProvider#getObservation()
   */
  @Override
  public IObservation getObservation( )
  {
    return m_observation;
  }

  /**
   * @see org.kalypso.hwv.core.chart.provider.observation.IZmlObsProvider#isLoaded()
   */
  @Override
  public final boolean isLoaded( )
  {
    return getObservation() != null;
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProvider#addListener(org.kalypso.ogc.sensor.template.IObsProviderListener)
   */
  @Override
  public final void addListener( final IObsProviderListener l )
  {
    m_listeners.add( l );
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProvider#removeListener(org.kalypso.ogc.sensor.template.IObsProviderListener)
   */
  @Override
  public final void removeListener( final IObsProviderListener l )
  {
    m_listeners.remove( l );
  }

  protected final void fireObservationChanged( final Object source )
  {
    final Object[] listeners;
    synchronized( m_listeners )
    {
      listeners = m_listeners.toArray();
    }

    for( final Object listener : listeners )
      ((IObsProviderListener) listener).observationChanged( source );
  }

  /**
   * ATM only triggered from setObservation
   */
  public void fireChanged( )
  {
    final Object[] listeners;
    synchronized( m_listeners )
    {
      listeners = m_listeners.toArray();
    }

    for( final Object listener : listeners )
      ((IObsProviderListener) listener).observationReplaced();
  }
}
