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

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.contribs.java.lang.NumberUtils;

/**
 * @author Dirk Kuch
 */
public class MetadataWQTable
{
  private final MetadataList m_metadata;

  public MetadataWQTable( final MetadataList metadata )
  {
    m_metadata = metadata;
  }

  public Double getMinW( )
  {
    final String property = m_metadata.getProperty( IMetadataConstants.WQ_BOUNDARY_W_MIN );
    if( Strings.isNotEmpty( property ) )
      return NumberUtils.parseQuietDouble( property );

    return null;
  }

  public Double getMinQ( )
  {
    final String property = m_metadata.getProperty( IMetadataConstants.WQ_BOUNDARY_Q_MIN );
    if( Strings.isNotEmpty( property ) )
      return NumberUtils.parseQuietDouble( property );

    return null;
  }

  public Double getMaxW( )
  {
    final String property = m_metadata.getProperty( IMetadataConstants.WQ_BOUNDARY_W_MAX );
    if( Strings.isNotEmpty( property ) )
      return NumberUtils.parseQuietDouble( property );

    return null;
  }

  public Double getMaxQ( )
  {
    final String property = m_metadata.getProperty( IMetadataConstants.WQ_BOUNDARY_Q_MAX );
    if( Strings.isNotEmpty( property ) )
      return NumberUtils.parseQuietDouble( property );

    return null;
  }

  /**
   * update settings from base
   */
  public static boolean updateWqTable( final MetadataList base, final MetadataList overwrite )
  {
    final String table = overwrite.getProperty( ITimeseriesConstants.MD_WQ_TABLE );
    if( Strings.isEmpty( table ) )
      return false;

    MetadataHelper.setWqTable( base, table );

    final MetadataWQTable overwrited = new MetadataWQTable( overwrite );

    final Double w1 = overwrited.getMinW();
    updateWqBoundary( base, IMetadataConstants.WQ_BOUNDARY_W_MIN, w1 );

    final Double w2 = overwrited.getMaxW();
    updateWqBoundary( base, IMetadataConstants.WQ_BOUNDARY_W_MAX, w2 );

    final Double q1 = overwrited.getMinQ();
    updateWqBoundary( base, IMetadataConstants.WQ_BOUNDARY_Q_MIN, q1 );

    final Double q2 = overwrited.getMaxQ();
    updateWqBoundary( base, IMetadataConstants.WQ_BOUNDARY_Q_MAX, q2 );

    return true;
  }

  private static void updateWqBoundary( final MetadataList metadata, final String property, final Double value )
  {
    if( Objects.isNull( value ) )
      metadata.remove( property );
    else
      metadata.setProperty( property, value.toString() );
  }

  protected MetadataList getMetadata( )
  {
    return m_metadata;
  }

}
