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
import org.kalypso.zml.core.debug.KalypsoZmlCoreDebug;
import org.kalypso.zml.core.table.IZmlTableElement;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.binding.TableTypes;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.ZmlModelColumn;
import org.kalypso.zml.core.table.model.data.IZmlModelColumnDataHandler;
import org.kalypso.zml.core.table.model.data.ObsProviderZmlColumnDataHandler;
import org.kalypso.zml.core.table.schema.DataColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlColumnLoadCommand implements IObsProviderListener
{
  private boolean m_canceled = false;

  protected final IZmlTableElement m_element;

  private final IZmlModel m_model;

  public ZmlColumnLoadCommand( final IZmlModel model, final IZmlTableElement element )
  {
    m_model = model;
    m_element = element;
  }

  public void execute( )
  {
    synchronized( this )
    {
      final IObsProvider provider = m_element.getObsProvider();
      if( provider.isLoaded() )
      {
        KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand - doExecute(): %s (provider: %s)\n", m_element.getIdentifier(), ObjectUtils.identityToString( provider ) );
        doExcecute();
      }
      else
      {
        KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand - ObsProvider.addListener(): %s (provider: %s)\n", m_element.getIdentifier(), ObjectUtils.identityToString( provider ) );
        provider.addListener( this );
      }
    }
  }

  @Override
  public void observationReplaced( )
  {
    KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand.observationReplaced(): %s (provider: %s\n", m_element.getIdentifier(), ObjectUtils.identityToString( m_element.getObsProvider() ) );

    // FIXME: this happens, if the zml does not exist.
    // It would be nice, to keep this listener,maybe it appears later....
    // but: who will dispose me?

    // final IObservation observation = m_element.getObsProvider().getObservation();
    // if( observation == null )
    // return;
    m_element.getObsProvider().removeListener( this );

    synchronized( this )
    {
      doExcecute();
    }
  }

  public synchronized void cancel( )
  {
    KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand.cancel(): %s\n", m_element.getIdentifier() );

    m_canceled = true;
    m_element.dispose();
  }

  @Override
  public void observationChanged( final Object source )
  {
    KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand.observationChanged(): %s\n", m_element.getIdentifier() );

    m_model.fireModelChanged();
  }

  private void doExcecute( )
  {
    KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand.doExecute(): %s\n", m_element.getIdentifier() );

    try
    {
      if( m_canceled )
      {
        KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand - Loading model column canceled: %s\n", m_element.getIdentifier() );
        return;
      }

      /** base observation will be disposed by NewZmlTableLayoutPart (save table) */
      final IObsProvider base = m_element.getObsProvider();

      final DataColumnType type = (DataColumnType) TableTypes.findColumnType( m_model.getTableType(), m_element.getIdentifier() );
      if( Objects.isNull( type ) )
        return;

      final IObservation observation = base.getObservation();
      final IAxis[] axes = Objects.isNotNull( observation ) ? observation.getAxes() : new IAxis[] {};

      final IObsProvider provider = base.copy();
      final IZmlModelColumnDataHandler handler = new ObsProviderZmlColumnDataHandler( provider );

      final DataColumn data = new DataColumn( type );

      IZmlModelColumn column = m_model.getColumn( m_element.getIdentifier() );
      if( Objects.isNull( column ) )
      {
        KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlColumnLoadCommand - Adding new model column: %s\n", m_element.getIdentifier() );
        column = new ZmlModelColumn( m_model, m_element.getIdentifier(), data );
        column.setDataHandler( handler );

        doUpdateColumn( column, data, axes );

        m_model.add( column );
      }
    }
    finally
    {
      m_element.dispose();
    }
  }

  private void doUpdateColumn( final IZmlModelColumn column, final DataColumn type, final IAxis[] axes )
  {
    final IAxis axis = AxisUtils.findAxis( axes, type.getValueAxis() );
    if( Objects.isNotNull( axis ) )
    {
      final String label = m_element.getTitle( axis );
      column.setLabel( label );
    }

    column.setLableTokenizer( m_element.getTitleTokenzizer() );
  }

}
