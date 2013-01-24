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
package org.kalypso.zml.ui.table.nat.editing;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

/**
 * Paste the contents of the clipbard into the zml table.
 * 
 * @author Gernot Belger
 */
public class ZmlTablePasteWorker
{
  private final IZmlTable m_table;

  public ZmlTablePasteWorker( final IZmlTable table )
  {
    m_table = table;
  }

  public IStatus execute( )
  {
    final ZmlModelViewport viewport = m_table.getModelViewport();
    final IZmlTableSelection selection = m_table.getSelection();

    final IZmlModelValueCell cell = findStartCell( viewport, selection );
    if( cell == null )
      return new Status( IStatus.INFO, KalypsoZmlUI.PLUGIN_ID, "Bitte selektieren Sie die Zelle, ab welcher eingefügt werden soll." );

    final Clipboard clipboard = new Clipboard( PlatformUI.getWorkbench().getDisplay() );

    try
    {
      final IZmlPasteData pasteData = findPasteData( viewport, clipboard );
      if( pasteData == null )
        return new Status( IStatus.INFO, KalypsoZmlUI.PLUGIN_ID, "Die Zwischenablage enthält keine tabellarischen Daten." );

      return doPaste( viewport, cell, pasteData );
    }
    catch( final Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoZmlUI.PLUGIN_ID, "Einfügen des Wertes aus der Zwischenablage fehlgeschlagen.", ex );
    }
    finally
    {
      clipboard.dispose();
    }
  }

  protected IZmlModelValueCell findStartCell( final ZmlModelViewport viewport, final IZmlTableSelection selection )
  {
    int minColumnIndex = Integer.MAX_VALUE;
    int minRowIndex = Integer.MAX_VALUE;

    final IZmlModelColumn[] selectedColumns = selection.getSelectedColumns();
    final IZmlModelRow[] selectedRows = selection.getSelectedRows();

    final IZmlModelColumn[] allColumns = viewport.getColumns();
    final IZmlModelRow[] allRows = viewport.getRows();

    /* find minimal column index */
    for( final IZmlModelColumn column : selectedColumns )
    {
      final int columnIndex = ArrayUtils.indexOf( allColumns, column );
      if( columnIndex != -1 )
        minColumnIndex = Math.min( minColumnIndex, columnIndex );
    }

    /* find minimal row index */
    for( final IZmlModelRow row : selectedRows )
    {
      final int rowIndex = ArrayUtils.indexOf( allRows, row );
      if( rowIndex != -1 )
        minRowIndex = Math.min( minRowIndex, rowIndex );
    }

    if( minColumnIndex == Integer.MAX_VALUE || minRowIndex == Integer.MAX_VALUE )
      return null;

    return viewport.getCell( minRowIndex, minColumnIndex );
  }

  private IStatus doPaste( final ZmlModelViewport viewport, final IZmlModelValueCell cell, final IZmlPasteData pasteData ) throws ExecutionException
  {
    final IStatusCollector log = new StatusCollector( KalypsoZmlUI.PLUGIN_ID );

    final IZmlModelColumn column = cell.getColumn();

    final int startInputColumn = 0;
    final int columnIndex = pasteData.findDataIndex( column, startInputColumn );
    if( columnIndex == -1 )
      throw new ExecutionException( "Keine passende Wertspalte gefunden" );

    IZmlModelValueCell ptr = cell;

    final int rowCount = pasteData.getRowCount();
    for( int rowIndex = 0; rowIndex < rowCount; rowIndex++ )
    {
      if( ptr == null )
      {
        log.add( IStatus.WARNING, "Daten aus Zwischenablage sind länger als die Ziel-Zeitreihe" );
        break;
      }

      final boolean editable = ptr.getColumn().getDataColumn().isEditable();

      try
      {
        if( editable )
        {
          final Object value = pasteData.getData( ptr, columnIndex, rowIndex );
          ptr.doUpdate( value, IDataSourceItem.SOURCE_MANUAL_CHANGED, KalypsoStati.BIT_USER_MODIFIED );
        }
      }
      catch( final Exception e )
      {
        log.add( IStatus.ERROR, "Zeile %d: Lesefehler", e, rowIndex );
      }

      // REMARK: skip cell with bad values, they keep their original data
      ptr = viewport.findNextCell( ptr );
    }

    return log.asMultiStatus( "Fehler beim Einfügen aus der Zwischenablage" );
  }

  private IZmlPasteData findPasteData( final ZmlModelViewport viewport, final Clipboard clipboard ) throws IOException
  {
    final String stringData = (String) clipboard.getContents( TextTransfer.getInstance() );
    final Object[][] tableData = (Object[][]) clipboard.getContents( ZmlTableTransfer.getInstance() );

    if( !ArrayUtils.isEmpty( tableData ) )
      return new ZmlPasteDataInternal( tableData );

    if( stringData != null )
      return new ZmlPasteDataFromString( viewport, stringData );

    return null;
  }
}