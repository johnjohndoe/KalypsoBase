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
package org.kalypso.zml.ui.table.nat.editing;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.edit.ICellEditHandler;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.edit.editor.IEditErrorHandler;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.widget.EditModeEnum;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.core.table.schema.DataColumnType;

/**
 * One zml table cell editor to handle all data types.
 * 
 * @author Dirk Kuch
 */
public class ZmlTableCellEditorFacade implements ICellEditor
{
  private final ZmlTableTextCellEditor m_textBoxEditor;

  private final ZmlTableCheckBoxCellEditor m_checkBoxEditor;

  private IZmlTableCellEditor m_lastEditor;

  public ZmlTableCellEditorFacade( final ZmlModelViewport viewport )
  {
    m_textBoxEditor = new ZmlTableTextCellEditor( viewport );
    m_checkBoxEditor = new ZmlTableCheckBoxCellEditor();
  }

  private IZmlTableCellEditor getCellEditor( final String type )
  {
    if( StringUtils.equalsIgnoreCase( ITimeseriesConstants.TYPE_POLDER_CONTROL, type ) )
    {
      m_lastEditor = m_checkBoxEditor;
      return m_checkBoxEditor;
    }

    m_lastEditor = m_textBoxEditor;
    return m_textBoxEditor;
  }

  @Override
  public Control activateCell( final Composite parent, final Object originalCanonicalValue, final Character initialEditValue, final EditModeEnum editMode, final ICellEditHandler editHandler, final LayerCell cell, final IConfigRegistry configRegistry )
  {
    if( originalCanonicalValue instanceof IZmlModelValueCell )
    {
      final IZmlModelValueCell zml = (IZmlModelValueCell) originalCanonicalValue;
      final IZmlModelColumn column = zml.getColumn();
      final DataColumnType type = column.getDataColumn().getType();

      final IZmlTableCellEditor editor = getCellEditor( type.getValueAxis() );
      return editor.doActivateCell( parent, originalCanonicalValue, initialEditValue, editMode, editHandler, cell, configRegistry );
    }

    return null;
  }

  @Override
  public boolean validateCanonicalValue( )
  {
    return m_lastEditor.validateCanonicalValue();
  }

  @Override
  public boolean validateCanonicalValue( final IEditErrorHandler conversionErrorHandler, final IEditErrorHandler validationErrorHandler )
  {
    return m_lastEditor.validateCanonicalValue( conversionErrorHandler, validationErrorHandler );
  }

  @Override
  public boolean commit( final MoveDirectionEnum direction, final boolean closeAfterCommit )
  {
    return m_lastEditor.commit( direction, closeAfterCommit );
  }

  @Override
  public void close( )
  {
    m_lastEditor.close();
  }

  @Override
  public boolean isClosed( )
  {
    return m_lastEditor.isClosed();
  }

  @Override
  public void setCanonicalValue( final Object canonicalValue )
  {
    m_lastEditor.setCanonicalValue( canonicalValue );
  }

  @Override
  public Object getCanonicalValue( )
  {
    return m_lastEditor.getCanonicalValue();
  }
}
