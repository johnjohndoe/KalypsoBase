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
package org.kalypsodeegree_impl.io.sax.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.io.sax.parser.TriangulatedSurfaceContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author felipe
 *
 */
public class MessungTriangulatedSurfaceContentHandlerTest
{
  public static int N_EXECUTION = 300;
  public static int WARM_UP_SAMPLES = Math.round( (float)0.15 * N_EXECUTION );
  public static String indiviualTimesFileName = "/home/felipe/Desktop/kalypso_all_refactored.txt";
  
  public void loadSurface( ) throws IOException, ParserConfigurationException, SAXException
  {
    // load a surface from a file    
    final MillisTimeLogger allLogger = new MillisTimeLogger();
    long[] individualTime = new long[N_EXECUTION];

    for( int i = 0; i < N_EXECUTION; i++ )
    {
      final MillisTimeLogger oneSurfaceLogger = new MillisTimeLogger();
 
      final GM_TriangulatedSurface surface = readTriangles( new InputSource( MessungTriangulatedSurfaceContentHandlerTest.class.getResourceAsStream( "resources/tin_TERRAIN" ) ) );
      
      individualTime[ i ] = oneSurfaceLogger.takeInterimTime();
    }

    allLogger.takeInterimTime();
    allLogger.printTotalTime( "All read in: " );
    
    saveIndividualTimes( individualTime );
  }
  
  private void saveIndividualTimes( final long[] times) {
    try {
        PrintStream os = new PrintStream( new File( indiviualTimesFileName ) );
        
        for( int i = WARM_UP_SAMPLES; i < times.length; i++ )
        {
            os.println( times[i] );             
        }
    } catch (Exception e) {
        System.out.println( "Error when writing to file: " + e.getMessage() );
    }       
  }
  
  
  private GM_TriangulatedSurface readTriangles( final InputSource is ) throws IOException, ParserConfigurationException, SAXException
  {
    final SAXParserFactory saxFac = SAXParserFactory.newInstance();
    saxFac.setNamespaceAware( true );

    final SAXParser saxParser = saxFac.newSAXParser();
    // make namespace-prefixes visible to content handler
    // used to allow necessary schemas from gml document
    final XMLReader xmlReader = saxParser.getXMLReader();
    xmlReader.setFeature( "http://xml.org/sax/features/namespace-prefixes", Boolean.TRUE ); //$NON-NLS-1$

    final GM_TriangulatedSurface[] result = new GM_TriangulatedSurface[1];
    final UnmarshallResultEater resultEater = new UnmarshallResultEater()
    {
      @Override
      public void unmarshallSuccesful( final Object value )
      {
        result[0] = (GM_TriangulatedSurface) value;
      }
    };
    final TriangulatedSurfaceContentHandler contentHandler = new TriangulatedSurfaceContentHandler( resultEater, xmlReader );
    xmlReader.setContentHandler( contentHandler );
    xmlReader.parse( is );

    return result[0];
  }
  
  public static void main (String[] args)
  {
    MessungTriangulatedSurfaceContentHandlerTest messung = new MessungTriangulatedSurfaceContentHandlerTest();
    try
    {
      messung.loadSurface();
    }
    catch( Exception e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
