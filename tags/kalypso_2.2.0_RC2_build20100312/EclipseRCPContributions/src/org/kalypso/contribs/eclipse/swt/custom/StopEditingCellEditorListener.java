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
package org.kalypso.contribs.eclipse.swt.custom;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TableViewer;

/**
 * This cell editor listeners call the excel table cursor, when the cell editor is stopped and stopes editing.
 * 
 * @author Gernot Belger
 */
public class StopEditingCellEditorListener implements ICellEditorListener
{
  private final ExcelTableCursor m_cursor;

  private final CellEditor m_cellEditor;

  private final TableViewer m_viewer;

  public StopEditingCellEditorListener( final CellEditor cellEditor, final ExcelTableCursor cursor, final TableViewer viewer )
  {
    m_cellEditor = cellEditor;
    m_cursor = cursor;
    m_viewer = viewer;
  }

  /**
   * @see org.eclipse.jface.viewers.ICellEditorListener#applyEditorValue()
   */
  public void applyEditorValue( )
  {
    m_cellEditor.removeListener( this );

    m_cursor.stopEditing( m_cellEditor.getControl() );
  }

  /**
   * @see org.eclipse.jface.viewers.ICellEditorListener#cancelEditor()
   */
  public void cancelEditor( )
  {
    m_cellEditor.removeListener( this );

    /* Call cancel editing first, because else the following focus loss will aply the editor value*/
    m_viewer.cancelEditing();
    
    m_cursor.stopEditing( m_cellEditor.getControl() );
  }

  /**
   * @see org.eclipse.jface.viewers.ICellEditorListener#editorValueChanged(boolean, boolean)
   */
  public void editorValueChanged( boolean oldValidState, boolean newValidState )
  {
  }

}
