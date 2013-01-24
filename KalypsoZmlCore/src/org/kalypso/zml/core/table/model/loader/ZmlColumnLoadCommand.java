/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.core.table.model.loader;

import org.apache.commons.lang.ObjectUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.base.IZmlSourceElement;
import org.kalypso.zml.core.debug.KalypsoZmlCoreDebug;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.binding.TableTypes;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.ZmlModelColumn;
import org.kalypso.zml.core.table.model.data.IZmlModelColumnDataHandler;
import org.kalypso.zml.core.table.model.data.ObsProviderZmlColumnDataHandler;
import org.kalypso.zml.core.table.model.event.IZmlModelColumnEvent;
import org.kalypso.zml.core.table.model.event.ZmlModelColumnChangeType;
import org.kalypso.zml.core.table.schema.DataColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlColumnLoadCommand implements IObsProviderListener
{
  private boolean m_canceled = false;

  protected final IZmlSourceElement m_source;

  private final IZmlModel m_model;

  public ZmlColumnLoadCommand( final IZmlModel model, final IZmlSourceElement element )
  {
    m_model = model;
    m_source = element;
  }

  public void execute( )
  {
    synchronized( this )
    {
      final IObsProvider provider = m_source.getObsProvider();
      if( provider.isLoaded() )
      {
        KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand - doExecute(): %s (provider: %s)\n", m_source.getIdentifier(), ObjectUtils.identityToString( provider ) );
        doExcecute();
      }
      else
      {
        KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand - ObsProvider.addListener(): %s (provider: %s)\n", m_source.getIdentifier(), ObjectUtils.identityToString( provider ) );
        provider.addListener( this );
      }
    }
  }

  @Override
  public void observationReplaced( )
  {
    KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand.observationReplaced(): %s (provider: %s\n", m_source.getIdentifier(), ObjectUtils.identityToString( m_source.getObsProvider() ) );

    // FIXME: this happens, if the zml does not exist.
    // It would be nice, to keep this listener,maybe it appears later....
    // but: who will dispose me?

    // final IObservation observation = m_element.getObsProvider().getObservation();
    // if( observation == null )
    // return;
    m_source.getObsProvider().removeListener( this );

    synchronized( this )
    {
      doExcecute();
    }

    m_model.fireModelChanged( new ZmlModelColumnChangeType( IZmlModelColumnEvent.STRUCTURE_CHANGE ) );
  }

  @Override
  public void observationChanged( final Object source )
  {
    KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand.observationChanged(): %s\n", m_source.getIdentifier() );
    m_model.fireModelChanged( new ZmlModelColumnChangeType( IZmlModelColumnEvent.VALUE_CHANGED ) );
  }

  public synchronized void cancel( )
  {
    KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand.cancel(): %s\n", m_source.getIdentifier() );

    m_canceled = true;
// m_source.getObsProvider().dispose();
  }

  private void doExcecute( )
  {
    KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand.doExecute(): %s\n", m_source.getIdentifier() );

    if( m_canceled )
    {
      KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand - Loading model column canceled: %s\n", m_source.getIdentifier() );
      return;
    }

    /** base observation will be disposed by NewZmlTableLayoutPart (save table) */
    final IObsProvider base = m_source.getObsProvider();
    final DataColumnType type = (DataColumnType) TableTypes.findColumnType( m_model.getTableType(), m_source.getIdentifier() );
    if( Objects.isNull( type ) )
      return;

    final IObservation observation = base.getObservation();
    final IAxis[] axes = Objects.isNotNull( observation ) ? observation.getAxes() : new IAxis[] {};

    final IZmlModelColumnDataHandler handler = new ObsProviderZmlColumnDataHandler( base.copy() );
    final DataColumn data = new DataColumn( type );

    IZmlModelColumn column = m_model.getColumn( m_source.getIdentifier() );
    if( Objects.isNull( column ) )
    {
      // add new column
      KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand - Adding new model column: %s\n", m_source.getIdentifier() );

      column = new ZmlModelColumn( m_model, m_source.getIdentifier(), data );
      m_model.add( m_source, column );

      column.setDataHandler( handler );
    }
    else
    {
      // update data of existing column
      column.setDataHandler( handler );
    }

    doUpdateColumn( column, data, axes );
  }

  private void doUpdateColumn( final IZmlModelColumn column, final DataColumn type, final IAxis[] axes )
  {
    final IAxis axis = AxisUtils.findAxis( axes, type.getValueAxis() );
    column.setLabel( m_source.getLabel( axis ) );
  }
}