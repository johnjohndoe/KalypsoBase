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
package org.kalypso.zml.ui.table.provider;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;

/**
 * @author Dirk Kuch
 */
public class ZmlValueReference
{
  private final ZmlTableColumn m_column;

  private final int m_index;

  public ZmlValueReference( final ZmlTableColumn column, final int index )
  {
    m_column = column;
    m_index = index;
  }

  public Object getValue( ) throws SensorException
  {
    return m_column.get( m_index, m_column.getValueAxis() );
  }

  public void update( final Object value ) throws SensorException
  {
    m_column.update( m_index, value );
  }

  public IAxis getAxis( )
  {
    return m_column.getValueAxis();
  }

  public String getIdentifier( )
  {
    return m_column.getIdentifier();
  }

  public Integer getStatus( ) throws SensorException
  {
    final IAxis status = m_column.getStatusAxis();
    final Object value = m_column.get( m_index, status );

    if( value instanceof Number )
      return ((Number) value).intValue();

    return null;
  }

}
