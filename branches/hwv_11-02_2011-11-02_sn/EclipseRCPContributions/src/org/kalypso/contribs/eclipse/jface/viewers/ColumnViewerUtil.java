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
package org.kalypso.contribs.eclipse.jface.viewers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Utilities for {@link org.eclipse.jface.viewers.ColumnViewer}'s.
 * 
 * @author Gernot Belger
 */
public final class ColumnViewerUtil
{
  private ColumnViewerUtil( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  public static Item[] getSisterItems( final Item item )
  {
    if( item instanceof TableColumn )
      return ((TableColumn) item).getParent().getColumns();

    if( item instanceof TreeColumn )
      return ((TreeColumn) item).getParent().getColumns();

    throw new IllegalArgumentException( String.format( "Unknown item type for sorting %s", item.getClass().getName() ) );
  }

  public static Item itemForColumn( final ViewerColumn column ) throws IllegalArgumentException
  {
    if( column instanceof TableViewerColumn )
      return ((TableViewerColumn) column).getColumn();

    if( column instanceof TreeViewerColumn )
      return ((TreeViewerColumn) column).getColumn();

    throw new IllegalArgumentException( String.format( "Unknown column type %s", column.getClass() ) );
  }

  public static void packColumns( final TableViewer viewer )
  {
    final TableColumn[] columns = viewer.getTable().getColumns();
    for( final TableColumn column : columns )
      column.pack();
  }

  public static void packColumns( final TreeViewer viewer )
  {
    final TreeColumn[] columns = viewer.getTree().getColumns();
    for( final TreeColumn column : columns )
      column.pack();
  }

  public static ViewerColumn createViewerColumn( final ColumnViewer viewer, final int center )
  {
    if( viewer instanceof TreeViewer )
      return new TreeViewerColumn( (TreeViewer) viewer, center );
    if( viewer instanceof TableViewer )
      return new TableViewerColumn( (TableViewer) viewer, center );

    throw new IllegalArgumentException();
  }
}
