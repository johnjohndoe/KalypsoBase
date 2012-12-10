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
package org.kalypso.zml.ui.table.nat.context.menu;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.cell.LayerCell;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

/**
 * @author Dirk Kuch
 */
public class NatTableContextMenuSupport extends MouseAdapter
{
  protected final MenuManager m_manager = new MenuManager();

  private final NatTable m_table;

  private final ZmlModelViewport m_viewport;

  private final Menu m_contextMenu;

  public static int SELECTED_COLUMN;

  private final IZmlTableSelection m_selection;

  public NatTableContextMenuSupport( final NatTable table, final ZmlModelViewport viewport, final IZmlTableSelection selection )
  {
    m_table = table;
    m_viewport = viewport;
    m_selection = selection;

    m_contextMenu = m_manager.createContextMenu( m_table );
    m_table.setMenu( m_contextMenu );
  }

  @Override
  public void mouseDown( final MouseEvent e )
  {
    if( e.button == 3 )
    {
      m_manager.removeAll();

      final int column = m_table.getColumnPositionByX( e.x );
      final int row = m_table.getRowPositionByY( e.y );
      if( column < 0 || row < 0 )
        return;

      if( row == 0 )
      {
        /** table header selection */
        final IZmlModelColumn colum = m_viewport.getColum( column - 1 );
        if( Objects.isNull( colum ) )
          return;

        final ZmlTableHeaderContextMenuProvider menuProvider = new ZmlTableHeaderContextMenuProvider();
        menuProvider.fillMenu( m_viewport, colum, m_manager );
      }
      else
      {
        /** table cell was selected */
        final LayerCell cell = m_table.getCellByPosition( column, row );
        final Object objDataValue = cell.getDataValue();
        if( !(objDataValue instanceof IZmlModelValueCell) )
          return;

        final IZmlModelValueCell modelCell = (IZmlModelValueCell) objDataValue;

        final int columnIndexByPosition = m_table.getColumnIndexByPosition( column );
        final int rowIndexByPosition = m_table.getRowIndexByPosition( row );

        m_selection.updateLastSelectedCellPosition( rowIndexByPosition, columnIndexByPosition );

        final ZmlTableContextMenuProvider menuProvider = new ZmlTableContextMenuProvider();
        menuProvider.fillMenu( modelCell.getColumn(), m_manager );
      }

      SELECTED_COLUMN = column - 1;

      m_manager.update( true );
      m_contextMenu.setVisible( true );
    }
  }
}
