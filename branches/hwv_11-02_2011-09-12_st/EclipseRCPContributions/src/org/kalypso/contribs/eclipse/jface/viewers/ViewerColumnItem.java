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
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Wrapper class that wraps both {@link org.eclipse.jface.viewers.TreeViewerColumn} and
 * {@link org.eclipse.jface.viewers.TableViewerColumn}'s.
 * 
 * @author Gernot Belger
 */
public class ViewerColumnItem
{
  private final ViewerColumn m_viewerColumn;

  public ViewerColumnItem( final ViewerColumn viewerColumn )
  {
    m_viewerColumn = viewerColumn;
  }

  public ColumnViewer getViewer( )
  {
    return m_viewerColumn.getViewer();
  }

  public Item getColumn( )
  {
    if( m_viewerColumn instanceof TableViewerColumn )
      return ((TableViewerColumn) m_viewerColumn).getColumn();

    if( m_viewerColumn instanceof TreeViewerColumn )
      return ((TreeViewerColumn) m_viewerColumn).getColumn();

    throw new IllegalArgumentException();
  }

  public void setText( final String text )
  {
    final Item col = getColumn();

    if( col instanceof TableColumn )
      ((TableColumn) col).setText( text );
    else if( col instanceof TreeColumn )
      ((TreeColumn) col).setText( text );
  }

  public void setToolTipText( final String text )
  {
    final Item col = getColumn();

    if( col instanceof TableColumn )
      ((TableColumn) col).setToolTipText( text );
    else if( col instanceof TreeColumn )
      ((TreeColumn) col).setToolTipText( text );
  }

  public void setWidth( final int width )
  {
    final Item col = getColumn();

    if( col instanceof TableColumn )
      ((TableColumn) col).setWidth( width );
    else if( col instanceof TreeColumn )
      ((TreeColumn) col).setWidth( width );
  }

  public void setResizable( final boolean resizeable )
  {
    final Item col = getColumn();

    if( col instanceof TableColumn )
      ((TableColumn) col).setResizable( resizeable );
    else if( col instanceof TreeColumn )
      ((TreeColumn) col).setResizable( resizeable );
  }

  public void setMoveable( final boolean moveable )
  {
    final Item col = getColumn();

    if( col instanceof TableColumn )
      ((TableColumn) col).setMoveable( moveable );
    else if( col instanceof TreeColumn )
      ((TreeColumn) col).setMoveable( moveable );
  }

  public void addSelectionListener( final SelectionListener listener )
  {
    final Item column = getColumn();
    if( column instanceof TableColumn )
      ((TableColumn) column).addSelectionListener( listener );
    else if( column instanceof TreeColumn )
      ((TreeColumn) column).addSelectionListener( listener );
  }

  public void setData( final String key, final Object value )
  {
    getColumn().setData( key, value );
  }

  public Object getData( final String key )
  {
    return getColumn().getData( key );
  }

  public void setAsSortColumn( final Boolean sortState )
  {
    int sortDirection;
    if( sortState == null )
      sortDirection = SWT.NONE;
    else if( sortState )
      sortDirection = SWT.DOWN;
    else
      sortDirection = SWT.UP;

    final Item column = getColumn();
    if( column instanceof TableColumn )
    {
      final TableColumn sortColumn = (TableColumn) column;
      final Table table = sortColumn.getParent();
      table.setSortDirection( sortDirection );
      table.setSortColumn( sortColumn );
    }
    else if( column instanceof TreeColumn )
    {
      final TreeColumn sortColumn = (TreeColumn) column;
      final Tree tree = sortColumn.getParent();
      tree.setSortDirection( sortDirection );
      tree.setSortColumn( sortColumn );
    }
  }
}