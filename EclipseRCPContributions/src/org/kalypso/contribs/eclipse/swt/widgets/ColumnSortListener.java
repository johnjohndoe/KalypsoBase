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
package org.kalypso.contribs.eclipse.swt.widgets;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Item;
import org.kalypso.contribs.eclipse.jface.viewers.ColumnViewerUtil;
import org.kalypso.contribs.eclipse.jface.viewers.InverseSorter;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerColumnItem;

/**
 * Adds sorting support to a {@link org.eclipse.jface.viewers.ViewerColumn}
 * 
 * @author Gernot Belger
 */
public class ColumnSortListener extends SelectionAdapter
{
  private static final String SORT_SORTER = "sortSorter"; //$NON-NLS-1$

  private static final String SORT_COMPARATOR = "comparator"; //$NON-NLS-1$

  private static final String SORT_KEY = "sortViewer"; //$NON-NLS-1$

  private static final String SORT_LISTENER = "sortListener"; //$NON-NLS-1$

  private final ViewerColumnItem m_columnItem;

  public ColumnSortListener( final ViewerColumn column )
  {
    m_columnItem = new ViewerColumnItem( column );

    m_columnItem.addSelectionListener( this );
    /* Remember myself for easy access */
    m_columnItem.setData( SORT_LISTENER, this );
  }

  public void setSorter( final ViewerSorter sorter )
  {
    m_columnItem.setData( SORT_SORTER, null );
    m_columnItem.setData( SORT_COMPARATOR, null );

    m_columnItem.setData( SORT_SORTER, sorter );
  }

  public void setSorter( final ViewerComparator comparator )
  {
    m_columnItem.setData( SORT_SORTER, null );
    m_columnItem.setData( SORT_COMPARATOR, null );

    m_columnItem.setData( SORT_COMPARATOR, comparator );
  }

  @Override
  public void widgetSelected( final SelectionEvent e )
  {
    final Boolean newstate = getToggledState();
    setSortState( newstate );
  }

  protected Boolean getToggledState( )
  {
    // final Item item = ColumnViewerUtil.itemForColumn( columnToSort );
    final Boolean oldstate = (Boolean) m_columnItem.getData( SORT_KEY );
    // Determine newstate depending on oldState ( none -> down -> up )
    if( oldstate == null )
      return Boolean.FALSE;
    else if( !oldstate.booleanValue() )
      return Boolean.TRUE;

    return null;
  }

  /**
   * Activates this sorter and set the sorting state.<br>
   * 
   * @param sortState
   *          The sort state: <code>null</code>: do not sort; <code>true</code>: sort forwards; <code>false</code> sort
   *          backwards
   */
  public void setSortState( final Boolean sortState )
  {
    if( !isSortable() )
      return;

    clearSortKeys();

    m_columnItem.setData( SORT_KEY, sortState );
    applySortState( sortState );

    m_columnItem.setAsSortColumn( sortState );
  }

  /**
   * Set the sorting state of a column.<br>
   * Before calling this method, an instance of this listener has to be instantiated on the given column.
   * 
   * @param sortState
   *          The sort state: <code>null</code>: do not sort; <code>true</code>: sort forwards; <code>false</code> sort
   *          backwards
   */
  public static void setSortState( final ViewerColumn columnToSort, final Boolean sortState )
  {
    final ViewerColumnItem itemToSort = new ViewerColumnItem( columnToSort );
    final Object data = itemToSort.getData( SORT_LISTENER );
    if( !(data instanceof ColumnSortListener) )
      return;

    final ColumnSortListener listener = (ColumnSortListener) data;
    listener.setSortState( sortState );
  }

  private void clearSortKeys( )
  {
    final Item itemToSort = m_columnItem.getColumn();
    final Item[] columns = ColumnViewerUtil.getSisterItems( itemToSort );
    for( final Item column : columns )
      column.setData( SORT_KEY, null );
  }

  private void applySortState( final Boolean sortState )
  {
    final ColumnViewer viewer = m_columnItem.getViewer();

    final ViewerComparator comparator = getComparator();
    final ViewerSorter sorter = getSorter();

    if( comparator != null )
    {
      final ViewerComparator sortComparator = createComparator( comparator, sortState );
      viewer.setComparator( sortComparator );
    }
    else if( sorter != null )
    {
      final ViewerSorter sortSorter = createSorter( sorter, sortState );
      viewer.setComparator( sortSorter );
    }
  }

  private ViewerSorter createSorter( final ViewerSorter sorter, final Boolean sortState )
  {
    if( sortState == null )
      return null;

    if( sortState )
      return new InverseSorter( sorter );

    return sorter;
  }

  private ViewerComparator createComparator( final ViewerComparator comparator, final Boolean sortState )
  {
    if( sortState == null )
      return null;

    if( sortState )
      return new InverseComparator( comparator );

    return comparator;
  }

  private boolean isSortable( )
  {
    final ViewerSorter sorter = getSorter();
    final ViewerComparator comparator = getComparator();
    return sorter != null || comparator != null;
  }

  private ViewerSorter getSorter( )
  {
    final Object data = m_columnItem.getData( SORT_SORTER );
    if( data instanceof ViewerSorter )
      return (ViewerSorter) data;

    return null;
  }

  private ViewerComparator getComparator( )
  {
    final Object data = m_columnItem.getData( SORT_COMPARATOR );
    if( data instanceof ViewerComparator )
      return (ViewerComparator) data;

    return null;
  }
}