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
package org.kalypso.service.wps.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.service.wps.utils.ogc.OGCNamespacePrefixMapper;

/**
 * This class should help with handling XMLs and Binding-Objects.
 * 
 * @author Holger Albert
 */
public class MarshallUtilities
{
  /**
   * Context for marshalling and unmarshalling.
   */
  private static JAXBContext CONTEXT = JaxbUtilities.createQuiet( net.opengeospatial.ows.ObjectFactory.class, net.opengeospatial.wps.ObjectFactory.class );

  /**
   * The constructor.
   */
  public MarshallUtilities( )
  {
  }

  /**
   * Returns the XML-String of a binded object.
   * 
   * @return The XML representation as string.
   */
  public static String marshall( Object object ) throws JAXBException, IOException
  {
    /* Will be marshalled into a StringWriter. */
    StringWriter sw = new StringWriter();

    /* Create the marshaller. */
    Marshaller m = CONTEXT.createMarshaller();

    /* Set the properties. */
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    m.setProperty( "com.sun.xml.bind.namespacePrefixMapper", new OGCNamespacePrefixMapper() );

    /* Marshall ... */
    m.marshal( object, sw );
    sw.close();

    return sw.toString();
  }

  /**
   * Returns the object what out of the xml.
   * 
   * @return The binded object.
   */
  public static Object unmarshall( String xml ) throws JAXBException
  {
    /* Create the unmarshaller. */
    Unmarshaller um = CONTEXT.createUnmarshaller();

    /* Unmarshall ... */
    Object object = um.unmarshal( new StringReader( xml ) );

    return object;
  }

  /**
   * This function returns an input stream from one string.
   * 
   * @param text
   *          The string.
   * @return The inputstream.
   */
  public static InputStream getInputStream( String text )
  {
    return new ByteArrayInputStream( text.getBytes() );
  }

  /**
   * This function reads from an input stream and returns a string representation.
   * 
   * @param is
   *          The input stream.
   * @return The string.
   */
  public static String fromInputStream( InputStream is ) throws IOException
  {
    String value = null;
    BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );

    try
    {
      String line = null;
      StringBuffer buffer = new StringBuffer();

      while( (line = reader.readLine()) != null )
        buffer.append( line + "\n" );

      reader.close();
      value = buffer.toString();
    }
    finally
    {
      IOUtils.closeQuietly( reader );
    }

    return value;
  }
}