/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.core.diagram.data;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.event.IObservationListener;
import org.kalypso.ogc.sensor.event.ObservationChangeType;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.base.request.IRequestStrategy;
import org.kalypso.zml.core.diagram.base.IZmlLayer;

import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;

/**
 * @author Dirk Kuch
 * @deprecated Do not use, all observations should be provided by an IObservationProvider -> do only use the other
 *             implementation, even if loading synchronously
 */
@Deprecated
public class ZmlObservationDataHandler implements IZmlLayerDataHandler, IObservationListener
{
  private final String m_targetAxisId;

  private IObservation m_observation;

  private IAxis m_valueAxis;

  private final IZmlLayer m_layer;

  private DateRange m_dateRange;

  private final Set<IObservationListener> m_listeners = Collections.synchronizedSet( new LinkedHashSet<IObservationListener>() );

  public ZmlObservationDataHandler( final IZmlLayer layer, final String targetAxisId )
  {
    m_layer = layer;
    m_targetAxisId = targetAxisId;
  }

  @Override
  public void dispose( )
  {
  }

  @Override
  public IAxis getValueAxis( )
  {
    synchronized( this )
    {
      if( m_valueAxis == null )
      {
        m_valueAxis = AxisUtils.findAxis( m_observation.getAxes(), m_targetAxisId );
      }

      return m_valueAxis;
    }
  }

  @Override
  public String getTargetAxisId( )
  {
    return m_targetAxisId;
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter.isAssignableFrom( IObservation.class ) )
    {
      return m_observation;
    }

    return null;
  }

  public void setObservation( final IObservation observation )
  {
    synchronized( this )
    {
      m_valueAxis = null;

      if( m_observation != null )
      {
        m_observation.removeListener( this );
      }

      m_observation = observation;
      m_observation.addListener( this );
    }

    m_layer.onObservationChanged( ContentChangeType.all );
  }

  public void setDateRange( final DateRange dateRange )
  {
    m_dateRange = dateRange;
  }

  @Override
  public IRequest getRequest( )
  {
    return new ObservationRequest( m_dateRange );
  }

  public void addListener( final IObservationListener listener )
  {
    m_listeners.add( listener );
  }

  @Override
  public void observationChanged( final IObservation obs, final Object source, final ObservationChangeType type )
  {
    final IObservationListener[] listeners = m_listeners.toArray( new IObservationListener[] {} );
    for( final IObservationListener listener : listeners )
    {
      listener.observationChanged( obs, source, type );
    }
  }

  @Override
  public void setRequestStrategy( final IRequestStrategy strategy )
  {
    // FIXME
  }

  @Override
  public IRequestStrategy getRequestStrategy( )
  {
    // FIXME

    return null;
  }
}
