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
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.ObservationRequest;
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
        final IAxis[] srcAxes = srcModel.getAxisList();

        Object[][] data;
        if( AxisUtils.findDataSourceAxis( srcAxes ) == null )
        {
          data = getFromSourcelessData( srcObservation.getHref(), srcModel );
        }
        else
        {
          data = getFromSourceData( srcObservation, srcModel );
        }

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

    m_result = new SimpleObservation( m_href, m_href, m_metadata, baseModel );

    return StatusUtilities.createStatus( statis, "Combing tuple models." );
  }

  private Object[][] getFromSourceData( final IObservation srcObservation, final ITupleModel srcModel ) throws SensorException
  {
    final List<Object[]> data = new ArrayList<Object[]>();

    final AxisMapping mapping = new AxisMapping( m_axes, srcModel.getAxisList() );
    final IAxis[] srcAxes = mapping.getSourceAxes();

    final DataSourceHandler srcMetaDataHandler = new DataSourceHandler( srcObservation.getMetadataList() );
    final DataSourceHandler destMetaDataHandler = new DataSourceHandler( m_metadata );

    for( int index = 0; index < srcModel.getCount(); index++ )
    {
      try
      {
        if( !m_strategy.process( srcModel, index, srcAxes ) )
          continue;

        final Object[] values = new Object[srcAxes.length];

        for( final IAxis srcAxis : srcAxes )
        {
          final int baseIndex = mapping.getBaseIndex( srcAxis );

          /* adjust data src informations (add to metadata) */
          if( AxisUtils.isDataSrcAxis( srcAxis ) )
          {
            final Integer srcIndex = (Integer) srcModel.getElement( index, srcAxis );

            final String identifier;
            final String repository;

            if( srcIndex == null )
            {
              /** *grml* fallback - this should never happen! */
              identifier = IDataSourceItem.SOURCE_UNKNOWN;
              repository = IDataSourceItem.SOURCE_UNKNOWN;

              System.out.println( String.format( "Fallback: %s - found missing source reference.\nSource observation href: %s", this.getClass().getName(), srcObservation.getHref() ) );
            }
            else
            {
              identifier = srcMetaDataHandler.getDataSourceIdentifier( srcIndex );
              repository = srcMetaDataHandler.getDataSourceRepository( srcIndex );
            }

            final Integer destIndex = destMetaDataHandler.addDataSource( identifier, repository );
            values[baseIndex] = destIndex;
          }
          else
          {
            final Object value = srcModel.getElement( index, srcAxis );
            values[baseIndex] = value;
          }
        }

        data.add( values );
      }
      catch( final Throwable t )
      {
        KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    return data.toArray( new Object[][] {} );
  }

  private Object[][] getFromSourcelessData( final String href, final ITupleModel srcModel ) throws SensorException
  {
    final List<Object[]> data = new ArrayList<Object[]>();

    final AxisMapping mapping = new AxisMapping( m_axes, srcModel.getAxisList() );
    final IAxis[] srcAxes = mapping.getSourceAxes();

    final DataSourceHandler handler = new DataSourceHandler( m_metadata );
    final int dataSourceIndex = handler.addDataSource( href, href );
    final int dataSourceValueIndex = mapping.getBaseIndex( mapping.getDataSourceAxis() );

    for( int index = 0; index < srcModel.getCount(); index++ )
    {
      try
      {
        if( !m_strategy.process( srcModel, index, srcAxes ) )
          continue;

        final Object[] values = new Object[srcAxes.length + 1]; // the data source axis!

        for( final IAxis srcAxis : srcAxes )
        {
          final Object value = srcModel.getElement( index, srcAxis );
          final int baseIndex = mapping.getBaseIndex( srcAxis );
          values[baseIndex] = value;
        }

        values[dataSourceValueIndex] = dataSourceIndex;

        data.add( values );
      }
      catch( final Throwable t )
      {
        KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    return data.toArray( new Object[][] {} );
  }

  public IObservation getObservation( )
  {
    return m_result;
  }

}
