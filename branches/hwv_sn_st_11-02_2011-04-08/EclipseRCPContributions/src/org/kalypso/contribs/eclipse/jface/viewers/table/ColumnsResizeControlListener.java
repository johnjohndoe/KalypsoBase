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
package org.kalypso.contribs.eclipse.jface.viewers.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This listener resizes the columns of a table to match its borders, if the table is resized.
 * 
 * @author Holger Albert
 */
public class ColumnsResizeControlListener extends ControlAdapter
{
  /**
   * The table.
   */
  private Table m_table;

  /**
   * The constructor.
   * 
   * @param table
   *          The table.
   */
  public ColumnsResizeControlListener( Table table )
  {
    m_table = table;
  }

  /**
   * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
   */
  @Override
  public void controlResized( ControlEvent e )
  {
    /* Get the parent. */
    Composite parent = m_table.getParent();

    /* Get the area of the parent. */
    Rectangle area = parent.getClientArea();

    /* Calculate the needed size of the table. */
    Point size = m_table.computeSize( SWT.DEFAULT, SWT.DEFAULT );

    /* Get the vertical bar. */
    ScrollBar vBar = m_table.getVerticalBar();

    /* Calculate the available width for the table. */
    int width = area.width - m_table.computeTrim( 0, 0, 0, 0 ).width - vBar.getSize().x;

    /* Subtract the scrollbar width from the total column width if a vertical scrollbar will be required. */
    if( size.y > area.height + m_table.getHeaderHeight() )
    {
      Point vBarSize = vBar.getSize();
      width -= vBarSize.x;
    }

    /* Get the colums. */
    TableColumn[] columns = m_table.getColumns();

    /* Get the old size. */
    Point oldSize = m_table.getSize();

    /* Table is getting smaller so make the columns smaller first. */
    /* Then resize the table to match the client area width. */
    if( oldSize.x > area.width )
    {
      /* Set the size for each colum. */
      for( TableColumn column : columns )
        column.setWidth( width / columns.length );

      /* Set the size for the table. */
      m_table.setSize( area.width, area.height );

      return;
    }

    /* Table is getting bigger so make the table bigger first. */
    /* Then make the columns wider to match the client area width. */

    /* Set the size for the table. */
    m_table.setSize( area.width, area.height );

    /* Set the size for each colum. */
    for( TableColumn column : columns )
      column.setWidth( width / columns.length );
  }
}