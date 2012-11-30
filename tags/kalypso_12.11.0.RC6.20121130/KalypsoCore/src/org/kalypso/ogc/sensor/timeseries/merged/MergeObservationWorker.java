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
package org.kalypso.ogc.sensor.timeseries.merged;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.metadata.MetadataWQTable;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * merges multiple observation sources to one single result observation.
 *
 * @author Dirk Kuch
 */
public class MergeObservationWorker implements ICoreRunnableWithProgress
{

  private static final Comparator<ObservationSource> COMPARATOR = new Comparator<ObservationSource>()
  {
    @Override
    public int compare( final ObservationSource source1, final ObservationSource source2 )
    {
      final Date date1 = getDate( source1 );
      final Date date2 = getDate( source2 );

      if( date1 == null && date2 != null )
        return -1;
      else if( date1 != null && date2 == null )
        return 1;
      else if( date1 == null && date2 == null )
        return 0;

      return date1.compareTo( date2 );
    }

    private Date getDate( final ObservationSource source )
    {
      final DateRange dateRange = source.getDateRange();
      if( dateRange == null )
        return null;

      return dateRange.getFrom();
    }
  };

  private final ObservationSource[] m_sources;

  private final IAxis[] m_axes;

  private final MetadataList m_metadata;

  private final IObservationMergeStrategy m_strategy;

  private final String m_href;

  private SimpleObservation m_result;

  public MergeObservationWorker( final String href, final ObservationSource[] sources, final IAxis[] axes, final MetadataList metadata )
  {
    this( href, sources, axes, metadata, new LastDateObservationMergeStrategy() );
  }

  public MergeObservationWorker( final String href, final ObservationSource[] sources, final IAxis[] axes, final MetadataList metadata, final IObservationMergeStrategy strategy )
  {
    m_href = href;
    m_sources = sources;
    m_axes = axes;
    m_metadata = metadata;
    m_strategy = strategy;

    Arrays.sort( m_sources, COMPARATOR );
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final List<IStatus> statis = new ArrayList<>();

    final SimpleTupleModel baseModel = new SimpleTupleModel( m_axes );

    for( final ObservationSource source : m_sources )
    {
      final IObservation srcObservation = source.getObservation();

      try
      {
        final ITupleModel srcModel = srcObservation.getValues( new ObservationRequest( source.getDateRange() ) );
        final Object[][] data = getData( srcObservation, srcModel );

        for( final Object[] values : data )
        {
          baseModel.addTuple( values );
        }
      }
      catch( final Throwable t )
      {
        final String msg = String.format( Messages.getString("MergeObservationWorker_0"), srcObservation.getHref() ); //$NON-NLS-1$
        final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, msg, t );
        // Log status here, because it is never seen again...
        KalypsoCorePlugin.getDefault().getLog().log( status );
        statis.add( status );
      }
    }

    updateWqTable();

    m_result = new SimpleObservation( m_href, m_href, m_metadata, baseModel );

    return StatusUtilities.createStatus( statis, Messages.getString("MergeObservationWorker_1") ); //$NON-NLS-1$
  }

  /**
   * w/q tables of wiski time series will be generated on the fly (getValue() - the table is daterange related) - so
   * update our local w/q table with each request base observation
   */
  private void updateWqTable( )
  {
    // FIXME: this code does not belong here...

    final String wqTable = MetadataHelper.getWqTable( m_metadata );
    if( Strings.isEmpty( wqTable ) )
    {
      for( final ObservationSource source : m_sources )
      {
        final MetadataList metadata = source.getObservation().getMetadataList();
        if( MetadataWQTable.updateWqTable( m_metadata, metadata ) )
          break;
      }
    }
  }

  private Object[][] getData( final IObservation srcObservation, final ITupleModel srcModel ) throws SensorException
  {
    final List<Object[]> data = new ArrayList<>();

    final AxisMapping mapping = new AxisMapping( m_axes, srcModel.getAxes() );
    final IAxis[] srcAxes = mapping.getSourceAxes();

    final DataSourceHandler destMetaDataHandler = new DataSourceHandler( m_metadata );
    for( int index = 0; index < srcModel.size(); index++ )
    {
      if( !m_strategy.process( srcModel, index, srcAxes ) )
        continue;

      final Object[] targetValues = new Object[m_axes.length];
      for( int i = 0; i < targetValues.length; i++ )
      {
        final IAxis targetAxis = m_axes[i];
        final IAxis sourceAxis = mapping.getSourceAxis( targetAxis );

        if( AxisUtils.isDataSrcAxis( targetAxis ) ) // special handling for data source axes!
        {
          final String source = findDataSource( srcObservation, srcModel, sourceAxis, index );
          final int sourceIndex = destMetaDataHandler.addDataSource( source, source );

          targetValues[i] = sourceIndex;
        }
        else
        {
          targetValues[i] = getDestValue( srcModel, index, sourceAxis, targetAxis );
        }
      }

      // REMARK: prohibit adding corrupt data -> may result in an empty data set
      if( !ArrayUtils.contains( targetValues, null ) )
        data.add( targetValues );
    }

    return data.toArray( new Object[][] {} );
  }

  private String findDataSource( final IObservation srcObservation, final ITupleModel srcModel, final IAxis sourceAxis, final int index )
  {
    if( sourceAxis == null )
      return srcObservation.getHref();

    try
    {
      final Object value = srcModel.get( index, sourceAxis );
      if( value instanceof Number )
      {
        final Number srcIndex = (Number) value;

        final DataSourceHandler handler = new DataSourceHandler( srcObservation.getMetadataList() );
        final String dataSourceIdentifier = handler.getDataSourceIdentifier( srcIndex.intValue() );
        if( StringUtils.isNotEmpty( dataSourceIdentifier ) )
          return dataSourceIdentifier;
      }
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    return srcObservation.getHref();
  }

  private Object getDestValue( final ITupleModel srcModel, final int index, final IAxis srcAxis, final IAxis destAxis ) throws SensorException
  {
    final Object srcValue = srcAxis == null ? null : srcModel.get( index, srcAxis );

    if( srcValue == null )
    {
      final String type = destAxis.getType();
      if( ITimeseriesConstants.TYPE_WECHMANN_E.equals( type ) || ITimeseriesConstants.TYPE_WECHMANN_SCHALTER_V.equals( type ) )
      {
        // should never happen: trace origin of problem!!!
        return 0.0;
      }
    }

    /* Special handling for status axes */
    if( AxisUtils.isStatusAxis( destAxis ) )
    {
      /* Return OK if this source has no status */
      if( !(srcValue instanceof Number) )
        return KalypsoStati.BIT_OK;

      /* Clear the derived bit in all cases */
      final Number srcStatusNumber = (Number) srcValue;
      final int srcStatus = srcStatusNumber.intValue();
      return srcStatus & ~(KalypsoStati.BIT_DERIVATED | KalypsoStati.BIT_DERIVATION_ERROR);
    }

    return srcValue;
  }

  public IObservation getObservation( )
  {
    return m_result;
  }

}
