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

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.model.data.IZmlModelColumnDataHandler;
import org.kalypso.zml.core.table.model.data.IZmlModelColumnDataListener;
import org.kalypso.zml.core.table.model.data.ObservationZmlColumnDataHandler;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor;

/**
 * @author Dirk Kuch
 */
public class ZmlModelColumn implements IZmlModelColumn, IZmlModelColumnDataListener
{
  private final Set<IZmlModelColumnListener> m_listeners = new LinkedHashSet<IZmlModelColumnListener>();

  private final DataColumn m_type;

  private final String m_label;

  private final IZmlModelColumnDataHandler m_handler;

  private final String m_identifier;

  private final IZmlModel m_model;

  public ZmlModelColumn( final IZmlModel model, final String identifier, final String label, final DataColumn type, final IZmlModelColumnDataHandler dataHandler )
  {
    m_model = model;
    m_identifier = identifier;
    m_label = label;
    m_type = type;

    m_handler = dataHandler;
    m_handler.addListener( this );
  }

  public ZmlModelColumn( final IZmlModel model, final DataColumn column, final ObservationZmlColumnDataHandler handler )
  {
    this( model, column.getIdentifier(), column.getLabel(), column, handler );
  }

  @Override
  public void addListener( final IZmlModelColumnListener listener )
  {
    m_listeners.add( listener );
  }

  /**
   * @see org.kalypso.zml.ui.chart.layer.themes.IZmlLayer#getDataHandler()
   */
  @Override
  public IZmlModelColumnDataHandler getDataHandler( )
  {
    return m_handler;
  }

  public void dispose( )
  {
    m_handler.dispose();
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
  public void update( final int index, final Object value, final String source, final Integer status ) throws SensorException
  {
    final ITupleModel model = getTupleModel();
    final IAxis[] axes = model.getAxes();

    for( final IAxis axis : axes )
    {
      if( AxisUtils.isDataSrcAxis( axis ) )
      {
        // FIXME - user modified triggerd interpolated state?!?
        final DataSourceHandler handler = new DataSourceHandler( getMetadata() );
        final int sourceIndex = handler.addDataSource( source, source );

        model.set( index, axis, sourceIndex );
      }
      else if( AxisUtils.isStatusAxis( axis ) )
      {
        model.set( index, axis, status );
      }
      else if( isTargetAxis( axis ) )
      {
        model.set( index, axis, value );
      }
    }

    // FIXME improve update value handling
    final IObservation observation = m_handler.getObservation();
    observation.setValues( model );
    observation.fireChangedEvent( this );
  }

  @Override
  public MetadataList getMetadata( )
  {
    return m_handler.getObservation().getMetadataList();
  }

  @Override
  public DataColumn getDataColumn( )
  {
    return m_type;
  }

  @Override
  public String getLabel( )
  {
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
    return m_handler.getObservation().getAxes();
  }

  /**
   * @see org.kalypso.zml.ui.table.model.IZmlModelColumn#getObservation()
   */
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

  /**
   * @see org.kalypso.zml.ui.table.model.IZmlModelColumn#getStatusAxis()
   */
  @Override
  public IAxis getStatusAxis( )
  {
    final IAxis valueAxis = getValueAxis();
    if( valueAxis == null )
      return null;

    return KalypsoStatusUtils.findStatusAxisFor( getAxes(), valueAxis );
  }

  /**
   * @see org.kalypso.zml.core.table.model.data.IZmlModelColumnDataListener#eventObservationChanged()
   */
  @Override
  public void eventObservationChanged( )
  {
    fireColumnChanged();
  }

  /**
   * @see org.kalypso.zml.core.table.model.data.IZmlModelColumnDataListener#eventObservationLoaded()
   */
  @Override
  public void eventObservationLoaded( )
  {
    fireColumnChanged();
  }

  private void fireColumnChanged( )
  {
    final IZmlModelColumnListener[] listeners = m_listeners.toArray( new IZmlModelColumnListener[] {} );
    for( final IZmlModelColumnListener listener : listeners )
    {
      listener.modelColumnChangedEvent();
    }

  }

  /**
   * @see org.kalypso.zml.core.table.model.IZmlModelColumn#accept(org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor)
   */
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
        if( !visitor.visit( reference ) )
          return;
      }
    }
  }

  /**
   * @see org.kalypso.zml.core.table.model.IZmlModelColumn#getModel()
   */
  @Override
  public IZmlModel getModel( )
  {
    return m_model;
  }

}
