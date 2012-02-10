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
package org.kalypso.zml.ui.table.model;

import java.util.List;

import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.model.rows.IZmlTableRow;
import org.kalypso.zml.ui.table.model.rows.ZmlTableRow;

/**
 * @author Dirk Kuch
 */
public class ZmlMainTableModel extends AbstractZmlTableModel
{

  public ZmlMainTableModel( final IZmlTable table, final IZmlModel model )
  {
    super( table, model );
  }

  @Override
  public List<IZmlTableRow> getRows( )
  {
    synchronized( this )
    {
      if( m_rows.isEmpty() )
      {

        final IZmlModelRow[] rows = getModel().getRows();
        for( final IZmlModelRow row : rows )
        {
          m_rows.add( new ZmlTableRow( getTable(), row ) );
        }
      }
    }

    return m_rows;
  }
}
