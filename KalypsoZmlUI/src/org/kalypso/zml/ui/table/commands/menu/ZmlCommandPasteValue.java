/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  DenickestraÃŸe 22
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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.model.cells.IZmlTableValueCell;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.provider.strategy.editing.IZmlEditingStrategy;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandPasteValue extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    try
    {
      final IZmlTable table = ZmlHandlerUtil.getTable( event );
      final IZmlTableSelectionHandler selection = table.getSelectionHandler();
      final IZmlTableValueCell cell = (IZmlTableValueCell) selection.findActiveCellByPosition();
      if( cell.getColumn().isIndexColumn() )
        throw new ExecutionException( "Aktualisierung von Index-Spalten nicht möglich!" );

      IZmlTableValueCell ptr = cell;

      final IZmlTableColumn column = cell.getColumn();
      final IZmlEditingStrategy strategy = column.getEditingStrategy();

      final String[] data = getData();
      for( final String value : data )
      {
        if( Objects.isNull( ptr ) )
          break;

        strategy.setValue( ptr.getRow().getModelRow(), value );

        ptr = ptr.findNextCell();
      }

      return Status.OK_STATUS;
    }
    catch( final Exception ex )
    {
      throw new ExecutionException( "Einfügen des Wertes aus der Zwischenablage fehlgeschlagen.", ex );
    }

  }

  private String[] getData( ) throws IOException, ExecutionException
  {
    final Clipboard clipboard = new Clipboard( PlatformUI.getWorkbench().getDisplay() );
    final TextTransfer transfer = TextTransfer.getInstance();
    final String data = (String) clipboard.getContents( transfer );

    final List<String> strings = new ArrayList<String>();

    final CSVReader reader = new CSVReader( new StringReader( data ), '\t' );
    final List<String[]> rows = reader.readAll();
    for( final String[] row : rows )
    {
      if( row.length != 1 )
        throw new ExecutionException( "Ungültige Anzahl Spalten. Nur Verarbeitung von einer Spalte möglich!" );

      strings.add( row[0] );
    }

    return strings.toArray( new String[] {} );
  }
}
