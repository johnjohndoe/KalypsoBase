/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.java.swing.table;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;

/**
 * <p>
 * This TableCell editor is a wrapper for TableCellEditors (DecoratorPattern)!
 * </p>
 * <p>
 * This editor behaves exactly as the given editor, except that it tries to select all text of the given editor component, when invoked
 * </p>
 * <p>
 * Iplementation:
 * </p>
 * <p>
 * This editor checks, if the Component of the given editor is a JTextComponent. If so, it just calls 'selectAll' on it
 * </p>
 * 
 * @author belger
 */
public class SelectAllCellEditor implements TableCellEditor
{
  private final TableCellEditor m_editor;

  public SelectAllCellEditor( final TableCellEditor editor )
  {
    m_editor = editor;
  }

  @Override
  public Component getTableCellEditorComponent( final JTable table, final Object value, final boolean isSelected, final int row, final int column )
  {
    final Component c = m_editor.getTableCellEditorComponent( table, value, isSelected, row, column );

    if( c instanceof JTextComponent )
      ((JTextComponent)c).selectAll();

    return c;
  }

  @Override
  public void cancelCellEditing( )
  {
    m_editor.cancelCellEditing();
  }

  @Override
  public boolean stopCellEditing( )
  {
    return m_editor.stopCellEditing();
  }

  @Override
  public Object getCellEditorValue( )
  {
    return m_editor.getCellEditorValue();
  }

  @Override
  public boolean isCellEditable( final EventObject anEvent )
  {
    return m_editor.isCellEditable( anEvent );
  }

  @Override
  public boolean shouldSelectCell( final EventObject anEvent )
  {
    return m_editor.shouldSelectCell( anEvent );
  }

  @Override
  public void addCellEditorListener( final CellEditorListener l )
  {
    m_editor.addCellEditorListener( l );
  }

  @Override
  public void removeCellEditorListener( final CellEditorListener l )
  {
    m_editor.removeCellEditorListener( l );
  }
}
