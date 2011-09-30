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

import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;

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
   * 
   * @deprecated Use {@link #registerSorter(ViewerColumn, ViewerComparator)} instead. Usage of Viewersorter is
   *             discouraged by eclipse.
   */
  @Deprecated
  public static void registerSorter( final ViewerColumn column, final ViewerSorter sorter )
  {
    final ColumnSortListener sortListener = new ColumnSortListener( column );
    sortListener.setSorter( sorter );
  }

  /**
   * Registers a {@link ViewerComparator} with a column. If the sorter is registered, clicking on the column header will
   * toggle the sorting state for this column.
   */
  public static void registerSorter( final ViewerColumn column, final ViewerComparator comparator )
  {
    final ColumnSortListener sortListener = new ColumnSortListener( column );
    sortListener.setSorter( comparator );
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
    ColumnSortListener.setSortState( columnToSort, sortState );
  }

  private ColumnViewerSorter( )
  {
    throw new UnsupportedOperationException();
  }
}