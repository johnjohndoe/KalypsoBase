/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.service.wps.utils.WPSUtilities.WPS_VERSION;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

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
  private static JAXBContext CONTEXT_1_0_0 = JaxbUtilities.createQuiet( net.opengis.ows._1.ObjectFactory.class, net.opengis.wps._1_0.ObjectFactory.class );

  private static JAXBContext CONTEXT_0_4_0 = JaxbUtilities.createQuiet( net.opengeospatial.ows.ObjectFactory.class, net.opengeospatial.wps.ObjectFactory.class );

  private static DocumentBuilderFactory m_factory = DocumentBuilderFactory.newInstance();

  private static XPathFactory m_xpFactory = XPathFactory.newInstance();

  /**
   * The constructor.
   */
  private MarshallUtilities( )
  {
  }

  /**
   * Returns the XML-String of a binded object.
   * 
   * @return The XML representation as string.
   */
  public static String marshall( final Object object, final WPS_VERSION wpsVersion ) throws JAXBException, IOException
  {
    /* Will be marshalled into a StringWriter. */
    final Marshaller m;
    switch( wpsVersion )
    {
      case V040:
        m = CONTEXT_0_4_0.createMarshaller();
        break;
      case V100:
        m = CONTEXT_1_0_0.createMarshaller();
        break;
      default:
        throw new JAXBException( "Unsupported WPS version " + wpsVersion ); //$NON-NLS-1$
    }
    /* Create the marshaller. */
    final StringWriter sw = new StringWriter();

    /* Set the properties. */
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
// m.setProperty( "com.sun.xml.bind.namespacePrefixMapper", NAMESPACE_PREFIX_MAPPER );
// FIXME: namespace definitions are missing in resulting xml-document!
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
  public static Object unmarshall( final String xml ) throws JAXBException
  {
    try
    {
      final XPath xpath = m_xpFactory.newXPath();
      final XPathExpression expr = xpath.compile( "//@version" ); //$NON-NLS-1$
      final XPathExpression expr2 = xpath.compile( "//AcceptVersions/Version/text()" ); //$NON-NLS-1$

      final DocumentBuilder builder = m_factory.newDocumentBuilder();
      final InputSource is = new InputSource( new StringReader( xml ) );
      final Document doc = builder.parse( is );

      // try both expressions
      final String version;
      Node node = (Node) expr.evaluate( doc, XPathConstants.NODE );
      if( node == null )
      {
        node = (Node) expr2.evaluate( doc, XPathConstants.NODE );
      }

      if( node == null )
      {
        // fallback to version 0.4.0
        version = "0.4.0";
      }
      else
      {
        version = node.getTextContent();
      }

      final WPS_VERSION wpsVersion = WPSUtilities.WPS_VERSION.getValue( version );
      return unmarshall( xml, wpsVersion );
    }
    catch( final JAXBException e )
    {
      throw e;
    }
    catch( final Exception e )
    {
      throw new JAXBException( "Malformed document", e ); //$NON-NLS-1$
    }
  }

  public static Object unmarshall( final String xml, final WPS_VERSION wpsVersion ) throws JAXBException
  {
    /* Create the unmarshaller depending on WPS version */
    final Unmarshaller um;
    switch( wpsVersion )
    {
      case V040:
        um = CONTEXT_0_4_0.createUnmarshaller();
        break;
      case V100:
        um = CONTEXT_1_0_0.createUnmarshaller();
        break;
      default:
        throw new JAXBException( "Unsupported WPS version " + wpsVersion ); //$NON-NLS-1$
    }

    return um.unmarshal( new StringReader( xml ) );
  }

  /**
   * This function returns an input stream from one string.
   * 
   * @param text
   *          The string.
   * @return The inputstream.
   */
  public static InputStream getInputStream( final String text )
  {
    return new ByteArrayInputStream( text.getBytes() );
  }
}