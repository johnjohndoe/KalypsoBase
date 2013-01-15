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
package org.kalypso.zml.ui.table.nat.layers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.selection.SelectionLayer;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

/**
 * @author Dirk Kuch
 */
public class ZmlTableSelectionLayer extends SelectionLayer implements IZmlTableSelection
{
  List<IZmlTableSelectionListener> m_listener = new ArrayList<IZmlTableSelectionListener>();

  private final ZmlModelViewport m_model;

  private IZmlModelColumn m_clickedHeaderColumn;

  public ZmlTableSelectionLayer( final ZmlModelViewport model, final IUniqueIndexLayer layer )
  {
    super( layer );

    m_model = model;
    m_clickedHeaderColumn = null;
  }

  @Override
  public IZmlModelColumn[] getSelectedColumns( )
  {
    final Set<IZmlModelColumn> selection = new LinkedHashSet<IZmlModelColumn>();

    final int[] columns = getSelectedColumnPositions();
    for( final int index : columns )
    {
      final IZmlModelColumn column = m_model.getColum( index );
      selection.add( column );
    }

    return selection.toArray( new IZmlModelColumn[] {} );
  }

  public IZmlModelRow[] getSelectedRows( final IZmlModelColumn column )
  {
    final Set<IZmlModelRow> selection = new TreeSet<IZmlModelRow>( IZmlModelRow.COMPARATOR );

    final IZmlModelRow[] modelRows = m_model.getRows();

    final PositionCoordinate[] positions = getSelectedCellPositions();
    for( final PositionCoordinate position : positions )
    {
      final IZmlModelColumn cellColumn = m_model.getColum( position.getColumnPosition() );
      if( Objects.equal( column, cellColumn ) )
      {
        final int rowIndex = position.getRowPosition();
        if( rowIndex == -1 )
          continue;

        if( ArrayUtils.getLength( modelRows ) > rowIndex )
          selection.add( modelRows[rowIndex] );
      }
    }

    return selection.toArray( new IZmlModelRow[] {} );
  }

  @Override
  public IZmlModelRow[] getSelectedRows( )
  {
    final Set<IZmlModelRow> selection = new TreeSet<IZmlModelRow>( IZmlModelRow.COMPARATOR );

    final IZmlModelRow[] modelRows = m_model.getRows();

    final Set<Range> selectedRowPositions = getSelectedRowPositions();
    for( final Range range : selectedRowPositions )
    {
      final Set<Integer> rows = range.getMembers();
      for( final Integer rowIndex : rows )
      {
        if( rowIndex == -1 )
          continue;

        if( ArrayUtils.getLength( modelRows ) > rowIndex )
          selection.add( modelRows[rowIndex] );
      }
    }

    return selection.toArray( new IZmlModelRow[] {} );
  }

  @Override
  public IZmlModelValueCell[] getSelectedCells( )
  {
    final Set<IZmlModelCell> selection = new LinkedHashSet<IZmlModelCell>();

    final IZmlModelColumn[] columns = getSelectedColumns();
    final IZmlModelRow[] rows = getSelectedRows();
    for( final IZmlModelRow row : rows )
    {
      for( final IZmlModelColumn column : columns )
        selection.add( row.get( column ) );
    }

    return selection.toArray( new IZmlModelValueCell[] {} );
  }

  @Override
  public IZmlModelValueCell getFocusCell( )
  {
    final PositionCoordinate position = getSelectionAnchor();
// final PositionCoordinate position = getLastSelectedCellPosition();
    if( position == null )
      return null;

    return m_model.getCell( position.getRowPosition(), position.getColumnPosition() );
  }

  @Override
  public IZmlModelValueCell[] getSelectedCells( final IZmlModelColumn column )
  {
    final Set<IZmlModelCell> selection = new LinkedHashSet<IZmlModelCell>();
    final IZmlModelRow[] rows = getSelectedRows( column );
    for( final IZmlModelRow row : rows )
    {
      final IZmlModelValueCell cell = row.get( column );
      if( Objects.isNotNull( cell ) )
        selection.add( cell );
    }

    return selection.toArray( new IZmlModelValueCell[] {} );
  }

  @Override
  public void updateLastSelectedCellPosition( final int row, final int column )
  {
    setLastSelectedCell( column, row );
  }

  @Override
  public IZmlModelColumn getClickedHeaderColumn( )
  {
    return m_clickedHeaderColumn;
  }

  @Override
  public void setClickedHeaderColumn( final IZmlModelColumn clickedHeaderColumn )
  {
    m_clickedHeaderColumn = clickedHeaderColumn;

    fireClickedHeaderColumnChanged( clickedHeaderColumn );
  }

  @Override
  public void addSelectionListener( final IZmlTableSelectionListener listener )
  {
    if( !m_listener.contains( listener ) )
      m_listener.add( listener );
  }

  @Override
  public void removeSelectionListener( final IZmlTableSelectionListener listener )
  {
    if( m_listener.contains( listener ) )
      m_listener.remove( listener );
  }

  private void fireClickedHeaderColumnChanged( final IZmlModelColumn clickedHeaderColumn )
  {
    for( final IZmlTableSelectionListener listener : m_listener )
      listener.clickedHeaderColumnChanged( clickedHeaderColumn );
  }
}