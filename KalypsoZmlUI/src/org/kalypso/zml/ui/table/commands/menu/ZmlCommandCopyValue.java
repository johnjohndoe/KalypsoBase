/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.commands.menu;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.labeling.ZmlModelCellLabelProvider;
import org.kalypso.zml.ui.i18n.Messages;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.nat.base.ZmlModelRowHeaderDisplayConverter;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandCopyValue extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    try
    {
      final IZmlTable table = ZmlHandlerUtil.getTable( event );
      final IZmlTableSelection selection = table.getSelection();

      final StringBuffer buffer = new StringBuffer();

      final IZmlModelColumn[] columns = selection.getSelectedColumns();
      final IZmlModelRow[] rows = selection.getSelectedRows();

      /** table header */
      buffer.append( Messages.ZmlCommandCopyValue_0 );
      buffer.append( "\t" ); //$NON-NLS-1$

      for( final IZmlModelColumn column : columns )
      {
        buffer.append( column.getLabel() );
        if( !Arrays.isLastItem( columns, column ) )
          buffer.append( "\t" ); //$NON-NLS-1$
        else
          buffer.append( "\n" );//$NON-NLS-1$
      }

      /** table body */
      for( final IZmlModelRow row : rows )
      {
        final String date = ZmlModelRowHeaderDisplayConverter.toLabel( row.getIndexCell() );
        buffer.append( date );
        buffer.append( "\t" ); //$NON-NLS-1$

        for( final IZmlModelColumn column : columns )
        {
          final IZmlModelValueCell cell = row.get( column );
          if( cell == null )
            buffer.append( " " ); //$NON-NLS-1$
          else
          {
            final ZmlModelCellLabelProvider provider = new ZmlModelCellLabelProvider( column );
            buffer.append( provider.getText( table.getModelViewport(), cell ) );
          }

          if( !Arrays.isLastItem( columns, column ) )
            buffer.append( "\t" ); //$NON-NLS-1$
          else
            buffer.append( "\n" ); //$NON-NLS-1$
        }
      }

      final StringSelection clipboardSelection = new StringSelection( buffer.toString() );
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents( clipboardSelection, clipboardSelection );

      return Status.OK_STATUS;
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();

      throw new ExecutionException( Messages.ZmlCommandCopyValue_3, ex );
    }
  }

}
