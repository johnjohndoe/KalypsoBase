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
package org.kalypso.zml.ui.table.commands.menu.adjust.pages;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.transaction.ZmlModelTransaction;
import org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor;

/**
 * @author Dirk Kuch
 */
public class ShiftDateValuesVisitor implements IZmlModelColumnVisitor
{
  Map<Date, Number> m_shift = new HashMap<Date, Number>();

  ZmlModelTransaction m_transaction = new ZmlModelTransaction();

  public ShiftDateValuesVisitor( final IZmlModelCell[] selected, final Integer offset )
  {
    for( final IZmlModelCell cell : selected )
    {
      if( !(cell instanceof IZmlModelValueCell) )
        continue;

      final IZmlModelValueCell reference = (IZmlModelValueCell) cell;

      try
      {
        final Date date = cell.getIndexValue();

        final Calendar calendar = Calendar.getInstance( KalypsoCorePlugin.getDefault().getTimeZone() );
        calendar.setTime( date );
        calendar.add( Calendar.MINUTE, offset );

        m_shift.put( calendar.getTime(), (Number) reference.getValue() );
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }

  }

  @Override
  public void visit( final IZmlModelValueCell reference )
  {
    final Number value = m_shift.get( reference.getIndexValue() );
    if( Objects.isNotNull( value ) )
      m_transaction.add( reference, value, IDataSourceItem.SOURCE_MANUAL_CHANGED, KalypsoStati.BIT_USER_MODIFIED );

  }

  public void doFinish( )
  {
    m_transaction.execute();
  }
}
