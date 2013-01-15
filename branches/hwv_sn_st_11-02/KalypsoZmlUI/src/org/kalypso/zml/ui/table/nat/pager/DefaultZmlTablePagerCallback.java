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
package org.kalypso.zml.ui.table.nat.pager;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.selection.event.CellSelectionEvent;
import net.sourceforge.nattable.selection.event.RowSelectionEvent;
import net.sourceforge.nattable.viewport.command.ShowRowInViewportCommand;
import net.sourceforge.nattable.viewport.event.ScrollEvent;

import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.nat.layers.BodyLayerStack;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

/**
 * @author Holger Albert
 */
public class DefaultZmlTablePagerCallback implements IZmlTablePagerCallback
{
  private final ILayerListener m_listener = new ILayerListener()
  {
    @Override
    public void handleLayerEvent( final ILayerEvent event )
    {
      doHandleLayerEvent( event );
    }
  };

  private final IZmlTable m_zmlTable;

  private boolean m_firstRun;

  private Date m_lastRow;

  private Date m_focusRow;

  private final Set<Date> m_selectedRows = new HashSet<Date>();

  private boolean m_handleEvents = true;

  public DefaultZmlTablePagerCallback( final IZmlTable zmlTable )
  {
    m_zmlTable = zmlTable;
    m_firstRun = true;
    m_lastRow = null;

    final NatTable table = zmlTable.getTable();
    table.addLayerListener( m_listener );
  }

  protected void doHandleLayerEvent( final ILayerEvent event )
  {
    if( !m_handleEvents )
      return;

    if( event instanceof ScrollEvent )
    {
      // choose last row and the table paging will be correct - in most cases :-)
      final NatTable table = m_zmlTable.getTable();
      final int rowCount = table.getRowCount();
      final LayerCell cell = table.getCellByPosition( 1, rowCount - 1 );
      if( cell == null )
        return;

      final Object dataValue = cell.getDataValue();
      if( dataValue instanceof IZmlModelCell )
      {
        final IZmlModelCell modelCell = (IZmlModelCell) dataValue;
        m_lastRow = modelCell.getIndexValue();
      }
    }

    if( event instanceof RowSelectionEvent )
      rememberSelection();

    if( event instanceof CellSelectionEvent )
      rememberSelection();
  }

  private void rememberSelection( )
  {
    final IZmlTableSelection selection = m_zmlTable.getSelection();

    final IZmlModelValueCell focusCell = selection.getFocusCell();
    updateFocusCell( focusCell );

    m_selectedRows.clear();
    final IZmlModelRow[] selectedRows = selection.getSelectedRows();
    for( final IZmlModelRow selectedRow : selectedRows )
    {
      final Date index = selectedRow.getIndex();
      m_selectedRows.add( index );
    }
  }

  private void updateFocusCell( final IZmlModelValueCell focusCell )
  {
    if( focusCell == null )
      m_focusRow = null;
    else
    {
      final IZmlModelRow row = focusCell.getRow();
      if( row == null )
        m_focusRow = null;
      else
      {
        m_focusRow = row.getIndex();
        System.out.format( "Set focus row to index %s%n", m_focusRow );
      }
    }
  }

  @Override
  public void beforeRefresh( )
  {
    // freeze update, because the refresh of the table will trigger selection events
    m_handleEvents = false;
  }

  @Override
  public void afterRefresh( )
  {
    try
    {
      applySelections();
    }
    finally
    {
      m_handleEvents = true;
    }
  }

  private void applySelections( )
  {
    final BodyLayerStack bodyLayer = m_zmlTable.getBodyLayer();

    final Date[] selectedRows = m_selectedRows.toArray( new Date[m_selectedRows.size()] );
    final Date focusRow = m_focusRow;

    /* select the same rows as before */
    for( final Date selected : selectedRows )
    {
      if( !selected.equals( focusRow ) )
        selectRow( bodyLayer, selected );
    }

    /* focus the same row as before */
    if( focusRow != null )
      selectRow( bodyLayer, focusRow );

    /* scroll viewport as it was before */
    final Date date = findLastRowDate( m_zmlTable );
    final int index = FindClosestDateVisitor.findRowIndex( m_zmlTable, date );
    if( index != -1 )
    {
      final ShowRowInViewportCommand command = new ShowRowInViewportCommand( bodyLayer, index );
      m_zmlTable.getTable().doCommand( command );
    }
  }

  private void selectRow( final BodyLayerStack bodyLayer, final Date row )
  {
    final int rowIndex = FindClosestDateVisitor.findRowIndex( m_zmlTable, row );
    if( rowIndex == -1 )
      return;

    final int columnCount = bodyLayer.getColumnCount();
    for( int column = 0; column < columnCount; column++ )
    {
      final SelectCellCommand cmd = new SelectCellCommand( bodyLayer.getSelectionLayer(), column, rowIndex, false, true );
      m_zmlTable.getTable().doCommand( cmd );
    }
  }

  @Override
  public void dispose( )
  {
    final NatTable table = m_zmlTable.getTable();
    table.removeLayerListener( m_listener );
  }

  private Date findLastRowDate( final IZmlTable table )
  {
    Date date = null;
    if( m_firstRun )
      date = findForecastDate( table );

    if( date == null )
      return m_lastRow;

    return date;
  }

  private Date findForecastDate( final IZmlTable table )
  {
    final ZmlModelViewport viewport = table.getModelViewport();
    final IZmlModelColumn[] columns = viewport.getColumns();

    final Date date = findForecastDate( columns );
    if( date == null )
      return null;

    m_firstRun = false;

    return date;
  }

  private Date findForecastDate( final IZmlModelColumn[] columns )
  {
    for( final IZmlModelColumn column : columns )
    {
      if( !column.isMetadataSource() )
        continue;

      final Date date = MetadataHelper.getForecastStart( column.getMetadata() );
      if( date != null )
        return date;
    }

    return null;
  }
}