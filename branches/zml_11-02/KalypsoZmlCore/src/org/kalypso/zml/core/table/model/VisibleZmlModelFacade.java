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
package org.kalypso.zml.core.table.model;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;

/**
 * @author Dirk Kuch
 */
public class VisibleZmlModelFacade
{
  // FIXME set as zml table base
  // FIXME zml model change listener
  // FIXME caching
  // FIXME zml time filter

  private final IZmlModel m_model;

  public VisibleZmlModelFacade( final IZmlModel model )
  {
    m_model = model;
  }

  public IZmlModelCell getCell( final IZmlModelRow row, final int columnIndex )
  {
    final ZmlModelColumn[] columns = m_model.getActiveColumns();
    if( ArrayUtils.getLength( columns ) < columnIndex )
      return null;

    final ZmlModelColumn column = columns[columnIndex];
    return row.get( column );
  }

  public ZmlModelColumn[] getColumns( )
  {
    return m_model.getActiveColumns();
  }

  public IZmlModelColumn getColum( final int index )
  {
    final ZmlModelColumn[] columns = getColumns();
    if( ArrayUtils.getLength( columns ) < index )
      return null;

    return columns[index];
  }

  public IZmlModelRow[] getRows( )
  {
    // TODO return only visible rows

    return m_model.getRows();
  }

  public IZmlModelCell getCell( final int rowPosition, final int columnPosition )
  {

    final IZmlModelRow row = getRow( rowPosition );
    final IZmlModelColumn column = getColum( columnPosition );

    if( Objects.isNotNull( row, column ) )
      return row.get( column );

    return null;
  }

  public IZmlModelRow getRow( final int rowIndex )
  {
    final IZmlModelRow[] rows = getRows();

    if( ArrayUtils.getLength( rows ) < rowIndex )
      return null;

    return rows[rowIndex];
  }

  public IZmlModelCell findPreviousCell( final IZmlModelCell current )
  {
    throw new UnsupportedOperationException();
  }

  public IZmlModelCell findNextCell( final IZmlModelCell current )
  {
    throw new UnsupportedOperationException();
  }

  public int getResolution( )
  {
    throw new UnsupportedOperationException();
  }
}
