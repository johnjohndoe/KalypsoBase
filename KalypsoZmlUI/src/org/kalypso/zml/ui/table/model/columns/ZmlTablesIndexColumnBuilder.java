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
package org.kalypso.zml.ui.table.model.columns;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.ui.table.IZmlTable;

/**
 * @author Dirk Kuch
 */
public class ZmlTablesIndexColumnBuilder implements ICoreRunnableWithProgress
{
  protected final IZmlTable[] m_tables;

  private final BaseColumn m_column;

  public ZmlTablesIndexColumnBuilder( final IZmlTable[] tables, final BaseColumn column )
  {
    m_tables = tables;
    m_column = column;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    for( final IZmlTable table : m_tables )
    {
// final TableViewer viewer = table.getViewer();
// final int index = viewer.getTable().getColumnCount();
// final TableViewerColumn viewerColumn = new TableViewerColumn( viewer, TableTypes.toSWT( m_column.getAlignment() ) );

      final ZmlTableIndexColumn column = new ZmlTableIndexColumn( table, m_column, null, -1 );
      table.getModel().add( column );
    }

    return Status.OK_STATUS;
  }
}
