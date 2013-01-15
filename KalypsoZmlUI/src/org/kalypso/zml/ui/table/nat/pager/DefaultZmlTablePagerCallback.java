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
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.selection.event.CellSelectionEvent;
import net.sourceforge.nattable.selection.event.RowSelectionEvent;
import net.sourceforge.nattable.viewport.ViewportLayer;
import net.sourceforge.nattable.viewport.event.ScrollEvent;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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

  private final ControlAdapter m_resizeListener = new ControlAdapter()
  {
    @Override
    public void controlResized( final ControlEvent e )
    {
      handleTableResize();
    }
  };

  private final IZmlTable m_zmlTable;

  private boolean m_firstRun = true;

  private Date m_originRow = null;

  private Date m_focusRow = null;

  private final Set<Date> m_selectedRows = new HashSet<Date>();

  private boolean m_handleEvents = true;

  public DefaultZmlTablePagerCallback( final IZmlTable zmlTable )
  {
    m_zmlTable = zmlTable;

    final NatTable table = zmlTable.getTable();
    table.addLayerListener( m_listener );

    table.addControlListener( m_resizeListener );
  }

  @Override
  public void dispose( )
  {
    final NatTable table = m_zmlTable.getTable();
    table.removeLayerListener( m_listener );

    table.removeControlListener( m_resizeListener );
  }

  protected void handleTableResize( )
  {
    // BUGFIX: if the table is initially not visible, the whole forecast date selection does not work
    // as the viewport size is set to 0.
    if( m_firstRun )
      afterRefresh();
  }

  protected void doHandleLayerEvent( final ILayerEvent event )
  {
    if( !m_handleEvents )
      return;

    if( event instanceof ScrollEvent )
      rememberOrigin();

    if( event instanceof RowSelectionEvent )
      rememberSelection();

    if( event instanceof CellSelectionEvent )
      rememberSelection();
  }

  private void rememberOrigin( )
  {
    /* remember current origin of viewport in order to restore it later */
    final BodyLayerStack bodyLayer = m_zmlTable.getBodyLayer();
    final ViewportLayer viewportLayer = bodyLayer.getViewportLayer();
    final int originRowPosition = viewportLayer.getOriginRowPosition();
    final LayerCell originCell = viewportLayer.getScrollableLayer().getCellByPosition( 0, originRowPosition );
    if( originCell != null )
    {
      final Object originValue = originCell.getDataValue();
      if( originValue instanceof IZmlModelCell )
      {
        final IZmlModelCell modelCell = (IZmlModelCell) originValue;
        m_originRow = modelCell.getIndexValue();
      }
    }
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
        m_focusRow = row.getIndex();
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
    if( m_zmlTable.getModelViewport().getColumns().length == 0 )
      return;

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
    if( m_firstRun )
    {
      if( makeForecastDateVisible() )
      {
        m_firstRun = false;
        m_zmlTable.getTable().removeControlListener( m_resizeListener );
      }
    }
    else
      resetOrigin();
  }

  private boolean makeForecastDateVisible( )
  {
    final Date forecastDate = findForecastDate( m_zmlTable );
    final int index = FindClosestDateVisitor.findRowIndex( m_zmlTable, forecastDate );
    if( index == -1 )
      return true;

    final ViewportLayer viewportLayer = m_zmlTable.getBodyLayer().getViewportLayer();
    final int viewportRows = viewportLayer.getRowCount();
    if( viewportRows == 0 )
    {
      /*
       * probably table is invisible, we cannot correctly determine the origin of the forecast row, i.e. we do not do it
       * now but later...
       */
      if( !m_zmlTable.getTable().isVisible() )
        return false;
    }

    /* forecast date will be in middle of table */
    final int wishIndexToSet = Math.max( 0, index - viewportRows / 2 + 1 );

    setRowOrigin( wishIndexToSet );

    m_originRow = forecastDate;

    rememberOrigin();

    return true;
  }

  private void setRowOrigin( final int wishIndexToSet )
  {
    final ViewportLayer viewportLayer = m_zmlTable.getBodyLayer().getViewportLayer();
    final IUniqueIndexLayer dataLayer = viewportLayer.getScrollableLayer();

    final int viewportRows = viewportLayer.getRowCount();
    final int dataRows = dataLayer.getRowCount();

    /*
     * if > 0, we would scroll outside the viewport (empty are below last element), this is not ok, because next
     * scrolling will compensate for this and will scroll a big amount at once
     */
    final int underCut = viewportRows - (dataRows - wishIndexToSet);

    final int indexToSet;

    if( underCut > 0 )
      indexToSet = Math.max( 0, wishIndexToSet - underCut );
    else
      indexToSet = wishIndexToSet;

    viewportLayer.setOriginRowPosition( indexToSet );
  }

  private void resetOrigin( )
  {
    final Date date = m_originRow;
    if( date == null )
      return;

    final int index = FindClosestDateVisitor.findRowIndex( m_zmlTable, date );
    if( index == -1 )
      return;

    setRowOrigin( index );
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

  private Date findForecastDate( final IZmlTable table )
  {
    final ZmlModelViewport viewport = table.getModelViewport();
    final IZmlModelColumn[] columns = viewport.getColumns();

    final Date date = findForecastDate( columns );
    if( date == null )
      return null;

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