/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.metadata;

import java.util.Properties;

import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * Metadata for Observations.
 * 
 * @author schlienger
 */
public class MetadataList extends Properties
{
  private final DataSourceHandler m_sourceHandler;

  public MetadataList( )
  {
    super();

    m_sourceHandler = new DataSourceHandler( this );
  }

  /** REMARK: Sets the given properties as default values. It doesn't mean 'putAll'! */
  public MetadataList( final Properties arg0 )
  {
    super( arg0 );

    m_sourceHandler = new DataSourceHandler( this );
  }

  /**
   * Returns the source handler associated with this metadata list. All source modifications on this metadata should be
   * made via this source handler.
   */
  public DataSourceHandler getSourceHandler( )
  {
    return m_sourceHandler;
  }

  /**
   * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
   */
  @Override
  public synchronized Object setProperty( final String key, final String value )
  {
    return super.setProperty( key, value );
  }

  /**
   * @see java.util.Hashtable#put(java.lang.Object, java.lang.Object)
   */
  @Override
  public synchronized Object put( final Object key, final Object value )
  {
    return super.put( key, value );
  }
}
