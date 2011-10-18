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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.ZmlTableCell;
import org.kalypso.zml.ui.table.model.ZmlTableRow;

import com.google.common.base.Objects;
import com.google.common.collect.MapMaker;

/**
 * @author Dirk Kuch
 */
public class ZmlTableCellCache
{

  class CacheEntry
  {
    private final IZmlModelRow m_row;

    private final IZmlModelColumn m_column;

    private Object m_value;

    public CacheEntry( final IZmlModelRow row, final IZmlModelColumn column )
    {
      m_row = row;
      m_column = column;
    }

    @Override
    public boolean equals( final Object obj )
    {
      if( obj instanceof CacheEntry )
      {
        final CacheEntry other = (CacheEntry) obj;

        final EqualsBuilder builder = new EqualsBuilder();
        builder.append( m_row, other.m_row );
        builder.append( m_column, other.m_column );

        return builder.isEquals();
      }

      return super.equals( obj );
    }

    @Override
    public int hashCode( )
    {
      final HashCodeBuilder builder = new HashCodeBuilder();
      builder.append( m_row );
      builder.append( m_column );

      return builder.toHashCode();
    }

    public void setValue( final Object value )
    {
      m_value = value;
    }

    public boolean isValid( final IZmlTableCell cell )
    {
      try
      {
        if( cell.getColumn().isIndexColumn() )
        {
          return true;
        }

        final IZmlValueReference reference = cell.getValueReference();
        if( reference == null )
          return false;

        return Objects.equal( m_value, reference.getValue() );
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }

      return false;
    }
  }

  private final Map<CacheEntry, ZmlTableCellPainter> m_cache;

  public ZmlTableCellCache( )
  {
    final MapMaker marker = new MapMaker().expireAfterAccess( 10, TimeUnit.MINUTES );
    m_cache = marker.makeMap();
  }

  public ZmlTableCellPainter getPainter( final IZmlModelRow row, final IZmlTableColumn column )
  {
    if( !column.isVisible() )
      return null;

    synchronized( this )
    {
      final CacheEntry entry = new CacheEntry( row, column.getModelColumn() );
      ZmlTableCellPainter painter = m_cache.get( entry );
      final ZmlTableCell cell = new ZmlTableCell( new ZmlTableRow( column.getTable(), row ), column );

      if( painter == null )
      {
        painter = new ZmlTableCellPainter( cell );
        m_cache.put( entry, painter );
        updateEntry( entry, cell );

        return painter;
      }

      if( entry.isValid( cell ) )
        return painter;

      painter = new ZmlTableCellPainter( cell );
      m_cache.put( entry, painter );
      updateEntry( entry, cell );

      return painter;
    }
  }

  private void updateEntry( final CacheEntry entry, final ZmlTableCell cell )
  {
    try
    {
      if( cell.getColumn().isIndexColumn() )
        return;

      final IZmlValueReference reference = cell.getValueReference();
      if( reference == null )
        return;

      entry.setValue( reference.getValue() );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

  }

}
