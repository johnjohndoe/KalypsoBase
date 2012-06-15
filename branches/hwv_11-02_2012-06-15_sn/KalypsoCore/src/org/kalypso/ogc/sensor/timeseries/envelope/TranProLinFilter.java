/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.ogc.sensor.timeseries.envelope;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Assert;
import org.kalypso.commons.math.IMathOperation;
import org.kalypso.commons.math.MathOperationFactory;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;
import org.kalypso.repository.IDataSourceItem;

/**
 * The Linear-Progressiv-Transformation Filter is used for creating 'lower and upper envelopes' in the sense of a
 * timeserie. It is currently designed to work only with timeseries: meaning an observation with an axis of type date.
 * 
 * @author schlienger
 */
public class TranProLinFilter extends AbstractObservationFilter
{
  private static final String FILTER_ID = "TranProLinFilter"; //$NON-NLS-1$

  final static String DATA_SOURCE = IDataSourceItem.FILTER_SOURCE + FILTER_ID;
  
  private final double m_operandBegin;

  private final double m_operandEnd;

  private final IMathOperation m_operation;

  private final String m_axisType;

  private final DateRange m_range;

  private IAxis[] m_axes;

  private MetadataList m_metadata;

  private int m_dataSourceIndex;

  /**
   * @param statusToMerge
   *          status is merged to modified values as bitwise OR operation (use <code>statusToMerge=0</code> for
   *          unchanged status)
   * @param axisTypes
   */
  public TranProLinFilter( final DateRange range, final String operator, final double operandBegin, final double operandEnd, final String axisType )
  {
    m_range = range;
    m_operandBegin = operandBegin;
    m_operandEnd = operandEnd;
    m_axisType = axisType;
    m_operation = MathOperationFactory.createMathOperation( operator );

    Assert.isNotNull( axisType );

    final long rangeLength = m_range.getLength();

    if( rangeLength <= 0 || rangeLength == Long.MAX_VALUE )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.envelope.TranProLinFilter.0" ) + m_range ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void initFilter( final Object conf, final IObservation obs, final URL context ) throws SensorException
  {
    super.initFilter( conf, obs, context );

    if( obs != null )
    {
      final IAxis[] sourceAxes = obs.getAxes();
      m_axes = buildTargetAxes( sourceAxes );
    }
    
    m_metadata = MetadataHelper.clone( obs.getMetadataList() );
    final DataSourceHandler dataSourceHandler = new DataSourceHandler( m_metadata );
    m_dataSourceIndex = dataSourceHandler.addDataSource( DATA_SOURCE, DATA_SOURCE );
  }
  
  @Override
  public MetadataList getMetadataList( )
  {
    return m_metadata;
  }

  @Override
  public IAxis[] getAxes( )
  {
    return m_axes;
  }

  @Override
  protected void appendSettings( final MetadataList metadata )
  {
  }

  @Override
  public ITupleModel getValues( final IRequest args ) throws SensorException
  {
    final ITupleModel sourceModel = super.getValues( args );

    final int outerSourceCount = sourceModel.size();
    if( outerSourceCount == 0 )
      return sourceModel;

    final IAxis[] sourceAxes = sourceModel.getAxes();

    try
    {
      final IAxis dateAxis = ObservationUtilities.findAxisByClass( sourceAxes, Date.class );

      // Always use request/full range for the target
      Date targetBegin = null;
      Date targetEnd = null;

      // If transformation start/end are null:
      // 1. use from/to from request
      // 2. use first/last from base observation
      Date transformBegin = m_range.getFrom();
      Date transformEnd = m_range.getTo();

      // try to assume from request if needed
      if( args != null && args.getDateRange() != null )
      {
        final DateRange dateRange = args.getDateRange();
        if( targetBegin == null && dateRange.getFrom() != null )
          targetBegin = dateRange.getFrom();
        if( targetEnd == null && dateRange.getTo() != null )
          targetEnd = dateRange.getTo();
      }
      // try to assume from base tuple model if needed
      if( targetBegin == null )
        targetBegin = (Date) sourceModel.get( 0, dateAxis );
      if( targetEnd == null )
        targetEnd = (Date) sourceModel.get( outerSourceCount - 1, dateAxis );

      if( transformBegin == null )
        transformBegin = targetBegin;
      if( transformEnd == null )
        transformEnd = targetEnd;

      final int sourceIndexBegin = ObservationUtilities.findNextIndexForDate( sourceModel, dateAxis, targetBegin, 0, outerSourceCount );
      final int sourceIndexEnd = ObservationUtilities.findNextIndexForDate( sourceModel, dateAxis, targetEnd, sourceIndexBegin, outerSourceCount );

      if( sourceIndexEnd > outerSourceCount - 1 )
      {
        System.out.println( Messages.getString( "org.kalypso.ogc.sensor.timeseries.envelope.TranProLinFilter.2" ) ); //$NON-NLS-1$
      }

      final int targetMaxRows = sourceIndexEnd - sourceIndexBegin + 1;

      // sort axis
// final List<IAxis> axesListToCopy = new ArrayList<IAxis>();
// final List<IAxis> axesListToTransform = new ArrayList<IAxis>();
// final List<IAxis> axesListStatus = new ArrayList<IAxis>();
// for( final IAxis axis : sourceAxes )
// {
// if( axis.getDataClass() == Date.class ) // always copy date axis
// continue;
// else if( KalypsoStatusUtils.isStatusAxis( axis ) )
// axesListStatus.add( axis );
// else if( m_axisType.equals( axis.getType() ) )
// axesListToTransform.add( axis );
// else
// axesListToCopy.add( axis ); // copy axis
// }

// final IAxis[] axesStatus = axesListStatus.toArray( new IAxis[axesListStatus.size()] );
// final IAxis[] axesCopy = axesListToCopy.toArray( new IAxis[axesListToCopy.size()] );
// final IAxis[] axesTransform = axesListToTransform.toArray( new IAxis[axesListToTransform.size()] );
// final Date[] targetDates = new Date[targetMaxRows];
// for( int row = 0; row < targetMaxRows; row++ )
// targetDates[row] = (Date) sourceModel.get( row + sourceIndexBegin, dateAxis );

      final SimpleTupleModel targetModel = new SimpleTupleModel( m_axes, new Object[targetMaxRows][m_axes.length] );

      performTransformation( sourceModel, dateAxis, transformBegin, transformEnd, sourceIndexBegin, sourceIndexEnd, targetModel );

      return targetModel;
    }
    catch( final Exception e )
    {
      // TODO: always gets here, even if only one value cannot be computed
      // better catch exceptions individually?

      e.printStackTrace();
      final Logger logger = Logger.getLogger( getClass().getName() );
      logger.log( Level.WARNING, Messages.getString( "org.kalypso.ogc.sensor.timeseries.envelope.TranProLinFilter.1" ), e ); //$NON-NLS-1$
      return sourceModel;
    }
  }

  private IAxis[] buildTargetAxes( final IAxis[] sourceAxes )
  {
    final Collection<IAxis> targetAxes = new ArrayList<IAxis>();

    /* persistable axes */
    for( final IAxis axis : sourceAxes )
    {
      final boolean isSource = AxisUtils.isDataSrcAxis( axis );
      final boolean isStatus = AxisUtils.isStatusAxis( axis );
      final boolean isPersistable = axis.isPersistable();

      final boolean isValues = isPersistable & !isSource & !isStatus;
      if( isValues )
      {
        final String type = axis.getType();
        if( type.equals( ITimeseriesConstants.TYPE_RUNOFF ) || type.equals( ITimeseriesConstants.TYPE_WATERLEVEL ) )
          continue;

        addTargetAxes( sourceAxes, targetAxes, axis );
      }
    }

    /* THE target axis */
    final IAxis targetAxis = AxisUtils.findAxis( sourceAxes, m_axisType );
    addTargetAxes( sourceAxes, targetAxes, targetAxis );

    return targetAxes.toArray( new IAxis[targetAxes.size()] );
  }

  private void addTargetAxes( final IAxis[] sourceAxes, final Collection<IAxis> targetAxes, final IAxis axis )
  {
    addTargetAxis( targetAxes, axis );
    
    final IAxis statusAxis = AxisUtils.findStatusAxis( sourceAxes, axis );
    if( statusAxis != null )
      addTargetAxis( targetAxes, statusAxis );
    
    final IAxis sourceAxis = AxisUtils.findDataSourceAxis( sourceAxes, axis );
    if( sourceAxis != null )
      addTargetAxis( targetAxes, sourceAxis );
    else
    {
      final IAxis newSourceAxis = DataSourceHelper.createSourceAxis( axis );
      addTargetAxis( targetAxes, newSourceAxis );
    }
  }

  private void addTargetAxis( final Collection<IAxis> targetAxes, final IAxis axis )
  {
    // REMARK: make all axis persistable, it might be not, because of W/Q
    final DefaultAxis persistableAxis = new DefaultAxis( axis.getName(), axis.getType(), axis.getUnit(), axis.getDataClass(), axis.isKey(), true );
    targetAxes.add( persistableAxis );
  }

  private void performTransformation( final ITupleModel sourceModel, final IAxis dateAxis, final Date dateBegin, final Date dateEnd, final int sourceIndexBegin, final int sourceIndexEnd, final ITupleModel targetModel ) throws SensorException
  {
    final IAxis[] targetAxes = targetModel.getAxes();

    final long distTime = dateEnd.getTime() - dateBegin.getTime();
    final double deltaOperand = m_operandEnd - m_operandBegin;

    // iterate second time to perform transformation
    int targetRow = 0;
    for( int sourceRow = sourceIndexBegin; sourceRow < sourceIndexEnd + 1; sourceRow++ )
    {
      // transform: important to set transformed last, as there may be dependencies to other axes
      // (e.g. WQ-Transformation)

      final Date date = (Date) sourceModel.get( sourceRow, dateAxis );

      final long hereTime = date.getTime() - dateBegin.getTime();
      final double hereCoeff = m_operandBegin + deltaOperand * hereTime / distTime;

      for( final IAxis axis : targetAxes )
      {
        final Object targetValue = getTargetValue( sourceModel, sourceRow, axis, date, dateBegin, dateEnd, hereCoeff );
        targetModel.set( targetRow, axis, targetValue );
      }
      targetRow++;
    }
  }

  private Object getTargetValue( final ITupleModel sourceModel, final int sourceRow, final IAxis axis, final Date date, final Date dateBegin, final Date dateEnd, final double hereCoeff ) throws SensorException
  {
    final String type = axis.getType();

    if( type.equals( m_axisType ) )
    {
      final double currentValue = ((Number) sourceModel.get( sourceRow, axis )).doubleValue();
      final double changedValue;

      // We do only transform within the specified interval
      if( date.before( dateBegin ) || date.after( dateEnd ) )
        changedValue = currentValue;
      else
      {
        final double[] operands = new double[] { currentValue, hereCoeff };

        changedValue = m_operation.calculate( operands );
      }

      final double checkedValue = checkValue( type, changedValue );
      return new Double( checkedValue );
    }
    else if(  AxisUtils.isDataSrcAxis( axis ) )
    {
      return m_dataSourceIndex;
    }
    else
    {
      return sourceModel.get( sourceRow, axis );
    }
  }

  /**
   * HACK we use this method to make some sanity checks here depending on the type of the axis. <b>Probably this should
   * better be a parameter to this filter?
   */
  private double checkValue( final String axisType, final double value )
  {
    // Prohibit negative value for runoff
    if( ITimeseriesConstants.TYPE_RUNOFF.equals( axisType ) )
      return Math.max( 0.0, value );

    return value;
  }
}
