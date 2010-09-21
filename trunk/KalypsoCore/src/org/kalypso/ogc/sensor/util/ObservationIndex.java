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
package org.kalypso.ogc.sensor.util;

import java.util.HashMap;
import java.util.Map;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;

/**
 * This class represents an index of an observation over a given key axis.<br/>
 * Once the index is built up, it can be used to access values of this observation in a fast way.
 * 
 * @author Gernot Belger
 */
public class ObservationIndex
{
  private final IAxis m_keyAxis;

  private Map<Object, Integer> m_index = null;

  private final ITupleModel m_data;

  public ObservationIndex( final ITupleModel data, final IAxis keyAxis )
  {
    m_data = data;
    m_keyAxis = keyAxis;
  }

  private synchronized void checkIndex( ) throws SensorException
  {
    if( m_index == null )
      builtIndex();
  }

  private void builtIndex( ) throws SensorException
  {
    m_index = new HashMap<Object, Integer>();

    final int count = m_data.getCount();
    for( int i = 0; i < count; i++ )
    {
      final Object key = m_data.getElement( i, m_keyAxis );
      m_index.put( key, i );
    }
  }

  public Integer getRow( final Object key ) throws SensorException
  {
    checkIndex();

    return m_index.get( key );
  }

  public Object getValue( final Object key, final IAxis valueAxis ) throws SensorException
  {
    final Integer row = getRow( key );
    if( row == null )
      throw new SensorException( String.format( "Unknown key: %s", key ) );

    return m_data.getElement( row, valueAxis );
  }
}
