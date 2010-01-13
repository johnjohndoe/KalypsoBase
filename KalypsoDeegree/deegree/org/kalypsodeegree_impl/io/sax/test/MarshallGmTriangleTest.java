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
package org.kalypsodeegree_impl.io.sax.test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.deegree.model.crs.UnknownCRSException;
import org.junit.Test;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree_impl.io.sax.marshaller.TriangulatedSurfaceMarshaller;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Gernot Belger
 */
public class MarshallGmTriangleTest
{
// @Test
// public void writeOneTriangle( ) throws IOException, GM_Exception, SAXException, UnknownCRSException
// {
// final File tinFile = File.createTempFile( "tinTest", ".gml" );
// tinFile.deleteOnExit();
//
// OutputStream os = null;
// try
// {
// os = new BufferedOutputStream( new FileOutputStream( tinFile ) );
//
// // Create XMLReader, where xml-output is written to;
// // A bit hacky, but the official way (using transformers) is way too complicated
// final ToXMLStream ch = new ToXMLStream();
// ch.setOutputStream( os );
// // Configure content handler. IMPORTANT: call after setOutputStream!
// ch.setLineSepUse( true );
// ch.setIndent( true );
// ch.setIndentAmount( 2 );
//
// final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
// xmlReader.setContentHandler( ch );
//
// final TriangulatedSurfaceMarshaller marshaller = new TriangulatedSurfaceMarshaller( xmlReader, null );
//
// // Write header
// ch.startDocument();
// marshaller.startSurface( TriangulatedSurfaceMarshaller.EMPTY_ATTRIBUTES );
//
// // write one triangle
// final GM_Position pos1 = GeometryFactory.createGM_Position( 0.0, 0.0, 1.0 );
// final GM_Position pos2 = GeometryFactory.createGM_Position( 0.0, 1.0, 2.0 );
// final GM_Position pos3 = GeometryFactory.createGM_Position( 1.0, 0.0, 3.0 );
// final GM_Triangle triangle = GeometryFactory.createGM_Triangle( pos1, pos2, pos3, null );
// marshaller.marshalTriangle( triangle, null );
//
// // Write footer
// marshaller.endSurface();
// // IMPORTANT: call endDocument before stream is closed,
// // else internal writer is not flushed and not everything may be written into the file
// ch.endDocument();
//
// os.close();
//
// final String xmlString = FileUtils.readFileToString( tinFile, "UTF-8" );
// System.out.println( xmlString );
// }
// finally
// {
// tinFile.delete();
//
// IOUtils.closeQuietly( os );
// }
// }


  @Test
  public void writeONeTriangleTransform( ) throws IOException, TransformerConfigurationException, SAXException, GM_Exception, UnknownCRSException
  {
    final String charsetEncoding = "UTF-8";

    final File tinFile = File.createTempFile( "tinTest", ".gml" );
    tinFile.deleteOnExit();

    OutputStream os = null;
    try
    {
      /* Output: to stream */
      os = new BufferedOutputStream( new FileOutputStream( tinFile ) );
      final Result result = new StreamResult( os );

      /* Create a transformer handler that will receive the contents */
      final SAXTransformerFactory tFac = (SAXTransformerFactory) TransformerFactory.newInstance();

      final TransformerHandler tHandler = tFac.newTransformerHandler();
      tHandler.setResult( result );

      final Transformer transformer = tHandler.getTransformer();
      transformer.setOutputProperty( OutputKeys.ENCODING, charsetEncoding );
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" ); //$NON-NLS-1$
      transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "1" );
      transformer.setOutputProperty( OutputKeys.METHOD, "xml" ); //$NON-NLS-1$

      final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      xmlReader.setContentHandler( tHandler );

      final TriangulatedSurfaceMarshaller marshaller = new TriangulatedSurfaceMarshaller( xmlReader, null );

      // Write header
      tHandler.startDocument();
      marshaller.startSurface( TriangulatedSurfaceMarshaller.EMPTY_ATTRIBUTES );

      // write one triangle
      final GM_Position pos1 = GeometryFactory.createGM_Position( 0.0, 0.0, 1.0 );
      final GM_Position pos2 = GeometryFactory.createGM_Position( 0.0, 1.0, 2.0 );
      final GM_Position pos3 = GeometryFactory.createGM_Position( 1.0, 0.0, 3.0 );
      final GM_Triangle triangle = GeometryFactory.createGM_Triangle( pos1, pos2, pos3, null );
      marshaller.marshallTriangle( triangle, null );

      // Write footer
      marshaller.endSurface();
      // IMPORTANT: call endDocument before stream is closed,
      // else internal writer is not flushed and not everything may be written into the file
      tHandler.endDocument();

      os.close();

      final String xmlString = FileUtils.readFileToString( tinFile, "UTF-8" );
      System.out.println( xmlString );
    }
    finally
    {
      tinFile.delete();

      IOUtils.closeQuietly( os );
    }

  }

}
