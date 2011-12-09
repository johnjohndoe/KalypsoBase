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
package org.kalypso.zml.core.table.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.model.data.IZmlModelColumnDataHandler;
import org.kalypso.zml.core.table.model.data.IZmlModelColumnDataListener;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.transaction.IZmlModelUpdateCommand;
import org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor;
import org.kalypso.zml.core.table.schema.DataColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlModelColumn implements IZmlModelColumn, IZmlModelColumnDataListener
{
  private final Set<IZmlModelColumnListener> m_listeners = new LinkedHashSet<IZmlModelColumnListener>();

  private final DataColumn m_type;

  private String m_label;

  private IZmlModelColumnDataHandler m_handler;

  private final String m_identifier;

  private final IZmlModel m_model;

  private String m_labelTokenizer;

  private boolean m_labeled = false;

  public ZmlModelColumn( final IZmlModel model, final String identifier, final DataColumn type )
  {
    m_model = model;
    m_identifier = identifier;
    m_type = type;
  }

  public ZmlModelColumn( final IZmlModel model, final DataColumn column )
  {
    this( model, column.getIdentifier(), column );
  }

  @Override
  public void addListener( final IZmlModelColumnListener listener )
  {
    m_listeners.add( listener );
  }

  @Override
  public void removeListener( final IZmlModelColumnListener listener )
  {
    m_listeners.remove( listener );
  }

  @Override
  public void setLabel( final String label )
  {
    m_label = label;
    m_labeled = true;
  }

  @Override
  public IZmlModelColumnDataHandler getDataHandler( )
  {
    return m_handler;
  }

  @Override
  public void setDataHandler( final IZmlModelColumnDataHandler handler )
  {
    synchronized( this )
    {
      m_type.reset();

      if( Objects.isNotNull( m_handler ) )
      {
        m_handler.removeListener( this );
        m_handler.dispose();
      }

      m_handler = handler;
      m_handler.addListener( this );

      fireColumnChanged();
    }
  }

  public void dispose( )
  {
    if( Objects.isNotNull( m_handler ) )
    {
      m_handler.removeListener( this );
      m_handler.dispose();

      fireColumnChanged();
    }

  }

  @Override
  public ITupleModel getTupleModel( ) throws SensorException
  {
    return m_handler.getModel();
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  @Override
  public Object get( final int index, final IAxis axis ) throws SensorException
  {
    if( axis == null )
      return null;

    return getTupleModel().get( index, axis );
  }

  private boolean isTargetAxis( final IAxis axis )
  {
    return axis.getType().equals( m_type.getValueAxis() );
  }

  @Override
  public int size( ) throws SensorException
  {
    return getTupleModel().size();
  }

  @Override
  public String toString( )
  {
    return getIdentifier();
  }

  @Override
  public void doUpdate( final int index, final Object value, final String source, final Integer status ) throws SensorException
  {
    update( index, value, source, status );

    fireColumnChangedEvent();
  }

  /**
   * @see org.kalypso.zml.core.table.model.IZmlModelColumn#doUpdate(org.kalypso.zml.core.table.model.transaction.IZmlModelUpdateCommand)
   */
  @Override
  public void doExecute( final IZmlModelUpdateCommand command ) throws SensorException
  {
    final IZmlValueReference target = command.getTarget();
    update( target.getModelIndex(), command.getValue(), command.getDataSource(), command.getStatus() );
  }

  private void update( final int index, final Object value, final String source, final Integer status ) throws SensorException
  {
    final ITupleModel model = getTupleModel();
    final IAxis[] axes = model.getAxes();

    for( final IAxis axis : axes )
    {
      if( AxisUtils.isDataSrcAxis( axis ) )
      {
        // FIXME - user modified triggered interpolated state?!?
        final DataSourceHandler handler = new DataSourceHandler( getMetadata() );
        final int sourceIndex;
        if( Objects.isNull( source ) )
          sourceIndex = handler.addDataSource( IDataSourceItem.SOURCE_UNKNOWN, IDataSourceItem.SOURCE_UNKNOWN );
        else
          sourceIndex = handler.addDataSource( source, source );

        model.set( index, axis, sourceIndex );
      }
      else if( AxisUtils.isStatusAxis( axis ) )
      {
        if( Objects.isNull( status ) )
          model.set( index, axis, KalypsoStati.BIT_OK );

        model.set( index, axis, status );
      }
      else if( isTargetAxis( axis ) )
      {
        if( Objects.isNull( value ) )
          model.set( index, axis, Double.NaN );

        model.set( index, axis, value );
      }
    }

  }

  @Override
  public MetadataList getMetadata( )
  {
    final IObservation observation = m_handler.getObservation();
    if( Objects.isNull( observation ) )
      return null;

    return observation.getMetadataList();
  }

  @Override
  public DataColumn getDataColumn( )
  {
    return m_type;
  }

  @Override
  public String getLabel( )
  {
    if( Strings.isEmpty( m_label ) )
      return m_type.getLabel();

    return m_label;
  }

  @Override
  public boolean isMetadataSource( )
  {
    return m_type.isMetadataSource();
  }

  @Override
  public IAxis[] getAxes( )
  {
    final IObservation observation = m_handler.getObservation();
    if( Objects.isNull( observation ) )
      return new IAxis[] {};

    return observation.getAxes();
  }

  @Override
  public IObservation getObservation( )
  {
    return m_handler.getObservation();
  }

  @Override
  public IAxis getIndexAxis( )
  {
    return AxisUtils.findAxis( getAxes(), m_type.getIndexAxis() );
  }

  @Override
  public IAxis getValueAxis( )
  {
    return AxisUtils.findAxis( getAxes(), m_type.getValueAxis() );
  }

  @Override
  public IAxis getStatusAxis( )
  {
    final IAxis valueAxis = getValueAxis();
    if( valueAxis == null )
      return null;

    return KalypsoStatusUtils.findStatusAxisFor( getAxes(), valueAxis );
  }

  @Override
  public void eventObservationChanged( )
  {
    fireColumnChanged();
  }

  @Override
  public void eventObservationLoaded( )
  {
    fireColumnChanged();
  }

  public void fireColumnChanged( )
  {
    final IZmlModelColumnListener[] listeners = m_listeners.toArray( new IZmlModelColumnListener[] {} );
    for( final IZmlModelColumnListener listener : listeners )
    {
      listener.modelColumnChangedEvent( this );
    }

  }

  @Override
  public void accept( final IZmlModelColumnVisitor visitor ) throws SensorException
  {
    final IZmlModel model = getModel();
    final IZmlModelRow[] rows = model.getRows();
    for( final IZmlModelRow row : rows )
    {
      final IZmlValueReference reference = row.get( this );
      if( Objects.isNotNull( reference ) )
      {
        try
        {
          visitor.visit( reference );
        }
        catch( final CancelVisitorException e )
        {
          return;
        }
      }
    }
  }

  @Override
  public void accept( final IZmlModelColumnVisitor visitor, final DateRange daterange ) throws SensorException
  {
    if( Objects.isNull( daterange ) )
    {
      accept( visitor );
      return;
    }

    final IZmlModel model = getModel();
    final IZmlModelRow[] rows = model.getRows();
    for( final IZmlModelRow row : rows )
    {
      final IZmlValueReference reference = row.get( this );

      if( Objects.isNotNull( reference ) )
      {
        try
        {
          if( daterange.containsInclusive( reference.getIndexValue() ) )
            visitor.visit( reference );
        }
        catch( final CancelVisitorException e )
        {
          return;
        }
      }
    }
  }

  @Override
  public IZmlModel getModel( )
  {
    return m_model;
  }

  @Override
  public boolean isActive( )
  {
    if( isIgnoreType() )
      return false;

    final IZmlModelColumnDataHandler handler = getDataHandler();
    if( Objects.isNull( handler ) )
      return false;

    final IObservation observation = handler.getObservation();
    if( Objects.isNull( observation ) )
      return false;

    final IAxis[] axes = observation.getAxes();
    final String valueAxis = getDataColumn().getValueAxis();

    return Objects.isNotNull( AxisUtils.findAxis( axes, valueAxis ) );
  }

  private boolean isIgnoreType( )
  {
    final IZmlModel model = getModel();
    final String[] ignoreTypes = model.getIgnoreTypes();

    final DataColumnType columnType = getDataColumn().getType();
    final String type = columnType.getValueAxis();

    return ArrayUtils.contains( ignoreTypes, type );
  }

  @Override
  public void fireColumnChangedEvent( )
  {
    try
    {
      // FIXME improve update value handling
      final IObservation observation = m_handler.getObservation();
      if( Objects.isNull( observation ) )
        return;

      observation.setValues( getTupleModel() );
      observation.fireChangedEvent( this );

    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void setLableTokenizer( final String titleTokenizer )
  {
    m_labelTokenizer = titleTokenizer;
  }

  @Override
  public String getLabelTokenizer( )
  {
    return m_labelTokenizer;
  }

  /**
   * @see org.kalypso.zml.core.table.model.IZmlModelColumn#isLabeled()
   */
  @Override
  public boolean isLabeled( )
  {
    return m_labeled;
  }

}
