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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.TableTypes;
import org.kalypso.zml.core.table.model.references.IZmlModelIndexCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.ZmlModelIndexCell;
import org.kalypso.zml.core.table.model.references.ZmlModelValueCell;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.IndexColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlModelRow implements IZmlModelRow
{
  private final Date m_index;

  /** Map<Reference (id), Reference> */
  Map<String, IZmlModelValueCell> m_valueCells = new HashMap<String, IZmlModelValueCell>();

  private final IZmlModel m_model;

  private ZmlModelIndexCell m_indexCell;

  public ZmlModelRow( final IZmlModel model, final Date index )
  {
    m_model = model;
    m_index = index;
  }

  public void add( final ZmlModelValueCell reference )
  {
    m_valueCells.put( reference.getIdentifier(), reference );
  }

  @Override
  public IZmlModelValueCell get( final AbstractColumnType type )
  {
    return m_valueCells.get( type.getId() );
  }

  @Override
  public IZmlModelValueCell get( final IZmlModelColumn column )
  {
    if( column == null )
      return null;

    return m_valueCells.get( column.getIdentifier() );
  }

  @Override
  public IZmlModelIndexCell getIndexCell( )
  {
    if( Objects.isNull( m_indexCell ) )
    {
      final IndexColumnType base = TableTypes.findIndexColumn( getModel().getTableType() );
      m_indexCell = new ZmlModelIndexCell( this, new BaseColumn( base ) );
    }

    return m_indexCell;
  }

  @Override
  public Date getIndex( )
  {
    return m_index;
  }

  @Override
  public IZmlModel getModel( )
  {
    return m_model;
  }

  @Override
  public IZmlModelValueCell[] getCells( )
  {
    return m_valueCells.values().toArray( new IZmlModelValueCell[] {} );
  }

  @Override
  public String toString( )
  {
    return String.format( "%s, Index: %s", getClass().getName(), m_index.toString() );
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof ZmlModelRow )
    {
      final ZmlModelRow other = (ZmlModelRow) obj;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( getIndex(), other.getIndex() );

      return builder.isEquals();
    }
    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getClass().getName() );
    builder.append( getIndex() );

    return builder.toHashCode();
  }

}
