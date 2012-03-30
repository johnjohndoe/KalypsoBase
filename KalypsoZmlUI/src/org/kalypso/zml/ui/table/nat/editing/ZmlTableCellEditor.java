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
package org.kalypso.zml.ui.table.nat.editing;

import net.sourceforge.nattable.edit.editor.EditorSelectionEnum;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.edit.editor.TextCellEditor;

import org.eclipse.swt.widgets.Text;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.labeling.IZmlModelCellLabelProvider;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

/**
 * @author Dirk Kuch
 */
public class ZmlTableCellEditor extends TextCellEditor implements ICellEditor
{
  private final ZmlModelViewport m_viewport;

  public ZmlTableCellEditor( final ZmlModelViewport viewport )
  {
    m_viewport = viewport;
  }

  @Override
  public void setCanonicalValue( final Object canonicalValue )
  {
    final Text text = getTextControl();
    final String displayValue = toDisplayValue( canonicalValue );

    text.setText( displayValue != null && displayValue.length() > 0 ? displayValue.toString() : "" ); //$NON-NLS-1$
    selectText();
  }

  private String toDisplayValue( final Object canonicalValue )
  {
    if( !(canonicalValue instanceof IZmlModelValueCell) )
      return ""; //$NON-NLS-1$

    final IZmlModelValueCell cell = (IZmlModelValueCell) canonicalValue;
    final IZmlModelCellLabelProvider provider = cell.getStyleProvider();

    return provider.getPlainText( m_viewport, cell );
  }

  private void selectText( )
  {
    final Text text = getTextControl();
    final int textLength = text.getText().length();
    if( textLength > 0 )
    {
      final EditorSelectionEnum selectionMode = getSelectionMode();
      if( selectionMode == EditorSelectionEnum.ALL )
      {
        text.setSelection( 0, textLength );
      }
      else if( selectionMode == EditorSelectionEnum.END )
      {
        text.setSelection( textLength, textLength );
      }
    }
  }
}
