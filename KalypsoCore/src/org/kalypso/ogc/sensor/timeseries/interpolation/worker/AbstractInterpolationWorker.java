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
package org.kalypso.ogc.sensor.timeseries.interpolation.worker;

import java.util.Calendar;
import java.util.Date;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.repository.IDataSourceItem;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractInterpolationWorker implements ICoreRunnableWithProgress
{
  /**
   * the base tuple model which will be processed by derived workers
   */
  private final ITupleModel m_baseModel;

  private final DateRange m_dateRange;

  /**
   * the interpolated result model
   */
  private final SimpleTupleModel m_interpolated;

  private final IInterpolationFilter m_filter;

  public AbstractInterpolationWorker( final IInterpolationFilter filter, final ITupleModel baseModel, final DateRange dateRange )
  {
    m_filter = filter;
    m_baseModel = baseModel;
    m_dateRange = dateRange;

    m_interpolated = new SimpleTupleModel( getBaseModel().getAxes() );
  }

  protected IInterpolationFilter getFilter( )
  {
    return m_filter;
  }

  protected ITupleModel getBaseModel( )
  {
    return m_baseModel;
  }

  public boolean isFilled( )
  {
    return m_filter.isFilled();
  }

  protected boolean isLastFilledWithValid( )
  {
    return m_filter.isLastFilledWithValid();
  }

  protected DateRange getDateRange( )
  {
    return m_dateRange;
  }

  public static AbstractInterpolationWorker createWorker( final IInterpolationFilter filter, final IRequest request ) throws SensorException
  {
    final DateRange dateRange = request == null ? null : request.getDateRange();

    // BUGIFX: fixes the problem with the first value:
    // the first value was always ignored, because the interval
    // filter cannot handle the first value of the source observation
    // FIX: we just make the request a big bigger in order to get a new first value
    // HACK: we always use DAY, so that work fine only up to time series of DAY-quality.
    // Maybe there should be one day a mean to determine, which is the right amount.
    final ITupleModel values = ObservationUtilities.requestBuffered( filter.getObservation(), dateRange, Calendar.DAY_OF_MONTH, 2 );

    if( values.size() == 0 )
      return new EmptyValueInterpolationWorker( filter, values, dateRange );

    return new ValueInterpolationWorker( filter, values, dateRange );
  }

  public SimpleTupleModel getInterpolatedModel( )
  {
    return m_interpolated;
  }

  protected Integer getDefaultStatus( )
  {
    return m_filter.getDefaultStatus();
  }

  protected IAxis getDateAxis( )
  {
    final IAxis[] axes = getBaseModel().getAxes();

    return ObservationUtilities.findAxisByClass( axes, Date.class );
  }

  /**
   * @return implementation changed - now only real value axes will be returned! a values axis have to deal with it owns
   *         status and data source axis
   */
  protected IAxis[] getValueAxes( )
  {
    final IAxis[] axes = getBaseModel().getAxes();
    final IAxis[] valueAxes = AxisUtils.findValueAxes( axes, false );

    return ObservationUtilities.findAxesByClasses( valueAxes, new Class[] { Number.class, Boolean.class } );
  }

  /**
   * Add one tuple with default values. The date is set to the given calendar which is stepped after the tuple was
   * added.
   */
  protected void addDefaultTupple( final IAxis dateAxis, final LocalCalculationStack stack, final Calendar calendar ) throws SensorException
  {
    final SimpleTupleModel interpolatedModel = getInterpolatedModel();

    final LocalCalculationStackValue[] values = stack.getValues();

    final Object[] tuple = new Object[interpolatedModel.getAxes().length];
    tuple[interpolatedModel.getPosition( dateAxis )] = calendar.getTime();

    final DataSourceHandler dataSourceHandler = new DataSourceHandler( m_filter.getMetaDataList() );

    for( final LocalCalculationStackValue value : values )
    {
      final IAxis[] baseAxes = interpolatedModel.getAxes();
      final IAxis valueAxis = value.getAxis();
      final IAxis statusAxis = AxisUtils.findStatusAxis( baseAxes, valueAxis );
      final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( baseAxes, valueAxis );

      final int posValueAxis = interpolatedModel.getPosition( valueAxis );
      final TupleModelDataSet defaultValue = value.getDefaultValue( m_filter );
      tuple[posValueAxis] = defaultValue.getValue();

      if( Objects.isNotNull( statusAxis ) )
      {
        final int posStatusAxis = interpolatedModel.getPosition( statusAxis );
        tuple[posStatusAxis] = defaultValue.getStatus();
      }

      if( Objects.isNotNull( dataSourceAxis ) )
      {
        final int posDataSourceAxis = interpolatedModel.getPosition( dataSourceAxis );
        final int dataSourceIndex = dataSourceHandler.addDataSource( defaultValue.getSource(), defaultValue.getSource() );

        tuple[posDataSourceAxis] = dataSourceIndex;
      }
    }

    interpolatedModel.addTuple( tuple );
    doStep( calendar );
  }

  protected void doStep( final Calendar calendar )
  {
    calendar.add( m_filter.getCalendarField(), m_filter.getCalendarAmnount() );
  }

  protected TupleModelDataSet toDataSet( final ITupleModel baseModel, final int index, final LocalCalculationStackValue value ) throws SensorException
  {
    final IAxis[] baseAxes = baseModel.getAxes();
    final IAxis valueAxis = value.getAxis();
    final IAxis statusAxis = AxisUtils.findStatusAxis( baseAxes, valueAxis );
    final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( baseAxes, valueAxis );

    final Number number = (Number) getBaseModel().get( index, valueAxis );

    Integer status = KalypsoStati.BIT_OK;
    if( Objects.isNotNull( statusAxis ) )
    {
      final Number statusValue = (Number) getBaseModel().get( index, statusAxis );
      if( Objects.isNotNull( statusValue ) )
        status = statusValue.intValue();
    }

    String dataSource = IDataSourceItem.SOURCE_UNKNOWN;
    if( Objects.isNotNull( dataSourceAxis ) )
    {
      final Number dataSrcIndex = (Number) getBaseModel().get( index, dataSourceAxis );
      if( Objects.isNotNull( dataSrcIndex ) )
      {
        final DataSourceHandler handler = new DataSourceHandler( getFilter().getMetaDataList() );
        dataSource = handler.getDataSourceIdentifier( dataSrcIndex.intValue() );
      }
    }

    return new TupleModelDataSet( valueAxis, number, status, dataSource );
  }
}
