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
package org.kalypso.zml.core.table.model.references;

import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;

/**
 * @author Dirk Kuch
 */
public class ZmlIndexValueReference implements IZmlValueReference
{
  private final IZmlModelRow m_row;

  public ZmlIndexValueReference( final IZmlModelRow row )
  {
    m_row = row;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getColumn()
   */
  @Override
  public IZmlModelColumn getColumn( )
  {
    return null;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getRow()
   */
  @Override
  public IZmlModelRow getRow( )
  {
    return m_row;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getModel()
   */
  @Override
  public IZmlModel getModel( )
  {
    return m_row.getModel();
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlModelCell#getValue()
   */
  @Override
  public Object getValue( )
  {
    return getRow().getIndexValue();
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlModelCell#getDataSource()
   */
  @Override
  public String getDataSource( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlModelCell#getHref()
   */
  @Override
  public String getHref( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlModelCell#getStatus()
   */
  @Override
  public Integer getStatus( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlModelCell#getTupleModelIndex()
   */
  @Override
  public Integer getModelIndex( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlModelCell#update(java.lang.Object)
   */
  @Override
  public void update( final Object targetValue, final String source, final Integer status )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.zml.core.table.model.references.IZmlValueReference#getIndexValue()
   */
  @Override
  public Object getIndexValue( )
  {
    return getRow().getIndexValue();
  }
}
