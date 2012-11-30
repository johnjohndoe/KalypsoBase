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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.editing.IZmlEditingStrategy;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.i18n.Messages;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

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
      final IZmlTableSelection selection = table.getSelection();
      final IZmlModelValueCell cell = selection.getFocusCell();
      if( cell == null )
        return Status.CANCEL_STATUS;

      final IZmlModelColumn column = cell.getColumn();

      final ZmlModelViewport viewport = table.getModelViewport();
      final IZmlEditingStrategy strategy = viewport.getEditingStrategy( column );

      IZmlModelValueCell ptr = cell;

      final String[] data = getData( column );
      for( final String value : data )
      {
        if( Objects.isNull( value ) )
          break;
        else if( StringUtils.isBlank( value ) )
          break;

        strategy.setValue( ptr, value );
        ptr = viewport.findNextCell( ptr );
      }

      return Status.OK_STATUS;
    }
    catch( final Exception ex )
    {
      throw new ExecutionException( Messages.ZmlCommandPasteValue_0, ex );
    }

  }

  private String[] getData( final IZmlModelColumn column ) throws IOException, ExecutionException
  {
    final String type = column.getDataColumn().getValueAxis();

    final Clipboard clipboard = new Clipboard( PlatformUI.getWorkbench().getDisplay() );
    final TextTransfer transfer = TextTransfer.getInstance();
    final String data = (String) clipboard.getContents( transfer );

    final List<String> strings = new ArrayList<>();

    final CSVReader reader = new CSVReader( new StringReader( data ), '\t' );
    final List<String[]> rows = reader.readAll();
    if( rows.isEmpty() )
      return new String[] {};

    int index = getIndex( rows.get( 0 ), type );
    if( index == -1 )
    {
      rows.remove( 0 );// header row
      if( !rows.isEmpty() )
        index = getIndex( rows.get( 0 ), type );
    }

    if( index == -1 )
      throw new ExecutionException( Messages.ZmlCommandPasteValue_1 );

    for( final String[] row : rows )
    {
      strings.add( row[index] );
    }

    return strings.toArray( new String[] {} );
  }

  private int getIndex( final String[] row, final String type )
  {

    for( int index = 0; index < ArrayUtils.getLength( row ); index++ )
    {
      try
      {
        final String cell = row[index];

        if( StringUtils.equalsIgnoreCase( ITimeseriesConstants.TYPE_POLDER_CONTROL, type ) )
        {
          if( StringUtils.equalsIgnoreCase( "true", cell ) ) //$NON-NLS-1$
            return index;
          else if( StringUtils.equalsIgnoreCase( "false", cell ) ) //$NON-NLS-1$
            return index;
        }
        else
        {
          final double value = NumberUtils.parseDouble( cell );
          if( Double.isNaN( value ) )
            continue;

          return index;
        }

      }
      catch( final Throwable t )
      {
        // nothing to do
      }
    }

    return -1;
  }
}
