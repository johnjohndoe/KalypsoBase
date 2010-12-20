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

import java.util.HashMap;
import java.util.Map;

import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.references.ZmlDataValueReference;
import org.kalypso.zml.core.table.model.references.ZmlIndexValueReference;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.IndexColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlModelRow implements IZmlModelRow
{
  private final Object m_index;

  /** Map<Reference (id), Reference> */
  Map<String, IZmlValueReference> m_references = new HashMap<String, IZmlValueReference>();

  private final IZmlModel m_model;

  protected ZmlModelRow( final IZmlModel model, final Object index )
  {
    m_model = model;
    m_index = index;
  }

  public void add( final ZmlDataValueReference reference )
  {
    m_references.put( reference.getIdentifier(), reference );
  }

  @Override
  public IZmlValueReference get( final AbstractColumnType type )
  {
    if( type instanceof IndexColumnType )
    {
      return new ZmlIndexValueReference( this );
    }

    return m_references.get( type.getId() );
  }

  /**
   * @see org.kalypso.zml.ui.table.model.IZmlModelRow#get(org.kalypso.zml.ui.table.model.IZmlModelColumn)
   */
  @Override
  public IZmlValueReference get( final IZmlModelColumn column )
  {
    if( column == null )
      return null;

    return get( column.getDataColumn().getType() );
  }

  @Override
  public Object getIndexValue( )
  {
    return m_index;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.IZmlModelRow#getModel()
   */
  @Override
  public IZmlModel getModel( )
  {
    return m_model;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.IZmlModelRow#getReferences()
   */
  @Override
  public IZmlValueReference[] getReferences( )
  {
    return m_references.values().toArray( new IZmlValueReference[] {} );
  }

}
