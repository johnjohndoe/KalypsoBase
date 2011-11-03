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
package org.kalypso.zml.ui.table;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.TableTypes;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.ui.table.focus.ZmlTableEditingSupport;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.ZmlTableColumn;
import org.kalypso.zml.ui.table.provider.ZmlTooltipProvider;

/**
 * @author Dirk Kuch
 */
public class ZmlTableColumnBuilder implements ICoreRunnableWithProgress
{
  protected final IZmlTable m_table;

  private final BaseColumn m_column;

  public ZmlTableColumnBuilder( final IZmlTable table, final BaseColumn column )
  {
    m_table = table;
    m_column = column;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final TableViewer viewer = m_table.getViewer();
    final int index = viewer.getTable().getColumnCount();
    final TableViewerColumn viewerColumn = new TableViewerColumn( viewer, TableTypes.toSWT( m_column.getAlignment() ) );

    final ZmlTableColumn column = new ZmlTableColumn( m_table, viewerColumn, m_column, index );
    m_table.add( column );

    viewerColumn.setLabelProvider( new ZmlTooltipProvider( column ) );
    viewerColumn.getColumn().setText( m_column.getLabel() );

    /** edit support */
    if( m_column.getType() instanceof DataColumnType && m_column.isEditable() )
    {
      final ZmlTableEditingSupport editingSupport = new ZmlTableEditingSupport( column, m_table.getFocusHandler() );
      column.setEditingSupport( editingSupport );
    }

    viewerColumn.getColumn().addControlListener( new ControlListener()
    {
      @Override
      public void controlResized( final ControlEvent e )
      {
        doRedraw();
      }

      @Override
      public void controlMoved( final ControlEvent e )
      {
        doRedraw();
      }

      private void doRedraw( )
      {
        final IZmlTableCell cell = m_table.getFocusHandler().getFocusTableCell();
        if( Objects.isNull( cell ) )
          return;

        if( Objects.equal( cell.getColumn(), column ) )
          m_table.getFocusHandler().getCursor().redraw();
      }
    } );

    return Status.OK_STATUS;
  }
}