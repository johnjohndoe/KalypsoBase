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
package org.kalypso.grid;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.contribs.java.util.Arrays;

/**
 * @author Gernot Belger
 */
public final class WorldFileFormat
{
  private static final WorldFileFormat[] AVAILABLE_FORMATS = new WorldFileFormat[3];

  static
  {
    AVAILABLE_FORMATS[0] = new WorldFileFormat( "tif", "tfw", "TIF File" );
    AVAILABLE_FORMATS[1] = new WorldFileFormat( "jpg", "jgw", "JPEG File" );
    AVAILABLE_FORMATS[2] = new WorldFileFormat( "png", "pgw", "Portable Network Graphic" );
  }

  public static WorldFileFormat[] getAvailableFormats( )
  {
    return AVAILABLE_FORMATS;
  }

  private final String m_imgFileExtension;

  private final String m_worldFileExtension;

  private final String m_name;

  private WorldFileFormat( final String imgFileExtension, final String worldFileExtension, final String name )
  {
    m_imgFileExtension = imgFileExtension;
    m_worldFileExtension = worldFileExtension;
    m_name = name;
  }

  public String getFilterName( )
  {
    return String.format( "%s (*.%s)", m_name, m_imgFileExtension );
  }

  public String getFilterExtension( )
  {
    return String.format( "*.%s", m_imgFileExtension );
  }

  public String getImgFileExtension( )
  {
    return m_imgFileExtension;
  }

  public String getWorldFileExtension( )
  {
    return m_worldFileExtension;
  }

  public static String getAllSuportedFilterName( )
  {
    String[] names = ArrayUtils.EMPTY_STRING_ARRAY;
    final WorldFileFormat[] availableFormats = getAvailableFormats();
    for( final WorldFileFormat worldFileFormat : availableFormats )
      names = (String[]) ArrayUtils.add( names, worldFileFormat.getFilterExtension() );

    final String allNames = Arrays.implode( names, ", " );
    return String.format( "All Supported Image Formats (%s)", allNames );
  }

  public static Object getAllSupportedFilters( )
  {
    String[] names = ArrayUtils.EMPTY_STRING_ARRAY;
    final WorldFileFormat[] availableFormats = getAvailableFormats();
    for( final WorldFileFormat worldFileFormat : availableFormats )
      names = (String[]) ArrayUtils.add( names, worldFileFormat.getFilterExtension() );
    return Arrays.implode( names, ";" );
  }
}
