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
package org.kalypso.zml.ui.table.nat;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.print.command.TurnViewportOffCommand;
import net.sourceforge.nattable.print.command.TurnViewportOnCommand;
import net.sourceforge.nattable.resize.MaxCellBoundsHelper;
import net.sourceforge.nattable.resize.command.MultiColumnResizeCommand;
import net.sourceforge.nattable.util.GCFactory;

import org.eclipse.swt.widgets.ScrollBar;

/**
 * @author Gernot Belger
 */
public class ColumnResizeWorker
{
  private final NatTable m_table;

  private final GridLayer m_gridLayer;

  public ColumnResizeWorker( final NatTable table, final GridLayer gridLayer )
  {
    m_table = table;
    m_gridLayer = gridLayer;
  }

  public void execute( )
  {
    final GCFactory gcFactory = new GCFactory( m_table );
    final IConfigRegistry configRegistry = m_table.getConfigRegistry();

    m_gridLayer.doCommand( new TurnViewportOffCommand() );

    /* row header columns -> pack */
    final ILayer rowHeaderLayer = m_gridLayer.getRowHeaderLayer();
    final int[] fixedColumnPositions = createColumnPositions( rowHeaderLayer, 0 );
    final int[] fixedColumnWidths = MaxCellBoundsHelper.getPreferedColumnWidths( configRegistry, gcFactory, m_gridLayer, fixedColumnPositions );
    for( int i = 0; i < fixedColumnWidths.length; i++ )
      fixedColumnWidths[i] += 6;
    m_gridLayer.doCommand( new MultiColumnResizeCommand( rowHeaderLayer, fixedColumnPositions, fixedColumnWidths ) );

    /* now we can calculate the remaining size */
    final int maxViewportWidth = calculateMaxViewPortWidth( fixedColumnWidths );

    /* 'data' columns: never wider than the viewport */
    final ILayer bodyLayer = m_gridLayer.getBodyLayer();
    final int[] bodyColumnPositions = createColumnPositions( bodyLayer, fixedColumnPositions.length );
    final int[] bodyColumnWidths = MaxCellBoundsHelper.getPreferedColumnWidths( configRegistry, gcFactory, m_gridLayer, bodyColumnPositions );
    for( int pos = 0; pos < bodyColumnWidths.length; pos++ )
    {
      final int adjustedWidth = Math.min( bodyColumnWidths[pos], maxViewportWidth );
      bodyColumnWidths[pos] = adjustedWidth;
    }
    m_gridLayer.doCommand( new MultiColumnResizeCommand( m_gridLayer, bodyColumnPositions, bodyColumnWidths ) );

    /* switch viewport on */
    m_gridLayer.doCommand( new TurnViewportOnCommand() );
  }

  private int[] createColumnPositions( final ILayer layer, final int offset )
  {
    final int fixedColumnsCount = layer.getColumnCount();

    final int[] columnPositions = new int[fixedColumnsCount];

    for( int i = 0; i < fixedColumnsCount; i++ )
      columnPositions[i] = i + offset;

    return columnPositions;
  }

  private int calculateMaxViewPortWidth( final int[] fixedColumnWidths )
  {
    /* determine width of fixed columns */
    final int rowHeaderColumnWidth = calculateFixedColumnsWidth( fixedColumnWidths );

    final int tableWidth = m_table.getSize().x;

    final int scrollWidth = calculateScrollbarWidth();

    return Math.max( 0, tableWidth - rowHeaderColumnWidth ) - scrollWidth;
  }

  // FIXME: replace with MathUtils.#sum
  private int calculateFixedColumnsWidth( final int[] fixedColumnWidths )
  {
    int fixedColumnsWidth = 0;

    for( final int fixedColumnWidth : fixedColumnWidths )
      fixedColumnsWidth += fixedColumnWidth;

    return fixedColumnsWidth;
  }

  private int calculateScrollbarWidth( )
  {
    final ScrollBar verticalBar = m_table.getVerticalBar();
    if( verticalBar.isVisible() )
      return verticalBar.getSize().x;
    else
      return 0;
  }
}