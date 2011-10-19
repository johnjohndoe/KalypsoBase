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
package org.kalypso.zml.ui.table.layout;

import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.TableColumn;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.ui.table.provider.strategy.IExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class PackIndexColumnsVisitor extends AbstractTableColumnPackVisitor
{
  private final boolean m_visible;

  public PackIndexColumnsVisitor( final boolean visible )
  {
    m_visible = visible;
  }

  @Override
  public void visit( final IExtendedZmlTableColumn column )
  {
    if( !column.isIndexColumn() )
      return;

    final BaseColumn columnType = column.getColumnType();
    final TableViewerColumn tableViewerColumn = column.getTableViewerColumn();
    final TableColumn tableColumn = tableViewerColumn.getColumn();

    final String label = columnType.getLabel();
    tableColumn.setText( label );

    if( !m_visible )
    {
      hide( tableColumn );
    }
    else
      pack( tableColumn, columnType, label, true );
  }

}
