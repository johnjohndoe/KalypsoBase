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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

/**
 * An cell editor listener whichs changes the background color of the cell editor if the value is not valid.
 * <p>
 * Also sets the tooltiptext to the error message if an error occurs.
 * </p>
 * <p>
 * Unhooks itself from the editor as soon as editing stops.
 * </p>
 * 
 * @author Gernot Belger
 */
public class ValidateCellEditorListener implements ICellEditorListener
{
  private final CellEditor m_cellEditor;

  private final Color m_errorColor;

  public ValidateCellEditorListener( final CellEditor cellEditor, final Color errorColor )
  {
    m_cellEditor = cellEditor;
    m_errorColor = errorColor;
  }

  /**
   * @see org.eclipse.jface.viewers.ICellEditorListener#applyEditorValue()
   */
  @Override
  public void applyEditorValue( )
  {
    m_cellEditor.removeListener( this );

    /* Reset state of editor for next editing */
    validateEditor( true, m_cellEditor.getControl() );
  }

  /**
   * @see org.eclipse.jface.viewers.ICellEditorListener#cancelEditor()
   */
  @Override
  public void cancelEditor( )
  {
    m_cellEditor.removeListener( this );

    /* Reset state of editor for next editing */
    validateEditor( true, m_cellEditor.getControl() );
  }

  /**
   * @see org.eclipse.jface.viewers.ICellEditorListener#editorValueChanged(boolean, boolean)
   */
  @Override
  public void editorValueChanged( final boolean oldValidState, final boolean newValidState )
  {
    final Control control = m_cellEditor.getControl();
    validateEditor( newValidState, control );
  }

  private void validateEditor( final boolean newValidState, final Control control )
  {
    if( control == null || control.isDisposed() )
      return;

    if( newValidState )
    {
      control.setBackground( null );
      control.setToolTipText( null );
    }
    else
    {
      control.setBackground( m_errorColor );
      control.setToolTipText( m_cellEditor.getErrorMessage() );
    }
  }

}
