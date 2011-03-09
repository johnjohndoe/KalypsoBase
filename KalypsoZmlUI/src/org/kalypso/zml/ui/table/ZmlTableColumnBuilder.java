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
package org.kalypso.zml.ui.table;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.TableTypeHelper;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.ui.table.provider.ZmlEditingSupport;
import org.kalypso.zml.ui.table.provider.ZmlLabelProvider;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTableColumnBuilder implements ICoreRunnableWithProgress
{

  private final IZmlTable m_table;

  private final BaseColumn m_column;

  public ZmlTableColumnBuilder( final IZmlTable table, final BaseColumn column )
  {
    m_table = table;
    m_column = column;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final TableViewer viewer = m_table.getTableViewer();
    final int index = viewer.getTable().getColumnCount();
    final TableViewerColumn viewerColumn = new TableViewerColumn( viewer, TableTypeHelper.toSWT( m_column.getAlignment() ) );

    final ExtendedZmlTableColumn column = new ExtendedZmlTableColumn( m_table, viewerColumn, m_column, index );
    m_table.add( column );

    final ZmlLabelProvider labelProvider = new ZmlLabelProvider( column );
    viewerColumn.setLabelProvider( labelProvider );
    viewerColumn.getColumn().setText( m_column.getLabel() );

    /** edit support */
    if( m_column.getType() instanceof DataColumnType && m_column.isEditable() )
    {
      final ZmlEditingSupport editingSupport = new ZmlEditingSupport( column, labelProvider, m_table.getSelectionHandler() );
      column.setEditingSupport( editingSupport );
    }

    return Status.OK_STATUS;
  }
}
