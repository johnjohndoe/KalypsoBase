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
package org.kalypso.zml.ui.table.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.IZmlTable;

/**
 * @author Dirk Kuch
 */
public final class ZmlTableColumns
{
  private ZmlTableColumns( )
  {
  }

  public static IZmlTableColumn[] toTableColumns( final IZmlTable table, final boolean index, final IZmlModelColumn[] modelColumns )
  {
    final Set<IZmlTableColumn> columns = new HashSet<IZmlTableColumn>();

    final ZmlTableColumn[] tableColumns = (ZmlTableColumn[]) table.getColumns();
    for( final ZmlTableColumn tableColumn : tableColumns )
    {
      if( tableColumn.isIndexColumn() && index )
        columns.add( tableColumn );

      // columns empty? means refresh all columns
      if( ArrayUtils.isEmpty( modelColumns ) )
        columns.add( tableColumn );
      else
      {
        final IZmlModelColumn modelColumn = tableColumn.getModelColumn();
        if( ArrayUtils.contains( modelColumns, modelColumn ) )
          columns.add( tableColumn );
      }
    }

    return columns.toArray( new IZmlTableColumn[] {} );
  }
}