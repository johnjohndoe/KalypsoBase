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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.AxisSet;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.event.ObservationChangeType;
import org.kalypso.ogc.sensor.impl.AbstractTupleModel;
import org.kalypso.ogc.sensor.impl.ITupleModelChangeListener;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.repository.IDataSourceItem;

/**
 * The WQTuppleModel computes W, Q, V, etc. on the fly, depending on the underlying axis type. It also manages the
 * status information for the generated axis, depending on the success of the computation.
 * 
 * @author schlienger
 */
public class WQTuppleModel extends AbstractTupleModel
{
  private static final Double ZERO = new Double( 0 );

  private final ITupleModel m_model;

  private final IAxis m_dateAxis;

  /** source axis from the underlying model */
  private final AxisSet m_source;

  /** generated axis */
  private final AxisSet m_target;

  private final Map<Integer, TupleModelDataSet> m_values = Collections.synchronizedMap( new HashMap<Integer, TupleModelDataSet>() );

  private final IWQConverter m_converter;

  private final MetadataList m_metadata;

  /**
   * Creates a <code>WQTuppleModel</code> that can generate either W or Q on the fly. It needs an existing model from
   * whitch the values of the given type are fetched.
   * <p>
   * If it bases on a TimeserieConstants.TYPE_RUNOFF it can generate TYPE_WATERLEVEL values and vice versa.
   * 
   * @param model
   *          base model delivering values of the given type
   * @param metadata
   *          needed for handling of data sources
   * @param axes
   *          axes of this WQ-model, usually the same as model plus destAxis
   * @param src
   *          source value, status and datasource axes
   * @param target
   *          target value, status and datasource axes
   */
  public WQTuppleModel( final ITupleModel model, final MetadataList metadata, final IAxis[] axes, final AxisSet src, final AxisSet target, final IWQConverter converter )
  {
    super( axes );
    m_metadata = metadata;

    m_dateAxis = AxisUtils.findDateAxis( axes );
    m_source = src;
    m_target = target;

    mapAxisToPos( m_target.getValueAxis(), ArrayUtils.indexOf( axes, m_target.getValueAxis() ) );
    mapAxisToPos( m_target.getStatusAxis(), ArrayUtils.indexOf( axes, m_target.getStatusAxis() ) );
    mapAxisToPos( m_target.getDatasourceAxis(), ArrayUtils.indexOf( axes, m_target.getDatasourceAxis() ) );

    m_model = model;
    m_model.addChangeListener( new ITupleModelChangeListener()
    {
      @Override
      public void modelChangedEvent( final ObservationChangeType type )
      {
        fireModelChanged( type.getEvent() );
      }
    } );

    m_converter = converter;

  }

  public AxisSet getSourceAxes( )
  {
    return m_source;
  }

  public AxisSet getTargetAxes( )
  {
    return m_target;
  }

  public MetadataList getMetadata( )
  {
    return m_metadata;
  }

  @Override
  public int size( ) throws SensorException
  {
    return m_model.size();
  }

  @Override
  public Object get( final int index, final IAxis axis ) throws SensorException
  {
    if( axis == null )
      return null;

    final Integer objIndex = Integer.valueOf( index );

    if( m_target.hasAxis( axis ) )
    {
      if( !m_values.containsKey( objIndex ) )
      {
        final TupleModelDataSet dataSet = read( index );
        m_values.put( objIndex, dataSet );
      }

      final TupleModelDataSet data = m_values.get( objIndex );

      if( AxisUtils.isEqual( m_target.getValueAxis(), axis ) )
      {
        return data.getValue();
      }
      else if( AxisUtils.isEqual( m_target.getStatusAxis(), axis ) )
      {
        return data.getStatus();
      }
      else if( AxisUtils.isEqual( m_target.getDatasourceAxis(), axis ) )
      {
        final DataSourceHandler handler = new DataSourceHandler( m_metadata );
        return handler.addDataSource( data.getSource(), data.getSource() );
      }
    }

    return m_model.get( index, axis );
  }

  private TupleModelDataSet read( final int index ) throws SensorException
  {
    final Number srcValue = (Number) m_model.get( index, m_source.getValueAxis() );

    if( Objects.isNull( srcValue ) )
      return new TupleModelDataSet( m_target.getValueAxis(), srcValue, KalypsoStati.BIT_CHECK, IDataSourceItem.SOURCE_MISSING );

    final String type = m_target.getValueAxis().getType();
    final int status = readStatus( index );
    final String source = getTargetDataSource( index );

    try
    {
      if( type.equals( m_converter.getFromType() ) )
      {
        final double q = srcValue.doubleValue();
        final double w = m_converter.computeW( m_model, index, q );

        return new TupleModelDataSet( m_target.getValueAxis(), w, status, source );
      }
      else if( type.equals( m_converter.getToType() ) )
      {
        final double w = srcValue.doubleValue();
        final double q = m_converter.computeQ( m_model, index, w );

        return new TupleModelDataSet( m_target.getValueAxis(), q, status, source );
      }

      return new TupleModelDataSet( m_target.getValueAxis(), ZERO, KalypsoStati.STATUS_DERIVATION_ERROR, IDataSourceItem.SOURCE_MISSING );
    }
    catch( final WQException e )
    {
      return new TupleModelDataSet( m_target.getValueAxis(), ZERO, KalypsoStati.STATUS_DERIVATION_ERROR, IDataSourceItem.SOURCE_MISSING );
    }
  }

  private int readStatus( final int index ) throws SensorException
  {
    final IAxis statusAxis = m_source.getStatusAxis();
    if( statusAxis == null )
      return KalypsoStati.BIT_CHECK;

    final Number srcStatus = (Number) m_model.get( index, statusAxis );

    return KalypsoStati.STATUS_DERIVATED | (srcStatus == null ? KalypsoStati.BIT_CHECK : srcStatus.intValue());
  }

  private String getTargetDataSource( final int index ) throws SensorException
  {
    final IAxis datasourceAxis = m_source.getDatasourceAxis();
    if( Objects.isNull( datasourceAxis ) )
      return IDataSourceItem.SOURCE_UNKNOWN;

    final Number srcDataSource = (Number) m_model.get( index, datasourceAxis );
    if( srcDataSource == null )
      return IDataSourceItem.SOURCE_UNKNOWN;

    final DataSourceHandler handler = new DataSourceHandler( m_metadata );
    return handler.getDataSourceIdentifier( srcDataSource.intValue() );
  }

  @Override
  public void set( final int index, final IAxis axis, final Object element ) throws SensorException
  {
    final Integer objIndex = new Integer( index );
    m_values.remove( objIndex ); // remove updated value from cache

    if( m_target.hasAxis( axis ) )
    {
      try
      {
        if( AxisUtils.isEqual( m_target.getValueAxis(), axis ) )
        {
          final Double srcValue = toSourceValue( axis, index, (Number) element );
          m_model.set( index, m_source.getValueAxis(), srcValue );
        }
        else if( AxisUtils.isEqual( m_target.getStatusAxis(), axis ) )
        {
          Number status = (Number) element;
          if( (status.intValue() & KalypsoStati.BIT_DERIVATED) != 0 )
            status = status.intValue() - KalypsoStati.BIT_DERIVATED;

          m_model.set( index, m_source.getStatusAxis(), status );
        }
        else if( AxisUtils.isEqual( m_target.getDatasourceAxis(), axis ) )
        {
          m_model.set( index, m_source.getDatasourceAxis(), element );
        }
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }
    else
    {
      m_model.set( index, axis, element );
    }

  }

  private Double toSourceValue( final IAxis axis, final int index, final Number element ) throws WQException, SensorException
  {
    final String type = axis.getType();

    if( m_converter == null )
      return ZERO;
    else if( type.equals( m_converter.getFromType() ) )
    {
      final double w = element.doubleValue();
      return new Double( m_converter.computeQ( m_model, index, w ) );
    }
    else if( type.equals( m_converter.getToType() ) )
    {
      final double q = element.doubleValue();
      return new Double( m_converter.computeW( m_model, index, q ) );
    }

    return ZERO;
  }

  @Override
  public int indexOf( final Object element, final IAxis axis ) throws SensorException
  {
    if( m_target.hasAxis( axis ) )
      return -1; // indexOf only makes sense for key axes

    return m_model.indexOf( element, axis );
  }

  public IAxis getDateAxis( )
  {
    return m_dateAxis;
  }

  /**
   * Creates a TuppleModel from a potential WQTuppleModel for storing the values back in the original observation.
   * 
   * @throws SensorException
   */
  public static ITupleModel reverse( final ITupleModel values, final IAxis[] axes ) throws SensorException
  {
    final SimpleTupleModel stm = new SimpleTupleModel( axes );

    for( int i = 0; i < values.size(); i++ )
    {
      final Object[] tupple = new Object[axes.length];

      // straighforward: simply take the values for the axes of the original
      // observation, not the generated W/Q
      for( int j = 0; j < axes.length; j++ )
        tupple[stm.getPosition( axes[j] )] = values.get( i, axes[j] );

      stm.addTuple( tupple );
    }

    return stm;
  }

  public IWQConverter getConverter( )
  {
    return m_converter;
  }

  /**
   * @return the base model
   */
  public ITupleModel getBaseModel( )
  {
    return m_model;
  }
}