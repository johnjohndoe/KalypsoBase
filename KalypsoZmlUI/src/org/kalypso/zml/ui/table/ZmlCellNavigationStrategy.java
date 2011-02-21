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
package org.kalypso.zml.ui.table;

import org.eclipse.jface.viewers.CellNavigationStrategy;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.kalypso.commons.java.lang.Objects;

/**
 * @author Dirk Kuch
 */
public class ZmlCellNavigationStrategy extends CellNavigationStrategy
{
  /**
   * @see org.eclipse.jface.viewers.CellNavigationStrategy#findSelectedCell(org.eclipse.jface.viewers.ColumnViewer,
   *      org.eclipse.jface.viewers.ViewerCell, org.eclipse.swt.widgets.Event)
   */
  @Override
  public ViewerCell findSelectedCell( final ColumnViewer viewer, final ViewerCell currentSelectedCell, final Event event )
  {
    switch( event.keyCode )
    {
      case SWT.ARROW_UP:
        if( currentSelectedCell != null )
        {
          return findCell( currentSelectedCell, ViewerCell.ABOVE, false );
        }
        break;
      case SWT.ARROW_DOWN:
        if( currentSelectedCell != null )
        {
          return findCell( currentSelectedCell, ViewerCell.BELOW, false );
        }
        break;
      case SWT.ARROW_LEFT:
        if( currentSelectedCell != null )
        {
          return findCell( currentSelectedCell, ViewerCell.LEFT, true );
        }
        break;
      case SWT.ARROW_RIGHT:
        if( currentSelectedCell != null )
        {
          return findCell( currentSelectedCell, ViewerCell.RIGHT, true );
        }
        break;
    }

    return null;
  }

  private ViewerCell findCell( final ViewerCell current, final int direction, final boolean sameLevel )
  {
    final ViewerCell cell = current.getNeighbor( direction, sameLevel );
    if( Objects.isNull( cell ) )
      return null;

    if( cell.getBounds().width == 0 )
      return findCell( cell, direction, sameLevel );

    return cell;
  }
}
