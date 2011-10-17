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
package org.kalypso.zml.ui.table.provider;

import java.util.Date;

import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.ZmlModel;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.schema.AbstractColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlHeaderRow implements IZmlModelRow
{
  private final ZmlModel m_model;

  public ZmlHeaderRow( final ZmlModel model )
  {
    m_model = model;
  }

  /**
   * @see org.kalypso.zml.core.table.model.IZmlModelRow#get(org.kalypso.zml.core.table.schema.AbstractColumnType)
   */
  @Override
  public IZmlValueReference get( final AbstractColumnType type )
  {
    return null;
  }

  /**
   * @see org.kalypso.zml.core.table.model.IZmlModelRow#get(org.kalypso.zml.core.table.model.IZmlModelColumn)
   */
  @Override
  public IZmlValueReference get( final IZmlModelColumn column )
  {
    return new ZmlHeaderReference( column );
  }

  /**
   * @see org.kalypso.zml.core.table.model.IZmlModelRow#getIndexValue()
   */
  @Override
  public Date getIndexValue( )
  {
    return null;
  }

  /**
   * @see org.kalypso.zml.core.table.model.IZmlModelRow#getModel()
   */
  @Override
  public IZmlModel getModel( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.zml.core.table.model.IZmlModelRow#getReferences()
   */
  @Override
  public IZmlValueReference[] getReferences( )
  {
    throw new UnsupportedOperationException();
  }

}
