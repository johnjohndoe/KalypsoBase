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
package org.kalypso.ogc.sensor.timeseries.base;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.visitor.ITupleModelValueContainer;
import org.kalypso.ogc.sensor.visitor.ITupleModelVisitor;

import com.google.common.collect.Iterables;

/**
 * @author Dirk Kuch
 */
public class CacheTimeSeriesVisitor implements ITupleModelVisitor, ITimeseriesCache
{
  private final TreeMap<Date, TupleModelDataSet[]> m_values = new TreeMap<>();

  private final String m_source;

  private final MetadataList m_metadata;

  public CacheTimeSeriesVisitor( )
  {
    m_metadata = null;
    m_source = null;
  }

  public CacheTimeSeriesVisitor( final MetadataList metadata )
  {
    m_metadata = metadata;
    m_source = null;
  }

  public CacheTimeSeriesVisitor( final String source )
  {
    m_source = source;
    m_metadata = null;
  }

  @Override
  public TreeMap<Date, TupleModelDataSet[]> getValueMap( )
  {
    return m_values;
  }

  @Override
  public DatedDataSets[] getValues( )
  {
    final Set<DatedDataSets> sets = new LinkedHashSet<>();

    final Set<Entry<Date, TupleModelDataSet[]>> entries = m_values.entrySet();
    for( final Entry<Date, TupleModelDataSet[]> entry : entries )
    {
      sets.add( new DatedDataSets( entry.getKey(), entry.getValue() ) );
    }

    return sets.toArray( new DatedDataSets[] {} );
  }

  @Override
  public void visit( final ITupleModelValueContainer container )
  {
    try
    {
      final Date date = (Date) container.get( getDateAxis( container ) );

      final Set<TupleModelDataSet> sets = new LinkedHashSet<>();

      final IAxis[] axes = container.getAxes();
      final IAxis[] valueAxes = AxisUtils.findValueAxes( axes );
      for( final IAxis valueAxis : valueAxes )
      {
        final Number value = (Number) container.get( valueAxis );
        final Integer status = getStatus( container, valueAxis );

        sets.add( new TupleModelDataSet( valueAxis, value, status, getSource( container, valueAxis ) ) );
      }

      m_values.put( date, sets.toArray( new TupleModelDataSet[] {} ) );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private String getSource( final ITupleModelValueContainer container, final IAxis valueAxis ) throws SensorException
  {
    if( Objects.isNotNull( m_source ) )
      return m_source;

    if( Objects.isNull( m_metadata ) )
      return StringUtils.EMPTY;

    final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( container.getAxes(), valueAxis );
    if( Objects.isNull( dataSourceAxis ) )
      return StringUtils.EMPTY;

    final DataSourceHandler handler = new DataSourceHandler( m_metadata );
    final Object value = container.get( dataSourceAxis );
    if( !(value instanceof Number) )
      return StringUtils.EMPTY;

    return handler.getDataSourceIdentifier( ((Number) value).intValue() );
  }

  private Integer getStatus( final ITupleModelValueContainer container, final IAxis valueAxis ) throws SensorException
  {
    final IAxis statusAxis = AxisUtils.findStatusAxis( container.getAxes(), valueAxis );
    if( Objects.isNotNull( statusAxis ) )
    {
      final Object status = container.get( statusAxis );
      if( status instanceof Number )
        return ((Number) status).intValue();
    }

    return KalypsoStati.BIT_OK;
  }

  private IAxis getDateAxis( final ITupleModelValueContainer container )
  {
    return AxisUtils.findDateAxis( container.getAxes() );
  }

  @Override
  public TupleModelDataSet[] getValue( final Date date )
  {
    return m_values.get( date );
  }

  @Override
  public TupleModelDataSet getValue( final Date date, final IAxis valueAxis )
  {
    final TupleModelDataSet[] dataSets = m_values.get( date );
    if( ArrayUtils.isEmpty( dataSets ) )
      return null;

    for( final TupleModelDataSet dataset : dataSets )
    {
      if( dataset.getValueAxis().equals( valueAxis ) )
        return dataset;
    }

    return null;
  }

  public TupleModelDataSet getValue( final Date date, final String axis )
  {
    final TupleModelDataSet[] datasets = m_values.get( date );
    if( ArrayUtils.isEmpty( datasets ) )
      return null;
    for( final TupleModelDataSet dataset : datasets )
    {
      final String dataSetAxis = dataset.getValueAxis().getType();
      if( StringUtils.equalsIgnoreCase( dataSetAxis, axis ) )
        return dataset;
    }

    return null;
  }

  public TupleModelDataSet findValueBefore( final IAxis axis, final Date date )
  {
    TupleModelDataSet ptr = null;

    final Set<Entry<Date, TupleModelDataSet[]>> entries = m_values.entrySet();
    for( final Entry<Date, TupleModelDataSet[]> entry : entries )
    {
      final Date key = entry.getKey();
      if( key.before( date ) )
      {
        final TupleModelDataSet[] values = entry.getValue();
        for( final TupleModelDataSet value : values )
        {
          if( AxisUtils.isEqual( value.getValueAxis(), axis ) )
          {
            ptr = value;
            break;
          }
        }
      }
      else
        break;
    }

    return ptr;
  }

  public TupleModelDataSet findValueAfter( final IAxis axis, final Date date )
  {
    final Set<Entry<Date, TupleModelDataSet[]>> entries = m_values.entrySet();
    for( final Entry<Date, TupleModelDataSet[]> entry : entries )
    {
      final Date key = entry.getKey();
      if( key.after( date ) )
      {
        final TupleModelDataSet[] values = entry.getValue();
        for( final TupleModelDataSet value : values )
        {
          if( AxisUtils.isEqual( value.getValueAxis(), axis ) )
          {
            return value;
          }
        }
      }
    }

    return null;
  }

  @Override
  public DateRange getDateRange( )
  {
    final Set<Date> keys = m_values.keySet();
    if( keys.isEmpty() )
      return null;

    final Date from = Iterables.getFirst( keys, null );
    final Date to = Iterables.getLast( keys, null );

    return new DateRange( from, to );
  }

  public static ITimeseriesCache cache( final IObservation observation ) throws SensorException
  {
    final ITupleModel model = observation.getValues( null );
    final CacheTimeSeriesVisitor visitor = new CacheTimeSeriesVisitor( observation.getMetadataList() );
    model.accept( visitor, 1 );

    return visitor;
  }
}
