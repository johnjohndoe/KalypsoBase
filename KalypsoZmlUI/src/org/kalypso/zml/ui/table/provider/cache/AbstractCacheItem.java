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

import java.util.Date;

import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.references.ZmlDataValueReference;
import org.kalypso.zml.core.table.model.references.ZmlIndexValueReference;
import org.kalypso.zml.ui.table.model.ZmlTableCell;
import org.kalypso.zml.ui.table.provider.ZmlTableCellPainter;

import com.google.common.base.Objects;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractCacheItem
{
  static class IndexValueItem extends AbstractCacheItem
  {

    private final Date m_date;

    IndexValueItem( final ZmlTableCellPainter painter, final ZmlIndexValueReference reference )
    {
      super( painter );
      m_date = reference.getIndexValue();
    }

    @Override
    public boolean isValid( final Object other )
    {
      if( other instanceof ZmlIndexValueReference )
        return Objects.equal( m_date, ((ZmlIndexValueReference) other).getIndexValue() );

      return false;
    }
  }

  static class DataValueItem extends AbstractCacheItem
  {
    private final Number m_value;

    private final Integer m_status;

    private final String m_dataSource;

    public DataValueItem( final ZmlTableCellPainter painter, final ZmlDataValueReference reference ) throws SensorException
    {
      super( painter );

      m_value = reference.getValue();
      m_status = reference.getStatus();
      m_dataSource = reference.getDataSource();
    }

    @Override
    public boolean isValid( final Object other ) throws SensorException
    {
      if( !(other instanceof ZmlDataValueReference) )
        return false;

      final ZmlDataValueReference ref = (ZmlDataValueReference) other;

      final boolean value = isEqual( m_value, ref.getValue() );
      final boolean source = isEqual( m_status, ref.getDataSource() );
      final boolean status = isEqual( m_dataSource, ref.getStatus() );

      return value && source && status;
    }

    private boolean isEqual( final Object v1, final Object v2 )
    {
      if( org.kalypso.commons.java.lang.Objects.allNull( v1, v2 ) )
        return true;

      return Objects.equal( v1, v2 );
    }
  }

  private final ZmlTableCellPainter m_painter;

  public AbstractCacheItem( final ZmlTableCellPainter painter )
  {
    m_painter = painter;
  }

  public ZmlTableCellPainter getPainter( )
  {
    return m_painter;
  }

  public abstract boolean isValid( final Object other ) throws SensorException;

  public static AbstractCacheItem toItem( final ZmlTableCell cell ) throws SensorException
  {
    final IZmlValueReference reference = cell.getValueReference();
    if( reference == null )
      return null;

    if( reference instanceof ZmlIndexValueReference )
      return new IndexValueItem( new ZmlTableCellPainter( cell ), (ZmlIndexValueReference) reference );

    return new DataValueItem( new ZmlTableCellPainter( cell ), (ZmlDataValueReference) reference );
  }
}
