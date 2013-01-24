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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.labeling.ZmlModelCellLabelProvider;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.nat.base.ZmlModelRowHeaderDisplayConverter;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

/**
 * Copies the current selection of the table into the clipboard.
 * 
 * @author Gernot Belger
 */
public class ZmlTableCopyWorker
{
  private final IZmlTable m_table;

  public ZmlTableCopyWorker( final IZmlTable table )
  {
    m_table = table;
  }

  public IStatus execute( )
  {
    try
    {
      final ZmlModelViewport viewport = m_table.getModelViewport();
      final IZmlTableSelection selection = m_table.getSelection();

      final IZmlModelColumn[] columns = selection.getSelectedColumns();
      final IZmlModelRow[] rows = selection.getSelectedRows();

      fillClipboard( viewport, columns, rows );

      return Status.OK_STATUS;
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();

      return new Status( IStatus.ERROR, KalypsoZmlUI.PLUGIN_ID, "Kopieren der Werte in die Zwischenablage fehlgeschlagen.", ex );
    }
  }

  private void fillClipboard( final ZmlModelViewport viewport, final IZmlModelColumn[] columns, final IZmlModelRow[] rows ) throws SensorException
  {
    final StringBuilder buffer = new StringBuilder();
    final Collection<Object[]> dataBuffer = new ArrayList<Object[]>();

    /** table header */
    final Collection<String> headerBuffer = new ArrayList<String>();
    // FIXME: why hard coded?
    headerBuffer.add( "Datum" );

    for( final IZmlModelColumn column : columns )
      headerBuffer.add( column.getLabel() );

    buffer.append( StringUtils.join( headerBuffer, '\t' ) );
    buffer.append( '\n' );

    /** table body */
    for( final IZmlModelRow row : rows )
    {
      final Collection<String> rowStrings = new ArrayList<String>();
      final Collection<Object> rowData = new ArrayList<Object>();

      final String date = ZmlModelRowHeaderDisplayConverter.toLabel( row.getIndexCell() );
      rowStrings.add( date );

      for( final IZmlModelColumn column : columns )
      {
        final IZmlModelValueCell cell = row.get( column );
        if( cell == null )
          // FIXME: why not the empy string instead?
          rowStrings.add( " " );
        else
        {
          final ZmlModelCellLabelProvider provider = new ZmlModelCellLabelProvider( column );

          final String cellText = provider.getText( viewport, cell );
          final Object cellValue = cell.getValue();

          rowStrings.add( cellText );
          rowData.add( cellValue );
        }
      }

      buffer.append( StringUtils.join( rowStrings, '\t' ) );
      buffer.append( '\n' );

      dataBuffer.add( rowData.toArray( new Object[rowData.size()] ) );
    }

    final String stringRepresantation = StringUtils.chomp( buffer.toString() );
    final Object[][] tableData = dataBuffer.toArray( new Object[dataBuffer.size()][] );

    /* put into clipboard */
    final Clipboard clipboard = new Clipboard( PlatformUI.getWorkbench().getDisplay() );
    try
    {
      final Object[] clipboardData = new Object[] { stringRepresantation, tableData };
      final Transfer[] dataTypes = new Transfer[] { TextTransfer.getInstance(), ZmlTableTransfer.getInstance() };
      clipboard.setContents( clipboardData, dataTypes );
    }
    finally
    {
      clipboard.dispose();
    }
  }
}