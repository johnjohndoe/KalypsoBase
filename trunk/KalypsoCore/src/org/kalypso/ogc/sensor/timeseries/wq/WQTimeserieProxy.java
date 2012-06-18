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

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.AxisSet;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.event.IObservationListener;
import org.kalypso.ogc.sensor.event.ObservationChangeType;
import org.kalypso.ogc.sensor.event.ObservationEventAdapter;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.impl.ITupleModelChangeListener;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceProxyObservation;
import org.kalypso.ogc.sensor.util.Observations;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;

/**
 * WQTimeserieProxy for proxying W, Q, and V Timeseries.
 * 
 * @author schlienger
 */
public class WQTimeserieProxy implements IObservation
{
  private final ObservationEventAdapter m_eventAdapter = new ObservationEventAdapter( this );

  private IObservation m_obs;

  private IAxis[] m_axes;

  private IAxis m_dateAxis;

  private AxisSet m_srcAxis;

  private AxisSet m_targetAxis;

  private final String m_proxyAxisType;

  private final String m_realAxisType;

  private IWQConverter m_conv = null;

  private IRequest m_cachedArgs = null;

  private ITupleModel m_cachedModel = null;

  /**
   * @param realAxisType
   *          type of the real axis that will be used to proxy another axis
   * @param proxyAxisType
   *          type of the axis that should be generated based on the real axis
   * @param obs
   *          the underlying observation to proxy
   */
  public WQTimeserieProxy( final String realAxisType, final String proxyAxisType, final IObservation obs )
  {

    m_realAxisType = realAxisType;
    m_proxyAxisType = proxyAxisType;

    configure( obs );
  }

  private void configure( final IObservation obs )
  {
    IAxis[] axes = obs.getAxes();

    /** base observation defines source and status axis? */
    final IAxis baseValueAxis = AxisUtils.findAxis( axes, m_realAxisType );
    final IAxis baseDataSourceAxis = AxisUtils.findDataSourceAxis( axes, baseValueAxis );
    if( Objects.isNull( baseDataSourceAxis ) )
    {
      m_obs = new DataSourceProxyObservation( obs, obs.getHref(), obs.getHref() );
      axes = m_obs.getAxes();
    }
    else
      m_obs = obs;

    m_axes = new IAxis[axes.length + 3];
    for( int index = 0; index < axes.length; index++ )
    {
      m_axes[index] = axes[index];
    }

    m_dateAxis = AxisUtils.findDateAxis( axes );

    final String name = TimeseriesUtils.getName( m_proxyAxisType );
    final String unit = TimeseriesUtils.getUnit( m_proxyAxisType );

    m_srcAxis = new AxisSet( m_axes, AxisUtils.findAxis( axes, m_realAxisType ) );

    final IAxis targetAxis = new DefaultAxis( name, m_proxyAxisType, unit, Double.class, false, false );
    final IAxis targetStatusAxis = KalypsoStatusUtils.createStatusAxisFor( targetAxis, false );
    final IAxis targetDataSourceAxis = DataSourceHelper.createSourceAxis( targetAxis, false );

    m_axes[m_axes.length - 3] = targetAxis;
    m_axes[m_axes.length - 2] = targetStatusAxis;
    m_axes[m_axes.length - 1] = targetDataSourceAxis;

    m_targetAxis = new AxisSet( targetAxis, targetStatusAxis, targetDataSourceAxis );

    if( name.length() == 0 )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.wq.WQTimeserieProxy.0" ) + m_proxyAxisType ); //$NON-NLS-1$
  }

  @Override
  public IAxis[] getAxes( )
  {
    return m_axes;
  }

  @Override
  public ITupleModel getValues( final IRequest args ) throws SensorException
  {
    if( isValid( args ) )
      return m_cachedModel;

    m_cachedModel = new WQTuppleModel( m_obs.getValues( args ), m_obs.getMetadataList(), m_axes, m_srcAxis, m_targetAxis, getWQConverter() );
    m_cachedArgs = args;

    m_cachedModel.addChangeListener( new ITupleModelChangeListener()
    {
      @Override
      public void modelChangedEvent( final ObservationChangeType type )
      {
        fireChangedEvent( this, type );
      }
    } );

    return m_cachedModel;
  }

  private boolean isValid( final IRequest args )
  {
    if( m_cachedModel == null )
      return false;

    return Objects.equal( m_cachedArgs, args );
  }

  private IWQConverter getWQConverter( ) throws SensorException
  {
    if( m_conv == null )
      m_conv = WQFactory.createWQConverter( this );

    return m_conv;
  }

  @Override
  public void setValues( final ITupleModel values ) throws SensorException
  {
    m_obs.setValues( WQTuppleModel.reverse( values, m_obs.getAxes() ) );
  }

  public IAxis getDateAxis( )
  {
    return m_dateAxis;
  }

  public AxisSet getTargetAxes( )
  {
    return m_targetAxis;
  }

  public AxisSet getSourceAxes( )
  {
    return m_srcAxis;
  }

  @Override
  public void addListener( final IObservationListener listener )
  {
    m_eventAdapter.addListener( listener );
  }

  @Override
  public void removeListener( final IObservationListener listener )
  {
    m_eventAdapter.removeListener( listener );
  }

  @Override
  public void fireChangedEvent( final Object source, final ObservationChangeType type )
  {
    m_eventAdapter.fireChangedEvent( source, type );
  }

  @Override
  public String getName( )
  {
    return m_obs.getName();
  }

  @Override
  public MetadataList getMetadataList( )
  {
    return m_obs.getMetadataList();
  }

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

  @Override
  public int hashCode( )
  {
    return m_obs.hashCode();
  }

  @Override
  public void accept( final IObservationVisitor visitor, final IRequest request, final int direction ) throws SensorException
  {
    Observations.accept( this, visitor, request, direction );
  }

  @Override
  public boolean isEmpty( )
  {
    return m_obs.isEmpty();
  }
}