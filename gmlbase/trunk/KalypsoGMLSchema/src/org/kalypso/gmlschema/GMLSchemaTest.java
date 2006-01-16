/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.gmlschema;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;

import org.kalypso.gmlschema.builder.GMLSchemaBuilder;

import junit.framework.TestCase;

/**
 * 
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public class GMLSchemaTest extends TestCase
{
  public static final String NS_GML2 = "http://www.opengis.net/gml";

  //  private SchemaResourceManager m_manager;

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    final File schemaResourceDir = new File( "C:\\TMP\\schemaResourceDir" );
    schemaResourceDir.mkdirs();
    //    m_manager = new SchemaResourceManager( schemaResourceDir );
  }

  public void testLoadSchema() throws Exception
  {
    try
    {
      final GMLSchemaBuilder schemaBuilder = GMLSchemaBuilder.getInstance();
      //      final URL schemaLocationURL = getClass().getResource( "resources/feature.xsd" );
      //      GMLSchema schema2 = GMLSchemaBuilder.generateGMLSchema( NS_GML2, schemaLocationURL );

      //      final URL schemaLocationURL = getClass().getResource( "resources/namodell.xsd" );
      //      GMLSchema schema = schemaBuilder.generateGMLSchema( "http://www.tuhh.de/kalypsoNA", schemaLocationURL );

      final URL schemaLocationURL = getClass().getResource( "resources/xplanung/BPlanGML_2.xsd" );
      final GMLSchema schema = schemaBuilder.generateGMLSchema( "http://www.xplanung.de/bplangml", schemaLocationURL );

      //      final URL schemaLocationURL = getClass().getResource( "resources/XMLSchema.xsd" );
      //      GMLSchema schema2 = GMLSchemaBuilder.generateGMLSchema( GMLSchemaConstants.NS_XMLSCHEMA, schemaLocationURL );
      if( schema != null )
      {
        final StringWriter writer = new StringWriter();
        schema.toStringTree( writer, 0 );
        System.out.println( writer.toString() );
        System.out.println( "super" );
      }
    }
    catch( Exception e )
    {
      e.printStackTrace();
      throw e;
    }
    //    final SchemaResource resource = m_manager.lookupResource( NS_GML2, schemaLocationURL.toString() );
    //    final String schemaLocation = resource.getSchemaLocation();
    //    final Schema schema = resource.getSchema();
    //    final Import[] importArray = schema.getImportArray();
    //    String schemaLocation2 = importArray[0].getSchemaLocation();
    //    final Include[] includeArray = schema.getIncludeArray();
    //    String schemaLocation3 = includeArray[0].getSchemaLocation();
    //    final String namespace = resource.getNamespace();
    //    System.out.println( "fertig" );
  }
}
