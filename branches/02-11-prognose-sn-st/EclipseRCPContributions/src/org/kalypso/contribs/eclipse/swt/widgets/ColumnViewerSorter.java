/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.eclipse.swt.widgets;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.contribs.eclipse.ImageProvider;
import org.kalypso.contribs.eclipse.jface.viewers.ColumnViewerUtil;
import org.kalypso.contribs.eclipse.jface.viewers.InverseSorter;

/**
 * Adds a common sorting mechanism to tables and trees.
 * 
 * @author Gernot Belger
 */
public final class ColumnViewerSorter
{
  /**
   * Registers a {@link ViewerSorter} with a column. If the sorter is registered, clicking on the column header will
   * toggle the sorting state for this column.
   */
  public static void registerSorter( final ViewerColumn column, final ViewerSorter sorter )
  {
    final Item item = ColumnViewerUtil.itemForColumn( column );
    item.setData( SORT_SORTER, sorter );

    item.addListener( SWT.Selection, new Listener()
    {
      @Override
      public void handleEvent( final Event event )
      {
        final Boolean newstate = getToggledState( column );
        setSortState( column, newstate );
      }
    } );
  }

  public static final String SORT_KEY = "sortViewer";

  private static final String SORT_SORTER = "sortSorter";

  private static final Image m_emptyImage = ImageProvider.ID_EMPTY.createImage();

  private static final Image m_downImage = ImageProvider.ID_SORT_DOWN.createImage();

  private static final Image m_upImage = ImageProvider.ID_SORT_UP.createImage();

  private ColumnViewerSorter( )
  {
    throw new UnsupportedOperationException();
  }

  protected static Boolean getToggledState( final ViewerColumn columnToSort ) throws IllegalArgumentException
  {
    final Item item = ColumnViewerUtil.itemForColumn( columnToSort );
    final Boolean oldstate = (Boolean) item.getData( SORT_KEY );
    // Determine newstate depending on oldState ( none -> down -> up )
    if( oldstate == null )
      return Boolean.FALSE;
    else if( !oldstate.booleanValue() )
      return Boolean.TRUE;

    return null;
  }

  /**
   * Set the sorting state of a column.<br>
   * Before calling this method, a {@link ViewerSorter} has to be registered with
   * {@link #registerSorter(ViewerColumn, ViewerSorter)}.
   * 
   * @param sortState
   *          The sort state: <code>null</code>: do not sort; <code>true</code>: sort forwards; <code>false</code> sort
   *          backwards
   */
  public static void setSortState( final ViewerColumn columnToSort, final Boolean sortState )
  {
    final Item itemToSort = ColumnViewerUtil.itemForColumn( columnToSort );
    if( getSorter( itemToSort ) == null )
      return;

    clearSortKeys( itemToSort );

    final ViewerSorter sorter = createSorter( itemToSort, sortState );
    final Image img = getSortImage( sortState );

    itemToSort.setData( SORT_KEY, sortState );
    itemToSort.setImage( img );
    final ColumnViewer viewer = columnToSort.getViewer();
    viewer.setSorter( sorter );
  }

  private static void clearSortKeys( final Item itemToSort ) throws IllegalArgumentException
  {
    final Item[] columns = ColumnViewerUtil.getSisterItems( itemToSort );
    for( final Item column : columns )
    {
      column.setData( SORT_KEY, null );
      column.setImage( m_emptyImage );
    }
  }

  private static Image getSortImage( final Boolean sortState )
  {
    if( sortState == null )
      return m_emptyImage;

    if( sortState.booleanValue() )
      return m_upImage;

    return m_downImage;
  }

  private static ViewerSorter createSorter( final Item itemToSort, final Boolean sortState )
  {
    if( sortState == null )
      return null;

    final ViewerSorter sorter = getSorter( itemToSort );
    if( sorter == null )
      return null;

    if( sortState )
      return new InverseSorter( sorter );

    return sorter;
  }

  private static ViewerSorter getSorter( final Item itemToSort )
  {
    final Object data = itemToSort.getData( SORT_SORTER );
    if( data instanceof ViewerSorter )
      return (ViewerSorter) data;

    return null;
  }
}