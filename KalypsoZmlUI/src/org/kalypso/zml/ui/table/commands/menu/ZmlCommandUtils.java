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
package org.kalypso.zml.ui.table.commands.menu;

import java.util.Date;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.zml.ui.table.model.IZmlTableCell;

/**
 * @author Dirk Kuch
 */
public final class ZmlCommandUtils
{
  private ZmlCommandUtils( )
  {
  }

  public static IZmlTableCell[] findIntervall( final IZmlTableCell[] cells )
  {
    IZmlTableCell start = cells[0];
    IZmlTableCell end = cells[0];

    for( final IZmlTableCell cell : cells )
    {
      if( cell.getIndex() < start.getIndex() )
        start = cell;

      if( cell.getIndex() > end.getIndex() )
        end = cell;
    }

    return new IZmlTableCell[] { start, end };

  }

  public static DateRange findDateRange( final IZmlTableCell[] cells )
  {
    Date min = null;
    Date max = null;

    for( final IZmlTableCell cell : cells )
    {
      final Date date = cell.getRow().getModelRow().getIndexValue();
      if( Objects.isNull( min ) )
        min = date;
      else if( date.before( min ) )
        min = date;

      if( Objects.isNull( max ) )
        max = date;
      else if( date.after( max ) )
        max = date;
    }

    return new DateRange( min, max );
  }
}