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
package org.kalypso.zml.ui.table;

import java.util.Date;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.layout.ClosestDateVisitor;

/**
 * @author Dirk Kuch
 */
public class RevealTableWorker
{

  private final Date m_index;

  private final IZmlTable m_table;

  public RevealTableWorker( final IZmlTable table )
  {
    m_table = table;
    final TableViewer viewer = table.getTableViewer();
    m_index = getIndex( viewer );
  }

  private Date getIndex( final TableViewer viewer )
  {
    final ViewerCell cell = viewer.getCell( new Point( 10, 75 ) );
    if( Objects.isNull( cell ) )
      return null;

    final Object element = cell.getElement();
    if( !(element instanceof IZmlModelRow) )
      return null;

    final IZmlModelRow row = (IZmlModelRow) element;
    return (Date) row.getIndexValue();
  }

  public void reveal( )
  {
    if( Objects.isNull( m_index ) )
      return;

    final ClosestDateVisitor visitor = new ClosestDateVisitor( m_index );
    m_table.accept( visitor );

    final IZmlModelRow row = visitor.getModelRow();
    if( Objects.isNull( row ) )
      return;

    m_table.getTableViewer().reveal( row );

    // FIXME AbstractCellCursor has to listen to reveal events
    m_table.getFocusHandler().getCursor().redraw();
  }

}
