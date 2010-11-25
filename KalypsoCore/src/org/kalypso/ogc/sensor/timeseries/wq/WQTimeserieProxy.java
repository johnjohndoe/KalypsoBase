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
package org.kalypso.ogc.sensor.timeseries.wq;

import java.util.NoSuchElementException;

import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.IObservationListener;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.event.ObservationEventAdapter;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;

/**
 * WQTimeserieProxy for proxying W, Q, and V Timeseries.
 * 
 * @author schlienger
 */
public class WQTimeserieProxy implements IObservation
{
  private final ObservationEventAdapter m_eventAdapter = new ObservationEventAdapter( this );

  private final IObservation m_obs;

  private IAxis[] m_axes;

  private IAxis m_dateAxis;

  private IAxis m_srcAxis;

  private IAxis m_srcStatusAxis;

  private IAxis m_destAxis;

  private IAxis m_destStatusAxis;

  private int m_destAxisPos;

  private int m_destStatusAxisPos;

  private final String m_proxyAxisType;

  private final String m_realAxisType;

  private IWQConverter m_conv = null;

  private IRequest m_cachedArgs = null;

  private ITupleModel m_cachedModel = null;

  /**
   * Constructor
   * 
   * @param realAxisType
   *          type of the real axis that will be used to proxy another axis
   * @param proxyAxisType
   *          type of the axis that should be generated based on the real axis
   * @param obs
   *          the underlying observation to proxy
   */
  public WQTimeserieProxy( final String realAxisType, final String proxyAxisType, final IObservation obs )
  {
    m_obs = obs;
    m_realAxisType = realAxisType;
    m_proxyAxisType = proxyAxisType;

    configure( obs );
  }

  private void configure( final IObservation obs )
  {
    final IAxis[] axes = obs.getAxisList();
    m_axes = new IAxis[axes.length + 2];
    for( int i = 0; i < axes.length; i++ )
      m_axes[i] = axes[i];

    m_dateAxis = ObservationUtilities.findAxisByType( axes, ITimeseriesConstants.TYPE_DATE );

    final String name = TimeseriesUtils.getName( m_proxyAxisType );
    final String unit = TimeseriesUtils.getUnit( m_proxyAxisType );

    m_srcAxis = ObservationUtilities.findAxisByType( axes, m_realAxisType );
    try
    {
      m_srcStatusAxis = KalypsoStatusUtils.findStatusAxisFor( axes, m_srcAxis );
    }
    catch( final NoSuchElementException ignored )
    {
      // this exception is ignored since the source-status axis is optional
    }

    m_destAxis = new DefaultAxis( name, m_proxyAxisType, unit, Double.class, false, false );
    m_destAxisPos = m_axes.length - 2;
    m_axes[m_destAxisPos] = m_destAxis;

    m_destStatusAxis = KalypsoStatusUtils.createStatusAxisFor( m_destAxis, false );
    m_destStatusAxisPos = m_axes.length - 1;
    m_axes[m_destStatusAxisPos] = m_destStatusAxis;

    if( name.length() == 0 )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.WQTimeserieProxy.0" ) + m_proxyAxisType ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter#getAxisList()
   */
  @Override
  public IAxis[] getAxisList( )
  {
    return m_axes;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getValues(org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public ITupleModel getValues( final IRequest args ) throws SensorException
  {
    if( m_cachedModel != null && (m_cachedArgs == null && args == null || (m_cachedArgs != null && m_cachedArgs.equals( args ))) )
      return m_cachedModel;

    m_cachedModel = new WQTuppleModel( m_obs.getValues( args ), m_axes, m_dateAxis, m_srcAxis, m_srcStatusAxis, m_destAxis, m_destStatusAxis, getWQConverter(), m_destAxisPos, m_destStatusAxisPos );

    m_cachedArgs = args;

    return m_cachedModel;
  }

  private IWQConverter getWQConverter( ) throws SensorException
  {
    if( m_conv == null )
      m_conv = WQFactory.createWQConverter( this );

    return m_conv;
  }

  /**
   * @see org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter#setValues(org.kalypso.ogc.sensor.ITuppleModel)
   */
  @Override
  public void setValues( final ITupleModel values ) throws SensorException
  {
    m_obs.setValues( WQTuppleModel.reverse( values, m_obs.getAxisList() ) );
  }

  public IAxis getDateAxis( )
  {
    return m_dateAxis;
  }

  public IAxis getDestAxis( )
  {
    return m_destAxis;
  }

  public IAxis getSrcAxis( )
  {
    return m_srcAxis;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#addListener(org.kalypso.ogc.sensor.IObservationListener)
   */
  @Override
  public void addListener( final IObservationListener listener )
  {
    m_eventAdapter.addListener( listener );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#removeListener(org.kalypso.ogc.sensor.IObservationListener)
   */
  @Override
  public void removeListener( final IObservationListener listener )
  {
    m_eventAdapter.removeListener( listener );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#fireChangedEvent(java.lang.Object)
   */
  @Override
  public void fireChangedEvent( final Object source )
  {
    m_eventAdapter.fireChangedEvent( source );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getName()
   */
  @Override
  public String getName( )
  {
    return m_obs.getName();
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getMetadataList()
   */
  @Override
  public MetadataList getMetadataList( )
  {
    return m_obs.getMetadataList();
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getHref()
   */
  @Override
  public String getHref( )
  {
    return m_obs.getHref();
  }

  @Override
  public String toString( )
  {
    return m_obs.toString();
  }

  @Override
  public boolean equals( final Object obj )
  {
    return m_obs.equals( obj );
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return m_obs.hashCode();
  }

}