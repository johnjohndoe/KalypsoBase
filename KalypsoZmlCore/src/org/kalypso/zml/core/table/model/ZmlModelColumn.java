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

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.transaction.TupleModelTransaction;
import org.kalypso.ogc.sensor.transaction.UpdateTupleModelDataSetCommand;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.binding.rule.ZmlColumnRule;
import org.kalypso.zml.core.table.model.data.IZmlModelColumnDataHandler;
import org.kalypso.zml.core.table.model.data.IZmlModelColumnObservationListener;
import org.kalypso.zml.core.table.model.event.IZmlModelColumnEvent;
import org.kalypso.zml.core.table.model.event.IZmlModelColumnListener;
import org.kalypso.zml.core.table.model.event.ZmlModelColumnChangeType;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.transaction.IZmlModelUpdateCommand;
import org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor;
import org.kalypso.zml.core.table.rules.AppliedRule;
import org.kalypso.zml.core.table.rules.IZmlColumnRuleImplementation;
import org.kalypso.zml.core.table.schema.DataColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlModelColumn implements IZmlModelColumn, IZmlModelColumnObservationListener
{
  private IZmlModelColumnDataHandler m_handler;

  private final String m_identifier;

  private String m_label;

  private String m_labelTokenizer;

  private final Set<IZmlModelColumnListener> m_listeners = new LinkedHashSet<>();

  private final IZmlModel m_model;

  private final DataColumn m_type;

  final Set<IZmlModelValueCell> m_cells = new LinkedHashSet<>();

  public ZmlModelColumn( final IZmlModel model, final DataColumn column )
  {
    this( model, column.getIdentifier(), column );
  }

  public ZmlModelColumn( final IZmlModel model, final String identifier, final DataColumn type )
  {
    m_model = model;
    m_identifier = identifier;
    m_type = type;
  }

  private synchronized void doReset( )
  {
    m_type.reset();
    m_cells.clear();
  }

  @Override
  public void accept( final IZmlModelColumnVisitor visitor ) throws SensorException
  {
    final IZmlModel model = getModel();
    final IZmlModelRow[] rows = model.getRows();
    for( final IZmlModelRow row : rows )
    {
      final IZmlModelValueCell reference = row.get( this );
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
      final IZmlModelValueCell reference = row.get( this );

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
  public void addListener( final IZmlModelColumnListener listener )
  {
    m_listeners.add( listener );
  }

  @Override
  public void dispose( )
  {
    if( Objects.isNotNull( m_handler ) )
    {
      m_handler.removeListener( this );
      m_handler.dispose();

      m_handler = null;

      fireColumnChanged( IZmlModelColumnEvent.COLUMN_DISPOSED );
    }

  }

  @Override
  public void doExecute( final IZmlModelUpdateCommand command ) throws SensorException
  {
    final IZmlModelValueCell target = command.getTarget();
    update( target.getModelIndex(), command.getValue(), command.getDataSource(), command.getStatus() );
  }

  @Override
  public void doUpdate( final int index, final Object value, final String source, final Integer status ) throws SensorException
  {
    update( index, value, source, status );
  }

  @Override
  public void eventObservationChanged( )
  {
    fireColumnChanged( IZmlModelColumnEvent.VALUE_CHANGED );
  }

  @Override
  public void eventObservationLoaded( )
  {
    fireColumnChanged( IZmlModelColumnEvent.STRUCTURE_CHANGE );
  }

  public void fireColumnChanged( final int type )
  {
    final ZmlModelColumnChangeType event = new ZmlModelColumnChangeType( type );
    final IZmlModelColumnListener[] listeners = m_listeners.toArray( new IZmlModelColumnListener[] {} );
    for( final IZmlModelColumnListener listener : listeners )
    {
      listener.modelColumnChangedEvent( this, event );
    }
  }

  @Override
  public Object get( final int index, final IAxis axis ) throws SensorException
  {
    if( axis == null )
      return null;

    return getTupleModel().get( index, axis );
  }

  @Override
  public IAxis[] getAxes( )
  {
    if( Objects.isNull( m_handler ) )
      return new IAxis[] {};

    final IObservation observation = m_handler.getObservation();
    if( Objects.isNull( observation ) )
      return new IAxis[] {};

    return observation.getAxes();
  }

  @Override
  public synchronized IZmlModelValueCell[] getCells( )
  {
    if( !m_cells.isEmpty() )
      return m_cells.toArray( new IZmlModelValueCell[] {} );

    final IZmlModelRow[] rows = getModel().getRows();
    for( final IZmlModelRow row : rows )
    {
      final IZmlModelValueCell cell = row.get( this );
      if( Objects.isNotNull( cell ) )
        m_cells.add( cell );
    }

    return m_cells.toArray( new IZmlModelValueCell[] {} );
  }

  @Override
  public DataColumn getDataColumn( )
  {
    return m_type;
  }

  @Override
  public IZmlModelColumnDataHandler getDataHandler( )
  {
    return m_handler;
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  @Override
  public IAxis getIndexAxis( )
  {
    return AxisUtils.findAxis( getAxes(), m_type.getIndexAxis() );
  }

  @Override
  public String getLabel( )
  {
    if( Strings.isEmpty( m_label ) )
      return m_type.getLabel();

    return m_label;
  }

  @Override
  public String getLabelTokenizer( )
  {
    return m_labelTokenizer;
  }

  @Override
  public MetadataList getMetadata( )
  {
    if( Objects.isNull( m_handler ) )
      return null;

    final IObservation observation = m_handler.getObservation();
    if( Objects.isNull( observation ) )
      return null;

    return observation.getMetadataList();
  }

  @Override
  public IZmlModel getModel( )
  {
    return m_model;
  }

  @Override
  public IObservation getObservation( )
  {
    if( Objects.isNull( m_handler ) )
      return null;

    return m_handler.getObservation();
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
  public ITupleModel getTupleModel( ) throws SensorException
  {
    return m_handler.getModel();
  }

  @Override
  public IAxis getValueAxis( )
  {
    return AxisUtils.findAxis( getAxes(), m_type.getValueAxis() );
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
  public boolean isMetadataSource( )
  {
    return m_type.isMetadataSource();
  }

  @Override
  public void removeListener( final IZmlModelColumnListener listener )
  {
    m_listeners.remove( listener );
  }

  @Override
  public void setDataHandler( final IZmlModelColumnDataHandler handler )
  {
    synchronized( this )
    {
      doReset();

      if( Objects.isNotNull( m_handler ) )
      {
        m_handler.removeListener( this );
        m_handler.dispose();
      }

      m_handler = handler;
      m_handler.addListener( this );
    }

    fireColumnChanged( IZmlModelColumnEvent.STRUCTURE_CHANGE );
  }

  @Override
  public void setLabel( final String label )
  {
    m_label = label;
  }

  @Override
  public void setLableTokenizer( final String titleTokenizer )
  {
    m_labelTokenizer = titleTokenizer;
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

  private void update( final int index, final Object value, final String source, final Integer status ) throws SensorException
  {
    final Object val = Objects.firstNonNull( value, Double.NaN );
    final Integer stat = Objects.firstNonNull( status, KalypsoStati.BIT_OK );
    final String src = Objects.firstNonNull( source, IDataSourceItem.SOURCE_UNKNOWN );

    final TupleModelTransaction transaction = new TupleModelTransaction( getTupleModel(), getMetadata() );

    final TupleModelDataSet dataset = new TupleModelDataSet( getValueAxis(), val, stat, src );
    transaction.add( new UpdateTupleModelDataSetCommand( index, dataset, true ) );

    getTupleModel().execute( transaction );
  }

  @Override
  public AppliedRule[] getColumnRules( )
  {
    final Set<AppliedRule> active = new LinkedHashSet<>();

    final ZmlColumnRule[] columnRules = getDataColumn().getColumnRules();
    for( final ZmlColumnRule rule : columnRules )
    {
      try
      {
        final IZmlColumnRuleImplementation impl = rule.getImplementation();
        if( impl.doApply( this ) )
          active.add( new AppliedRule( rule.getBaseStyle(), rule.getRuleType().getLabel(), 1.0, true ) );
      }
      catch( final CoreException e )
      {
        e.printStackTrace();
      }
    }

    return active.toArray( new AppliedRule[] {} );
  }
}
