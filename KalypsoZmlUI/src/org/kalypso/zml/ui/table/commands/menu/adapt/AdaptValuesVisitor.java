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
package org.kalypso.zml.ui.table.commands.menu.adapt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.transaction.ZmlModelTransaction;
import org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor;

/**
 * - reads values from transformed observation and stores 'em into a internal value map<br>
 * - and afterwards updates the zml table model column
 *
 * @author Dirk Kuch
 */
public class AdaptValuesVisitor implements IObservationVisitor, IZmlModelColumnVisitor
{
  private final Map<Date, Number> m_values = new HashMap<>();

  private final ZmlModelTransaction m_transaction = new ZmlModelTransaction();

  private final String m_type;

  public AdaptValuesVisitor( final String type )
  {
    m_type = type;
  }

  @Override
  public void visit( final IObservationValueContainer container )
  {
    final IAxis dateAxis = AxisUtils.findDateAxis( container.getAxes() );
    final IAxis valueAxis = AxisUtils.findAxis( container.getAxes(), m_type );

    try
    {
      final Date date = (Date) container.get( dateAxis );
      final Number value = (Number) container.get( valueAxis );

      m_values.put( date, value );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public void visit( final IZmlModelValueCell reference )
  {
    final Date index = reference.getIndexValue();
    final Number value = m_values.get( index );
    if( Objects.isNull( value ) )
      return;

    // FIXME: use target axes instead!
    m_transaction.add( reference, value, IDataSourceItem.SOURCE_MANUAL_CHANGED, KalypsoStati.BIT_USER_MODIFIED );
  }

  public void doFinish( )
  {
    m_transaction.execute();
  }
}
