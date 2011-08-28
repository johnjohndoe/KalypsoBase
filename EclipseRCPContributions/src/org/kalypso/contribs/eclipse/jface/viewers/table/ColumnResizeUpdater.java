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
package org.kalypso.contribs.eclipse.jface.viewers.table;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * @author Gernot Belger
 */
class ColumnResizeUpdater
{
  private final int m_remainingWidth;

  private final ColumnWidthInfo[] m_infos;

  public ColumnResizeUpdater( final int width, final ColumnWidthInfo[] infos )
  {
    m_remainingWidth = width;
    m_infos = infos;
  }

  public void updateColumnsWidth( )
  {
    updateColumns();
    calculateWidths();
    setWidths();
  }

  private void updateColumns( )
  {
    if( m_infos.length == 0 )
      return;

    /* Do not redraw, packing the columns else resizes the table */
    final Composite control = findControl( m_infos[0] );
    control.setRedraw( false );

    for( final ColumnWidthInfo info : m_infos )
      info.calculateMinimumWidth();

        control.setRedraw( true );
  }

  private void calculateWidths( )
  {
    int remainingWidth = m_remainingWidth;
    int autoResizeColumnCount = 0;

    /* Set minimum widths; remaining widhts is reduced by minimum widths */
    for( final ColumnWidthInfo info : m_infos )
    {
      final int width = info.getCalculatedMinimumWidth();
      remainingWidth -= width;
      info.setColumnWidth( width );

      if( info.isAutoResize() )
        autoResizeColumnCount++;
    }

    if( remainingWidth <= 0 || autoResizeColumnCount == 0 )
      return;

    final int additionalWidth = remainingWidth / autoResizeColumnCount;

    /* Distribute remaining width */
    for( final ColumnWidthInfo info : m_infos )
    {
      if( info.isAutoResize() )
      {
        final int width = info.getColumnWidth();
        info.setColumnWidth( width  + additionalWidth);
      }
    }
  }

  private Composite findControl( final ColumnWidthInfo info )
  {
    final Item item = info.getColumn();
    if( item instanceof TableColumn )
      return ((TableColumn) item).getParent();

    if( item instanceof TreeColumn )
      return ((TreeColumn) item).getParent();

    throw new IllegalArgumentException();
  }

  private void setWidths( )
  {
    for( final ColumnWidthInfo info : m_infos )
      info.updateItemWidth();
  }
}