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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.kalypso.contribs.java.io.FileUtilities;

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
      final String[] keyValuePair = element.split( allocationString );
      if( keyValuePair.length != 2 )
        continue;
      collector.setProperty( keyValuePair[0], keyValuePair[1] );
    }
    return collector;
  }

  /**
   * Tires to load a (international) properties file from a given location.
   */
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

  /**
   * Tries to load a (international) properties file for a given existing 'file'.<br>
   * The existing file is given by its url location. The property file is searches as follows: the file extension is
   * replaces by the current language pattern plus '.properties'.<br>
   * Example: <code>http://somehist/workflow.xml</code> is replaced by
   * <code>http://somehost/workflow_de_DE.properties</code>.<br>
   * According to java conventions, different language patterns are tried before giving up: 'de_DE', 'de', 'DE', ''.<br>
   * Attention: as the file extension is determined by {@link URL#toExternalForm()}, url with query parts are not
   * supported.
   */
  public static void loadI18nProperties( final Properties properties, final URL baseUrl )
  {
    final String baseUrlAsString = baseUrl.toExternalForm();
    final String baseUrlWithoutExtension = FileUtilities.nameWithoutExtension( baseUrlAsString );

    final String[] suffixes = getLocalSuffixes();
    for( final String suffix : suffixes )
    {
      try
      {
        final URL url = new URL( baseUrlWithoutExtension + suffix );
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
        e.printStackTrace();
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

  /**
   * Returns an translated string for a given key, similar to i18n of the plugin.xml file.<br>
   * The string is translated according to the given properties, if it start with '%'. If not, the original value is
   * returned.<br>
   * If the given string starts with '%', but the properties do not contain a corresponding entry, the original string
   * is returned.
   * 
   * @param translatableString
   *          If <code>null</code>, <code>null</code> is returned.
   * @param i10nProperties
   *          If <code>null</code>, we always return <code>translatableString</code>.
   * @return If <code>translatableString</code> starts with '%',
   *         <code>i10nproperties.get( translatableString.substring(1) )</code> is returned. Else return
   *         <code>translatableString</code>.
   */
  public static String getI18NString( final String translatableString, final Properties i10nProperties )
  {
    if( translatableString == null )
      return null;

    if( translatableString.isEmpty() )
      return translatableString;

    if( i10nProperties == null )
      return translatableString;

    if( translatableString.charAt( 0 ) == '%' )
    {
      final String key = translatableString.substring( 1 );
      return i10nProperties.getProperty( key, translatableString );
    }

    return translatableString;
  }
}
