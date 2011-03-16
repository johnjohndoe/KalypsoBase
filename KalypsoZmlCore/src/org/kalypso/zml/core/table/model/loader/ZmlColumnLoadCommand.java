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

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.table.IZmlTableElement;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.binding.TableTypeHelper;
import org.kalypso.zml.core.table.model.IColumnLabelProvider;
import org.kalypso.zml.core.table.model.ZmlModel;
import org.kalypso.zml.core.table.model.ZmlModelColumn;
import org.kalypso.zml.core.table.model.data.ObsProviderZmlColumnDataHandler;
import org.kalypso.zml.core.table.schema.DataColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlColumnLoadCommand implements IObsProviderListener
{
  private boolean m_canceled = false;

  protected final IZmlTableElement m_element;

  private final ZmlModel m_model;

  public ZmlColumnLoadCommand( final ZmlModel model, final IZmlTableElement column )
  {
    m_model = model;
    m_element = column;

    synchronized( this )
    {
      final IObsProvider provider = column.getObsProvider();
      if( provider.isLoaded() )
      {
        execute();
      }
      else
      {
        provider.addListener( this );
      }
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProviderListener#observationLoadedEvent()
   */
  @Override
  public void observationReplaced( )
  {
    // FIXME: this happens, if the zml does not exist.
    // It would be nice, to keep this listener,maybe it appears later....
    // but: who will dispose me?

    // final IObservation observation = m_element.getObsProvider().getObservation();
    // if( observation == null )
    // return;

    m_element.getObsProvider().removeListener( this );

    synchronized( this )
    {
      execute();
    }
  }

  public void cancel( )
  {
    m_canceled = true;
  }

  private void execute( )
  {
    if( m_canceled )
      return;

    /** base observation will be disposed by NewZmlTableLayoutPart (save table) */
    final IObsProvider base = m_element.getObsProvider();
    final IObsProvider clone = base.copy();
    final IObservation observation = clone.getObservation();
    if( observation == null )
    {
// base.dispose();
      clone.dispose();
      return;
    }

    final DataColumnType type = (DataColumnType) TableTypeHelper.findColumnType( m_model.getTableType(), m_element.getIdentifier() );
    final IAxis[] axes = observation.getAxes();
    if( !hasValueAxis( axes, type ) )
      return;

    final DataColumn data = new DataColumn( type );

    final IColumnLabelProvider labelProvider = new IColumnLabelProvider()
    {
      @Override
      public String getLabel( )
      {
        return m_element.getTitle( AxisUtils.findAxis( axes, data.getValueAxis() ) );
      }
    };

    final ZmlModelColumn column = new ZmlModelColumn( m_model, m_element.getIdentifier(), labelProvider, data, new ObsProviderZmlColumnDataHandler( clone ) );
    m_model.add( column );

// base.dispose();
  }

  private boolean hasValueAxis( final IAxis[] axes, final DataColumnType type )
  {
    final IAxis axis = AxisUtils.findAxis( axes, type.getValueAxis() );

    return axis != null;
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProviderListener#observationChangedX(java.lang.Object)
   */
  @Override
  public void observationChanged( final Object source )
  {
    m_model.fireModelChanged();
  }
}
