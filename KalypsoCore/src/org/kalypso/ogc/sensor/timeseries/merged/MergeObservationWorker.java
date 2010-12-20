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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;

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

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final List<IStatus> statis = new ArrayList<IStatus>();

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
        final String msg = String.format( "Merging observation \"%s\" failed", srcObservation.getHref() );
        statis.add( StatusUtilities.createStatus( IStatus.ERROR, msg, t ) );
      }
    }

    updateWqTable();

    m_result = new SimpleObservation( m_href, m_href, m_metadata, baseModel );

    return StatusUtilities.createStatus( statis, "Combing tuple models." );
  }

  /**
   * w/q tables of wiski time series will be generated on the fly (getValue() - the table is daterange related) - so
   * update our local w/q table with each request base observation
   */
  private void updateWqTable( )
  {
    // FIXME: this code does not belong here...

    final String wqTable = MetadataHelper.getWqTable( m_metadata );
    if( wqTable == null )
    {
      for( final ObservationSource source : m_sources )
      {
        final MetadataList metadata = source.getObservation().getMetadataList();
        final String localTable = MetadataHelper.getWqTable( metadata );
        if( localTable != null )
        {
          MetadataHelper.setWqTable( m_metadata, localTable );
          break;
        }
      }
    }
  }

  private Object[][] getData( final IObservation srcObservation, final ITupleModel srcModel ) throws SensorException
  {
    final List<Object[]> data = new ArrayList<Object[]>();

    final AxisMapping mapping = new AxisMapping( m_axes, srcModel.getAxisList() );
    final IAxis[] srcAxes = mapping.getSourceAxes();

    final DataSourceHandler destMetaDataHandler = new DataSourceHandler( m_metadata );

    final IAxis srcDataSourceAxis = AxisUtils.findDataSourceAxis( srcAxes );

    final DataSourceHandler srcMetaDataHandler = srcDataSourceAxis == null ? null : new DataSourceHandler( srcObservation.getMetadataList() );

    /* If we have no sourceAxis, create fixed dataSource */
    final String href = srcObservation.getHref();
    final int defaultDataSourceIndex = srcDataSourceAxis == null ? destMetaDataHandler.addDataSource( href, href ) : -1;

    for( int index = 0; index < srcModel.size(); index++ )
    {
      if( !m_strategy.process( srcModel, index, srcAxes ) )
        continue;

      final Object[] destValues = new Object[m_axes.length];
      for( int i = 0; i < destValues.length; i++ )
      {
        final IAxis destAxis = m_axes[i];
        final IAxis srcAxis = mapping.getSourceAxis( destAxis );
        destValues[i] = getDestValue( srcObservation, srcModel, destMetaDataHandler, srcMetaDataHandler, defaultDataSourceIndex, index, srcAxis, destAxis );
      }

      data.add( destValues );
    }

    return data.toArray( new Object[][] {} );
  }

  private Object getDestValue( final IObservation srcObservation, final ITupleModel srcModel, final DataSourceHandler destMetaDataHandler, final DataSourceHandler srcMetaDataHandler, final int dataSourceIndex, final int index, final IAxis srcAxis, final IAxis destAxis ) throws SensorException
  {
    if( AxisUtils.isDataSrcAxis( destAxis ) )
    {
      if( srcAxis == null )
        return dataSourceIndex;

      return getDestDataSource( srcObservation, srcModel, srcAxis, index, srcMetaDataHandler, destMetaDataHandler );
    }

    final Object srcValue = srcAxis == null ? null : srcModel.get( index, srcAxis );

    /* Special handling for status axes */
    if( AxisUtils.isStatusAxis( destAxis ) )
    {
      /* Return OK if this source has no status */
      if( !(srcValue instanceof Number) )
        return KalypsoStati.BIT_OK;

      /* Clear the derived bit in all cases */
      final Number srcStatusNumber = (Number) srcValue;
      final int srcStatus = srcStatusNumber.intValue();
      return srcStatus & ~( KalypsoStati.BIT_DERIVATED | KalypsoStati.BIT_DERIVATION_ERROR );
    }

    return srcValue;
  }

  private Object getDestDataSource( final IObservation srcObservation, final ITupleModel srcModel, final IAxis srcAxis, final int index, final DataSourceHandler srcMetaDataHandler, final DataSourceHandler destMetaDataHandler ) throws SensorException
  {
    final Number srcIndex = (Number) srcModel.get( index, srcAxis );
    if( srcIndex == null )
    {
      System.out.println( String.format( "Fallback: Found missing source reference.\nSource observation href: %s", srcObservation.getHref() ) );
      return destMetaDataHandler.addDataSource( IDataSourceItem.SOURCE_UNKNOWN, IDataSourceItem.SOURCE_UNKNOWN );
    }

    final String identifier = srcMetaDataHandler.getDataSourceIdentifier( srcIndex.intValue() );
    final String repository = srcMetaDataHandler.getDataSourceRepository( srcIndex.intValue() );
    return destMetaDataHandler.addDataSource( identifier, repository );
  }

  public IObservation getObservation( )
  {
    return m_result;
  }

}
