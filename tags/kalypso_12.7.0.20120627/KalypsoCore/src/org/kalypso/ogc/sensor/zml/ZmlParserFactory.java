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
package org.kalypso.ogc.sensor.zml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.factory.FactoryException;
import org.kalypso.commons.parser.IParser;
import org.kalypso.commons.parser.ParserFactory;

/**
 * @author Gernot Belger
 */
public final class ZmlParserFactory
{
  private static Properties PARSER_PROPERTIES = null;

  private static ParserFactory PARSER_FACTORY = null;

  private ZmlParserFactory( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Helper, man sollte es benutzen um auf die ParserFactory zugreifen zu können
   * 
   * @return parser factory
   */
  static synchronized ParserFactory getParserFactory( )
  {
    if( PARSER_FACTORY == null )
      PARSER_FACTORY = new ParserFactory( getProperties(), ZmlFactory.class.getClassLoader() );

    return PARSER_FACTORY;
  }

  private static synchronized Properties getProperties( )
  {
    if( PARSER_PROPERTIES == null )
    {
      InputStream ins = null;

      try
      {
        PARSER_PROPERTIES = new Properties();

        ins = ZmlFactory.class.getResourceAsStream( "resource/types2parser.properties" ); //$NON-NLS-1$

        PARSER_PROPERTIES.load( ins );

        ins.close();

        return PARSER_PROPERTIES;
      }
      catch( final IOException e )
      {
        throw new RuntimeException( e );
      }
      finally
      {
        IOUtils.closeQuietly( ins );
      }
    }

    return PARSER_PROPERTIES;
  }

  /**
   * Supported types are listed in the types2parser.properties file. TODO: noch das default format (_format) hinzufügen
   * und eventuell die xs: Zeugs wegmachen Siehe properties datei
   * 
   * @return the XSD-Type for the given Java-Class
   */
  static String getXSDTypeFor( final String className )
  {
    return getProperties().getProperty( className );
  }

  // if format not specified, then we use the default specification
  // found in the properties file. Every type can have a default format
  // declared in this file using the convention that the property
  // must be build using the type name followed by the '_format' string.
  private static String getFormat( final String type, final String override )
  {
    if( !StringUtils.isEmpty( override ) )
      return override;

    return getProperties().getProperty( type + "_format" ); //$NON-NLS-1$
  }

  public static IParser createParser( final String type, final String override ) throws FactoryException
  {
    final String format = ZmlParserFactory.getFormat( type, override );

    return getParserFactory().createParser( type, format );
  }
}