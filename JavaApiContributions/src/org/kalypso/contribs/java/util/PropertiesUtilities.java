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
package org.kalypso.contribs.java.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * @author doemming
 */
public class PropertiesUtilities
{
  /**
   * @param query
   *          string to collect properties from
   * @param propSeparator
   *          example "&"
   * @param allocationgString
   *          example "="
   * @param collector
   */
  public static Properties collectProperties( final String query, final String propSeparator, final String allocationString, Properties collector )
  {
    if( collector == null )
      collector = new Properties();
    if( query == null )
      return collector;

    // now extract the key/value pairs from the query.
    final String[] params = query.split( propSeparator );
    for( final String element : params )
    {
      final String[] keyValuePair = element.split( allocationString, 2 );
      if( keyValuePair.length == 2 )
        collector.setProperty( keyValuePair[0], keyValuePair[1] );
    }
    return collector;
  }

  /**
   * Tries to load a (international) properties file from a given location.
   * 
   * @deprecated Use ResourceBundleUtils#loadResourceBundle() instead
   */
  // TODO: Gernot, use ResourceBundle#getBundle(String baseName, Locale locale, ClassLoader loader) with a specific class loader instead, in order
  // to completely simulate the java-ResourceBundle behaviour (i.e. fallback to more generel property)
  @Deprecated
  public static void loadI18nProperties( final Properties properties, final URL baseUrl, final String path )
  {
    final String[] suffixes = getLocalSuffixes();
    for( final String suffix : suffixes )
    {
      try
      {
        final URL url = new URL( baseUrl, path + suffix );
        load( url, properties );
        // On first success: stop loading
        return;
      }
      catch( final FileNotFoundException e )
      {
        // ignore file not found exceptions, as we have several tries
      }
      catch( final IOException e )
      {
        // TODO: this can happen for some strange url's (containing '(')
        // Introduce tracing option
        // e.printStackTrace();
      }
      finally
      {
      }
    }

    // If we reach this line, nothing was found...
    // TODO: produces too much output, make a tracing option
// final IStatus status = new Status( IStatus.ERROR,
// JavaApiContributionsPlugin.getDefault().getBundle().getSymbolicName(), "Message file not found: " + path +
// suffixes[0] );
// JavaApiContributionsPlugin.getDefault().getLog().log( status );
  }

  private static final String EXTENSION = ".properties"; //$NON-NLS-1$

  private static String[] NL_SUFFIXES;

  private static String[] getLocalSuffixes( )
  {
    if( NL_SUFFIXES == null )
    {
      // build list of suffixes for loading resource bundles
      String nl = Locale.getDefault().toString();
      final List<String> result = new ArrayList<String>( 4 );
      int lastSeparator;
      while( true )
      {
        result.add( '_' + nl + EXTENSION );
        lastSeparator = nl.lastIndexOf( '_' );
        if( lastSeparator == -1 )
          break;
        nl = nl.substring( 0, lastSeparator );
      }
      // add the empty suffix last (most general)
      result.add( EXTENSION );
      NL_SUFFIXES = result.toArray( new String[result.size()] );
    }
    return NL_SUFFIXES;
  }

  /**
   * Creates a new {@link Properties} object and initialises it from the contents of the given {@link URL}.
   */
  public static Properties load( final URL location ) throws IOException
  {
    final Properties properties = new Properties();
    load( location, properties );
    return properties;
  }

  /**
   * Loads the contents from the given {@link URL} into a {@link Properties} object.
   * 
   * @see Properties#load(InputStream)
   */
  public static void load( final URL location, final Properties properties ) throws IOException
  {
    InputStream is = null;
    try
    {
      is = new BufferedInputStream( location.openStream() );
      properties.load( is );
      is.close();
    }
    finally
    {
      if( is != null )
      {
        try
        {
          is.close();
        }
        catch( final IOException e )
        {
          // ignore, this time, there must be another exception just about to been thrown
        }
      }
    }
  }

  public static Properties load( final File file ) throws IOException
  {
    final Properties properties = new Properties();
    load( file, properties );
    return properties;
  }

  public static void load( final File file, final Properties properties ) throws IOException
  {
    InputStream is = null;
    try
    {

      is = new BufferedInputStream( new FileInputStream( file ) );
      properties.load( is );
      is.close();
    }
    finally
    {
      if( is != null )
      {
        try
        {
          is.close();
        }
        catch( final IOException e )
        {
          // ignore, this time, there must be another exception just about to been thrown
        }
      }
    }
  }
}
