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
package org.kalypso.zml.core.table.model.interpolation;

import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.IObservationListener;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;
import org.kalypso.ogc.sensor.timeseries.wq.WQTimeserieProxy;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;

/**
 * @author Gernot Belger
 */
public class TimeseriesObservation implements ITimeseriesObservation
{
  private final IObservation m_delegate;

  private final IAxis m_valueAxis;

  private final IAxis m_statusAxis;

  private final IAxis m_sourceAxis;

  private final IAxis m_dateAxis;

  private final DataSourceHandler m_sourceHandler;

  private ITupleModel m_model;

  public TimeseriesObservation( final IObservation delegate, final IAxis valueAxis )
  {
    Assert.isTrue( delegate instanceof SimpleObservation || delegate instanceof WQTimeserieProxy );

    m_delegate = delegate;
    m_valueAxis = valueAxis;

    m_sourceHandler = new DataSourceHandler( delegate.getMetadataList() );

    final IAxis[] axes = delegate.getAxes();
    m_dateAxis = AxisUtils.findDateAxis( axes );
    m_statusAxis = AxisUtils.findStatusAxis( axes );
    m_sourceAxis = AxisUtils.findDataSourceAxis( axes );
  }

  /**
   * @see org.kalypso.zml.core.table.model.interpolation.ITimeseriesObservation#getDateAxis()
   */
  @Override
  public IAxis getDateAxis( )
  {
    return m_dateAxis;
  }

  /**
   * @see org.kalypso.zml.core.table.model.interpolation.ITimeseriesObservation#getValueAxis()
   */
  @Override
  public IAxis getValueAxis( )
  {
    return m_valueAxis;
  }

  /**
   * @see org.kalypso.zml.core.table.model.interpolation.ITimeseriesObservation#getSourceAxis()
   */
  @Override
  public IAxis getSourceAxis( )
  {
    return m_sourceAxis;
  }

  /**
   * @see org.kalypso.zml.core.table.model.interpolation.ITimeseriesObservation#getStatusAxis()
   */
  @Override
  public IAxis getStatusAxis( )
  {
    return m_statusAxis;
  }

  /**
   * @see org.kalypso.zml.core.table.model.interpolation.ITimeseriesObservation#getDate(int)
   */
  @Override
  public Date getDate( final int row ) throws SensorException
  {
    return (Date) getValues( null ).get( row, m_dateAxis );
  }

  /**
   * @see org.kalypso.zml.core.table.model.interpolation.ITimeseriesObservation#getValue(int)
   */
  @Override
  public Number getValue( final int row ) throws SensorException
  {
    return (Number) getValues( null ).get( row, m_valueAxis );
  }

  /**
   * @see org.kalypso.zml.core.table.model.interpolation.ITimeseriesObservation#getStatus(int)
   */
  @Override
  public int getStatus( final int row ) throws SensorException
  {
    if( m_statusAxis == null )
      return KalypsoStati.BIT_OK;

    final Object statusObject = getValues( null ).get( row, m_statusAxis );
    if( statusObject instanceof Number )
      return ((Number) statusObject).intValue();

    return KalypsoStati.BIT_OK;
  }

  /**
   * @see org.kalypso.zml.core.table.model.interpolation.ITimeseriesObservation#getSource(int)
   */
  @Override
  public String getSource( final int row ) throws SensorException
  {
    if( m_sourceAxis == null )
      return IDataSourceItem.SOURCE_UNKNOWN;

    final Object sourceIndex = getValues( null ).get( row, m_sourceAxis );
    if( sourceIndex instanceof Number )
      return m_sourceHandler.getDataSourceIdentifier( ((Number) sourceIndex).intValue() );

    return IDataSourceItem.SOURCE_UNKNOWN;
  }

  // TODO: instead: give a list of commands that will be executed inside the transaction... or something similar.
  @Override
  public void startTransaction( ) throws SensorException
  {
    m_model = getValues( null );
  }

  @Override
  public void stopTransaction( ) throws SensorException
  {
    setValues( m_model );
    m_model = null;
    fireChangedEvent( this );
  }

  /**
   * @see org.kalypso.zml.core.table.model.interpolation.ITimeseriesObservation#inTransaction()
   */
  @Override
  public boolean inTransaction( )
  {
    return m_model != null;
  }

  /**
   * @see org.kalypso.zml.core.table.model.interpolation.ITimeseriesObservation#update(int, double, java.lang.String,
   *      int)
   */
  @Override
  public void update( final int index, final double value, final String dataSource, final int status ) throws SensorException
  {
    if( m_model == null )
      throw new IllegalStateException( "Call startTransaction first" );

    m_model.set( index, m_valueAxis, value );

    if( m_statusAxis != null )
      m_model.set( index, m_statusAxis, KalypsoStati.BIT_OK );

    if( m_sourceAxis != null )
    {
      final int sourceIndex = m_sourceHandler.getDataSourceIndex( dataSource );
      m_model.set( index, m_sourceAxis, sourceIndex );
    }
  }

  // // DELEGATE METHODS TO OBSERVATION ////

  /**
   * @param listener
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#addListener(org.kalypso.ogc.sensor.IObservationListener)
   */
  @Override
  public void addListener( final IObservationListener listener )
  {
    m_delegate.addListener( listener );
  }

  /**
   * @param listener
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#removeListener(org.kalypso.ogc.sensor.IObservationListener)
   */
  @Override
  public void removeListener( final IObservationListener listener )
  {
    m_delegate.removeListener( listener );
  }

  /**
   * @param source
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#fireChangedEvent(java.lang.Object)
   */
  @Override
  public void fireChangedEvent( final Object source )
  {
    m_delegate.fireChangedEvent( source );
  }

  /**
   * @param visitor
   * @param request
   * @throws SensorException
   * @see org.kalypso.ogc.sensor.IObservation#accept(org.kalypso.ogc.sensor.visitor.IObservationVisitor,
   *      org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public void accept( final IObservationVisitor visitor, final IRequest request ) throws SensorException
  {
    m_delegate.accept( visitor, request );
  }

  /**
   * @return
   * @see org.kalypso.ogc.sensor.IObservation#getName()
   */
  @Override
  public String getName( )
  {
    return m_delegate.getName();
  }

  /**
   * @return
   * @see org.kalypso.ogc.sensor.IObservation#getMetadataList()
   */
  @Override
  public MetadataList getMetadataList( )
  {
    return m_delegate.getMetadataList();
  }

  /**
   * @return
   * @see org.kalypso.ogc.sensor.IObservation#getAxes()
   */
  @Override
  public IAxis[] getAxes( )
  {
    return m_delegate.getAxes();
  }

  /**
   * @param args
   * @return
   * @throws SensorException
   * @see org.kalypso.ogc.sensor.IObservation#getValues(org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public ITupleModel getValues( final IRequest args ) throws SensorException
  {
    return m_delegate.getValues( args );
  }

  /**
   * @param values
   * @throws SensorException
   * @see org.kalypso.ogc.sensor.IObservation#setValues(org.kalypso.ogc.sensor.ITupleModel)
   */
  @Override
  public void setValues( final ITupleModel values ) throws SensorException
  {
    m_delegate.setValues( values );
  }

  /**
   * @return
   * @see org.kalypso.ogc.sensor.IObservation#getHref()
   */
  @Override
  public String getHref( )
  {
    return m_delegate.getHref();
  }
}