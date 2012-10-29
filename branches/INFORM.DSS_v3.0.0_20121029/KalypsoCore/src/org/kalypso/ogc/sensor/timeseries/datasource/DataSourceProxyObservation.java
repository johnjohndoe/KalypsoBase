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
package org.kalypso.ogc.sensor.timeseries.datasource;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.event.IObservationListener;
import org.kalypso.ogc.sensor.event.ObservationChangeType;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.base.CacheTimeSeriesVisitor;
import org.kalypso.ogc.sensor.util.Observations;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;

/**
 * Data source proxy observation class. To add a data source axis to an model we have to create a new model. This is a
 * very slow operation, so we introduced this proxy class. the new model will be created by the first request.
 *
 * @author Dirk Kuch
 */
public class DataSourceProxyObservation implements IObservation
{
  private final IObservation m_observation;

  private final String m_itemIdentifier;

  private final String m_repositoryId;

  private MetadataList m_metadata;

  private final int m_defaultStatus;

  public DataSourceProxyObservation( final IObservation observation, final String itemIdentifier, final String repositoryId )
  {
    this( observation, itemIdentifier, repositoryId, KalypsoStati.BIT_OK );
  }

  public DataSourceProxyObservation( final IObservation observation, final String itemIdentifier, final String repositoryId, final int defaultStatus )
  {
    m_observation = observation;
    m_itemIdentifier = itemIdentifier;
    m_repositoryId = repositoryId;
    m_defaultStatus = defaultStatus;
  }

  @Override
  public synchronized MetadataList getMetadataList( )
  {
    if( m_metadata == null )
    {
      final MetadataList metadata = m_observation.getMetadataList();
      m_metadata = MetadataHelper.clone( metadata );

      final DataSourceHandler handler = new DataSourceHandler( m_metadata );
      if( handler.containsDataSourceReferences() )
        handler.removeAllDataSources();

      handler.addDataSource( m_itemIdentifier, m_repositoryId );
    }

    return m_metadata;
  }

  @Override
  public IAxis[] getAxes( )
  {
    if( isComplete() )
      return m_observation.getAxes();

    final IAxis[] base = m_observation.getAxes();

    final Set<IAxis> axes = new LinkedHashSet<>();
    Collections.addAll( axes, base );

    final IAxis[] valueAxes = AxisUtils.findValueAxes( base, false );
    for( final IAxis valueAxis : valueAxes )
    {
      /** status axis */
      final IAxis statusAxis = AxisUtils.findStatusAxis( base, valueAxis );
      if( Objects.isNull( statusAxis ) )
      {
        axes.add( KalypsoStatusUtils.createStatusAxisFor( valueAxis, true ) );
      }

      /** data source axis */
      final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( base, valueAxis );
      if( Objects.isNull( dataSourceAxis ) )
      {
        axes.add( DataSourceHelper.createSourceAxis( valueAxis ) );
      }

    }

    return axes.toArray( new IAxis[] {} );
  }

  @Override
  public ITupleModel getValues( final IRequest args ) throws SensorException
  {
    getMetadataList(); // necessary!

    final ITupleModel base = m_observation.getValues( args );

    if( isComplete() )
      return base;

    final CacheTimeSeriesVisitor visitor = new CacheTimeSeriesVisitor( m_itemIdentifier );
    base.accept( visitor, 1 );

    final IAxis[] target = getAxes();
    final SimpleTupleModel model = new SimpleTupleModel( target );

    final int dateAxisPosition = model.getPosition( AxisUtils.findDateAxis( target ) );

    final Map<Date, TupleModelDataSet[]> values = visitor.getValueMap();
    final Set<Entry<Date, TupleModelDataSet[]>> entrySet = values.entrySet();
    for( final Entry<Date, TupleModelDataSet[]> entry : entrySet )
    {
      final Object[] tuple = new Object[ArrayUtils.getLength( target )];
      tuple[dateAxisPosition] = entry.getKey();

      final TupleModelDataSet[] dataSets = entry.getValue();
      for( final TupleModelDataSet dataSet : dataSets )
      {
        final IAxis statusAxis = AxisUtils.findStatusAxis( target, dataSet.getValueAxis() );
        final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( target, dataSet.getValueAxis() );

        final int valueAxisPosition = model.getPosition( dataSet.getValueAxis() );
        final int statusAxisPosition = model.getPosition( statusAxis );
        final int dataSourceAxisPosition = model.getPosition( dataSourceAxis );

        tuple[valueAxisPosition] = dataSet.getValue();
        tuple[statusAxisPosition] = com.google.common.base.Objects.firstNonNull( dataSet.getStatus(), m_defaultStatus );
        tuple[dataSourceAxisPosition] = 0; // data source reference is always 0! (see getMetadataList())
      }

      model.addTuple( tuple );
    }

    return model;
  }

  private boolean isComplete( )
  {
    final IAxis[] baseAxes = m_observation.getAxes();

    final IAxis[] valueAxes = AxisUtils.findValueAxes( baseAxes, false );
    for( final IAxis valueAxis : valueAxes )
    {
      final IAxis statusAxis = AxisUtils.findStatusAxis( baseAxes, valueAxis );
      if( Objects.isNull( statusAxis ) )
        return false;

      final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( baseAxes, valueAxis );
      if( Objects.isNull( dataSourceAxis ) )
        return false;
    }

    return true;
  }

  @Override
  public void setValues( final ITupleModel values ) throws SensorException
  {
    m_observation.setValues( values );
  }

  @Override
  public void addListener( final IObservationListener listener )
  {
    m_observation.addListener( listener );
  }

  @Override
  public void removeListener( final IObservationListener listener )
  {
    m_observation.removeListener( listener );
  }

  @Override
  public void fireChangedEvent( final Object source, final ObservationChangeType type )
  {
    m_observation.fireChangedEvent( source, type );
  }

  @Override
  public String getName( )
  {
    return m_observation.getName();
  }

  @Override
  public String getHref( )
  {
    return m_observation.getHref();
  }

  @Override
  public void accept( final IObservationVisitor visitor, final IRequest request, final int direction ) throws SensorException
  {
    Observations.accept( this, visitor, request, direction );
  }

  @Override
  public boolean isEmpty( )
  {
    // TODO
    return false;
  }
}
