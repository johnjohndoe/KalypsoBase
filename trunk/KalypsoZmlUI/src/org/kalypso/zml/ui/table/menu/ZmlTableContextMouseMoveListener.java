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
package org.kalypso.zml.ui.table.menu;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.kalypso.zml.ui.table.IZmlTableComposite;
import org.kalypso.zml.ui.table.binding.BaseColumn;
import org.kalypso.zml.ui.table.model.ZmlTableRow;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;

/**
 * @author Dirk Kuch
 */
public class ZmlTableContextMouseMoveListener implements MouseMoveListener
{
  Point m_position;

  private final IZmlTableComposite m_table;

  public ZmlTableContextMouseMoveListener( final IZmlTableComposite table )
  {
    m_table = table;
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
    m_position = new Point( e.x, e.y );
  }

  public IZmlValueReference findCell( final TableViewer viewer, final ZmlTableRow row )
  {
    final ViewerCell cell = viewer.getCell( m_position );
    if( cell == null )
      return null;

    final BaseColumn column = m_table.getColumn( cell.getColumnIndex() );
    final IZmlValueReference reference = row.get( column.getType() );

    return reference;
  }
}
