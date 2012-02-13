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
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.rows.IZmlTableValueRow;
import org.kalypso.zml.ui.table.provider.ZmlLabelProvider;

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
      final IZmlTableSelectionHandler selection = table.getSelectionHandler();

      final StringBuffer buffer = new StringBuffer();

      final IZmlTableColumn[] columns = table.getModel().getColumns();
      for( final IZmlTableColumn column : columns )
      {
        if( !column.isVisible() )
          continue;

        final String text = column.getTableViewerColumn().getColumn().getText();
        buffer.append( text );

        if( !Arrays.isLastItem( columns, column ) )
          buffer.append( "\t" );
      }
      buffer.append( "\n" );

      final IZmlTableValueRow[] rows = selection.getSelectedRows();
      for( final IZmlTableValueRow row : rows )
      {
        for( final IZmlTableColumn column : columns )
        {
          if( !column.isVisible() )
            continue;

          final ZmlLabelProvider provider = new ZmlLabelProvider( row.getModelRow(), null, new ZmlCellRule[] {} );
          buffer.append( provider.getText( null ) );

// if( column.isIndexColumn() )
// {
// final Date date = row.getModelRow().getIndexValue();
//            final SimpleDateFormat sdf = new SimpleDateFormat( "dd.MM.yyyy HH:mm" ); //$NON-NLS-1$
// sdf.setTimeZone( KalypsoCorePlugin.getDefault().getTimeZone() );
//
// buffer.append( sdf.format( date ) );
// }
// else
// {
//
// final IZmlValueReference reference = row.getValueReference( column );
// final Number value = reference.getValue();
// final String text = String.format( "%.3f", value.doubleValue() );
//
// buffer.append( text );
// }

          if( !Arrays.isLastItem( columns, column ) )
            buffer.append( "\t" );
        }

        buffer.append( "\n" );
      }

      final StringSelection clipboardSelection = new StringSelection( buffer.toString() );
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents( clipboardSelection, clipboardSelection );

      return Status.OK_STATUS;
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();

      throw new ExecutionException( "Kopieren der Werte in die Zwischenablage fehlgeschlagen.", ex );
    }
  }

}
