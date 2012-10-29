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
package org.kalypso.commons.java.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.lang.Strings;

/**
 * This class provides functions for dealing with {@link java.util.Properties}.
 * 
 * @author Holger Albert
 */
public class PropertiesUtilities
{
  /**
   * The constructor.
   */
  private PropertiesUtilities( )
  {
  }

  /**
   * This function loads the properties.
   * 
   * @param propertyFile
   *          The property file.
   * @return The properties.
   */
  public static Properties load( final File propertyFile ) throws IOException
  {
    /* The input stream. */
    InputStream inputStream = null;

    try
    {
      /* Create the properties. */
      final Properties properties = new Properties();

      /* If the file does not exist, return empty properties. */
      if( !propertyFile.exists() )
        return properties;

      /* Create the input stream. */
      inputStream = new BufferedInputStream( new FileInputStream( propertyFile ) );

      /* Load the properties. */
      properties.load( inputStream );

      return properties;
    }
    finally
    {
      /* Close the input stream. */
      IOUtils.closeQuietly( inputStream );
    }
  }

  /**
   * This function saves the properties.
   * 
   * @param propertyFile
   *          The property file.
   * @param properties
   *          The properties.
   */
  public static void save( final File propertyFile, final Properties properties ) throws IOException
  {
    /* The output stream. */
    OutputStream outputStream = null;

    try
    {
      /* Create the output stream. */
      outputStream = new BufferedOutputStream( new FileOutputStream( propertyFile ) );

      /* Load the properties. */
      properties.store( outputStream, null );
    }
    finally
    {
      /* Close the output stream. */
      IOUtils.closeQuietly( outputStream );
    }
  }

  public static void merge( final Properties base, final Properties extension )
  {
    final Set<Entry<Object, Object>> entries = extension.entrySet();
    for( final Entry<Object, Object> entry : entries )
    {
      final String property = base.getProperty( entry.getKey().toString() );
      if( Objects.isNull( property ) || Strings.isEmpty( property ) )
        base.setProperty( entry.getKey().toString(), entry.getValue().toString() );
    }
  }
}