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
package org.kalypso.zml.ui.table.model.visitors;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.IZmlTableRowVisitor;
import org.kalypso.zml.ui.table.model.rows.IZmlTableValueRow;

/**
 * @author Dirk Kuch
 */
public class DateRangeVisitor implements IZmlTableRowVisitor
{
  Map<Date, IZmlTableValueRow> m_rows = new TreeMap<Date, IZmlTableValueRow>();

  private final DateRange m_dateRange;

  public DateRangeVisitor( final DateRange dateRange )
  {
    m_dateRange = dateRange;
  }

  @Override
  public void visit( final IZmlTableValueRow row )
  {
    final IZmlModelRow modelRow = row.getModelRow();
    final Date index = modelRow.getIndex();

    if( m_dateRange.containsLazyInclusive( index ) )
      m_rows.put( index, row );
  }

  public IZmlTableValueRow[] getRows( )
  {
    return m_rows.values().toArray( new IZmlTableValueRow[] {} );
  }

  public IZmlModelRow[] getModelRows( )
  {
    final Set<IZmlModelRow> rows = new LinkedHashSet<IZmlModelRow>();
    final IZmlTableValueRow[] tableRows = getRows();
    for( final IZmlTableValueRow tableRow : tableRows )
    {
      rows.add( tableRow.getModelRow() );
    }

    return rows.toArray( new IZmlModelRow[] {} );
  }

  public DateRange getDateRange( )
  {
    final Date[] dates = m_rows.keySet().toArray( new Date[] {} );
    if( ArrayUtils.isEmpty( dates ) )
      return null;

    return new DateRange( dates[0], dates[dates.length - 1] );
  }
}
