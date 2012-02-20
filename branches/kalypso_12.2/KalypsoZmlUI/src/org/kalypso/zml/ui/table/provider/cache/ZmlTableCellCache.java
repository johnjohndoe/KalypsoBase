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
package org.kalypso.zml.ui.table.provider.cache;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.references.ZmlIndexValueReference;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.ZmlTableCell;
import org.kalypso.zml.ui.table.model.ZmlTableRow;
import org.kalypso.zml.ui.table.provider.ZmlTableCellPainter;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author Dirk Kuch
 */
public class ZmlTableCellCache
{

  static class IndexItem
  {
    private final IZmlModelRow m_row;

    private final IZmlModelColumn m_column;

    public IndexItem( final IZmlModelRow row, final IZmlModelColumn column )
    {
      m_row = row;
      m_column = column;
    }

    @Override
    public boolean equals( final Object obj )
    {
      if( obj instanceof IndexItem )
      {
        final IndexItem other = (IndexItem) obj;

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

    public boolean equals( final IZmlModelRow row, final IZmlModelColumn modelColumn )
    {
      return Objects.equal( m_row, row ) && Objects.equal( m_column, modelColumn );
    }
  }

  private final Cache<IndexItem, AbstractCacheItem> m_cache;

  public ZmlTableCellCache( )
  {
    final CacheBuilder builder = CacheBuilder.newBuilder().expireAfterAccess( 10, TimeUnit.MINUTES );
    m_cache = builder.build();
  }

  public ZmlTableCellPainter getPainter( final IZmlModelRow row, final IZmlTableColumn column )
  {
    if( !column.isVisible() )
      return null;

    synchronized( this )
    {
      final ZmlTableCell cell = new ZmlTableCell( new ZmlTableRow( column.getTable(), row ), column );
      try
      {
        final IndexItem entry = new IndexItem( row, column.getModelColumn() );
        AbstractCacheItem item = m_cache.asMap().get( entry );
        if( item == null )
        {
          item = AbstractCacheItem.toItem( cell );
          if( item == null )
            return null;

          m_cache.put( entry, item );

          return item.getPainter();
        }

        if( item.isValid( toValue( cell ) ) )
          return item.getPainter();

        item = AbstractCacheItem.toItem( cell );
        if( item == null )
          return null;

        m_cache.put( entry, item );

        return item.getPainter();
      }
      catch( final SensorException e )
      {
        e.printStackTrace();

        return new ZmlTableCellPainter( cell );
      }
    }
  }

  private Object toValue( final ZmlTableCell cell ) throws SensorException
  {
    final IZmlValueReference reference = cell.getValueReference();
    if( reference == null )
      return null;

    if( reference instanceof ZmlIndexValueReference )
      return reference.getIndexValue();

    return reference.getValue();
  }

  public synchronized void clear( )
  {
    m_cache.cleanUp();
  }
}
