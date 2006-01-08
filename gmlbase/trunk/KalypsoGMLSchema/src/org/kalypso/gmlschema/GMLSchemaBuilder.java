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
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.tool.SchemaResourceManager;
import org.apache.xmlbeans.impl.tool.SchemaImportResolver.SchemaResource;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;

/**
 * 
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public class GMLSchemaBuilder
{
  private static SchemaResourceManager m_manager;

  static
  {
    final File schemaResourceDir = new File( "C:\\TMP\\schemaResourceDir" );
    schemaResourceDir.mkdirs();

    m_manager = new SchemaResourceManager( schemaResourceDir );
    final String[] xsdFileNames = schemaResourceDir.list();
    m_manager.syncCacheWithLocalXsdFiles( xsdFileNames, false );
    //    m_manager.processAll(false, true, false)
  }

  public static GMLSchema generateGMLSchema( String urn, URL schemaLocationURL ) throws Exception
  {
    URL urlSchemaLocation = getSchemaLocationForURN( urn, schemaLocationURL );
    final SchemaResource resource = m_manager.lookupResource( urn, urlSchemaLocation.toExternalForm() );
    final Schema schema = resource.getSchema();
    if( schema == null )
    {
      throw new Exception( "Error bei schema... unthrown exception from apache :-( find cause with debug... " );
    }
    final GMLSchema gmlSchema = new GMLSchema( schema, Logger.global, schemaLocationURL );
    gmlSchema.init();
    rBuild( gmlSchema, gmlSchema );
    gmlSchema.rInit();
    //    buildFeatureElements( gmlSchema );
    //    initFeatureContentTypes( gmlSchema );
    //    final StringWriter writer = new StringWriter();
    //    gmlSchema.toStringTree( writer, 0 );
    //    System.out.println( writer.toString() );
    return gmlSchema;
  }

  /**
   * @param urn
   * @param schemaLocationURL
   */
  private static URL getSchemaLocationForURN( String urn, URL schemaLocationURL )
  {
    if( urn.equalsIgnoreCase( GMLSchemaConstants.NS_GML2 ) )
      return GMLSchemaBuilder.class.getResource( "resources/feature.xsd" );
    if( urn.equalsIgnoreCase( GMLSchemaConstants.NS_XLINK ) )
      return GMLSchemaBuilder.class.getResource( "resources/xlinks.xsd" );
    return schemaLocationURL;
  }

  private static IBuilder getBuilderFor( GMLSchema gmlSchema, Object object )
  {
    if(object instanceof FeatureContentType)
      return new FeatureContentType2ElementBuilder();
    if( object instanceof GMLSchema )
    {
      return new GMLSchemaBuilder2();
    }
    if( object instanceof FeatureType )
      return new FeatureType2ComplexTypeBuilder();
    if( object instanceof PropertyType )
      return new PropertyType2SimpleTypeBuilder(); // ?
    final QName baseType;
    if( object instanceof Element )
    {
      baseType = GMLSchemaUtilities.findBaseType( gmlSchema, (Element)object );
      return getElementBuilderForBaseType( baseType );
    }
    else if( object instanceof SimpleType )
    {
      baseType = GMLSchemaUtilities.findBaseType( gmlSchema, (SimpleType)object );
      return getContentBuilderForBaseType( baseType );
    }
    else if( object instanceof ComplexType )
    {
      baseType = GMLSchemaUtilities.findBaseType( gmlSchema, (ComplexType)object );
      return getContentBuilderForBaseType( baseType );
    }
    return null;
  }

  private static IBuilder getContentBuilderForBaseType( QName baseType )
  {
    if( baseType == null )
      return null;
    final String namespaceURI = baseType.getNamespaceURI();
    final String localPart = baseType.getLocalPart();

    // GML
    if( GMLSchemaConstants.NS_GML2.equals( namespaceURI ) )
    {
      if( "AbstractFeatureType".equals( localPart ) )
        return new ComplexType2FeatureContentTypeBuilder();
      if( "AbstractGeometryType".equals( localPart ) )
        return null;
    }
    // XML-SCHEMA
    else if( GMLSchemaConstants.NS_XMLSCHEMA.equals( namespaceURI ) )
    {
      if( "anyType".equals( localPart ) ) // how to assign a builder for this ?
        return null; // TODO implement builder for "anyType"
      return new SimpleType2PropertyContentTypeBuilder();
    }

    System.out.println( "no builder for type: " + baseType );
    return null;
  }

  /**
   * 
   * @param baseType
   */
  private static IBuilder getElementBuilderForBaseType( QName baseType )
  {
    if( baseType == null )
      return null;
    final String namespaceURI = baseType.getNamespaceURI();
    final String localPart = baseType.getLocalPart();

    // GML
    if( GMLSchemaConstants.NS_GML2.equals( namespaceURI ) )
    {
      if( "AbstractFeatureType".equals( localPart ) )
        return new Element2FeatureTypeBuilder();
    }
    // XML-SCHEMA
    else if( GMLSchemaConstants.NS_XMLSCHEMA.equals( namespaceURI ) )
    {
      if( "anyType".equals( localPart ) ) // how to assign a builder for this ?
        return null; // TODO implement builder for "anyType"
      return new Element2PropertyTypeBuilder();
    }

    System.out.println( "no builder for type: " + baseType );
    return null;

  }

  public static void rBuild( GMLSchema gmlSchema, Object object )
  {
    final IBuilder builder = getBuilderFor( gmlSchema, object );
    if( builder != null && !gmlSchema.hasBuildedObjectFor( object ) )
    {
      final Object[] buildedObjects = builder.build( gmlSchema, object );
      for( int i = 0; i < buildedObjects.length; i++ )
      {
        final Object buildedObject = buildedObjects[i];
        if( buildedObject != null ) // TODO and not registered
        {
          //          gmlSchema.register( object, buildedObject );
          //          System.out.println( "OBJECT:" + buildedObject.toString() );
        }
        else
          System.out.println( "UNKNOWN" );
      }
      rbuild( gmlSchema, buildedObjects );
    }
    else
      System.out.println( "no builder" );
  }

  /**
   * @param gmlSchema
   * @param objects
   */
  private static void rbuild( GMLSchema gmlSchema, Object[] objects )
  {
    for( int i = 0; i < objects.length; i++ )
    {
      final Object object = objects[i];
      rBuild( gmlSchema, object );
    }
  }
}
