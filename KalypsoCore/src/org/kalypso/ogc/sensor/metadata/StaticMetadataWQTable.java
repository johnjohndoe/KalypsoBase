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
package org.kalypso.ogc.sensor.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Dirk Kuch
 */
public class StaticMetadataWQTable
{

  private final String m_wqtable;

  private final Double m_minQ;

  private final Double m_maxQ;

  private final Double m_minW;

  private final Double m_maxW;

  public StaticMetadataWQTable( final String wqtable, final Double minQ, final Double maxQ, final Double minW, final Double maxW )
  {
    m_wqtable = wqtable;
    m_minQ = minQ;
    m_maxQ = maxQ;
    m_minW = minW;
    m_maxW = maxW;
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( m_wqtable );
    builder.append( m_minQ );
    builder.append( m_maxQ );
    builder.append( m_minW );
    builder.append( m_maxW );

    return builder.toHashCode();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof StaticMetadataWQTable )
    {
      final StaticMetadataWQTable other = (StaticMetadataWQTable) obj;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( m_wqtable, other.m_wqtable );
      builder.append( m_minQ, other.m_minQ );
      builder.append( m_maxQ, other.m_maxQ );
      builder.append( m_minW, other.m_minW );
      builder.append( m_maxW, other.m_maxW );

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  public static StaticMetadataWQTable toTable( final MetadataList metadata )
  {

    final MetadataWQTable table = new MetadataWQTable( metadata );

    final String property = metadata.getProperty( ITimeseriesConstants.MD_WQ_TABLE );
    final Double minQ = table.getMinQ();
    final Double maxQ = table.getMaxQ();

    final Double minW = table.getMinW();
    final Double maxW = table.getMaxW();

    return new StaticMetadataWQTable( property, minQ, maxQ, minW, maxW );

  }

}
