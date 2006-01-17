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
package org.kalypso.gmlschema.builder;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.xmlbeans.impl.tool.SchemaResourceManager;
import org.apache.xmlbeans.impl.tool.SchemaImportResolver.SchemaResource;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaConstants;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.FeatureContentType;
import org.kalypso.gmlschema.feature.FeatureType;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.PropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.relation.RelationContentType;

/**
 * 
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public class GMLSchemaBuilder
{
  final private SchemaResourceManager m_manager;

  final private Set m_registeredBuilders = new HashSet();

  private static GMLSchemaBuilder m_instance = null;

  public static GMLSchemaBuilder getInstance()
  {
    if( m_instance == null )
    {
      m_instance = new GMLSchemaBuilder();
      m_instance.registerBuilder( new GMLSchema2SchemaBasicsBuilder() );
      // complex type ...
      m_instance.registerBuilder( new ComplexType2FeatureContentTypeBuilder() );
      m_instance.registerBuilder( new FeatureContentType2ElementBuilder() );
      // element ...
      m_instance.registerBuilder( new Element2FeatureTypeBuilder() );
      m_instance.registerBuilder( new FeatureType2ComplexTypeBuilder() );
      // element ...
      m_instance.registerBuilder( new Element2PropertyTypeBuilder() );
      m_instance.registerBuilder( new PropertyType2SimpleTypeBuilder() );
      // simple type...
      m_instance.registerBuilder( new SimpleType2PropertyContentTypeBuilder() );
      // relations
      //      m_instance.registerBuilder( new Element2RelationTypeBuilder() );
      m_instance.registerBuilder( new RelationType2ComplexTypeBuilder() );
      m_instance.registerBuilder( new ComplexType2RelationContentTypeBuilder() );
      // test
      m_instance.registerBuilder( new ComplexType2GeometryPropertyContentTypeBuilder() );
      m_instance.registerBuilder( new AdvElement2RelationTypeBuilder() );
    }
    return m_instance;
  }

  private GMLSchemaBuilder()
  {
    final File schemaResourceDir = new File( "C:\\TMP\\schemaResourceDir" );
    schemaResourceDir.mkdirs();

    m_manager = new SchemaResourceManager( schemaResourceDir );
    final String[] xsdFileNames = schemaResourceDir.list();
    m_manager.syncCacheWithLocalXsdFiles( xsdFileNames, false );
    //    m_manager.processAll(false, true, false)
  }

  public void registerBuilder( final IBuilder builder )
  {
    m_registeredBuilders.add( builder );
  }

  private IBuilder getBuilderFor( final GMLSchema gmlSchema, final Object object, final String namedPass )
  {
    for( Iterator iterator = m_registeredBuilders.iterator(); iterator.hasNext(); )
    {
      final IBuilder builder = (IBuilder)iterator.next();
      if( builder.isBuilderFor( gmlSchema, object, null ) )
        return builder;
    }
    return null;
  }

  public GMLSchema generateGMLSchema( final String urn, final URL schemaLocationURL ) throws GMLSchemaException
  {
    final URL urlSchemaLocation = getSchemaLocationForURN( urn, schemaLocationURL );
    final SchemaResource resource = m_manager.lookupResource( urn, urlSchemaLocation.toExternalForm() );
    final Schema schema = resource.getSchema();
    if( schema == null )
    {
      throw new GMLSchemaException( "Error bei schema... unthrown exception from apache :-(, find cause with debug... " );
    }
    final GMLSchema gmlSchema = new GMLSchema( schema, Logger.global, schemaLocationURL );
    // I Step build the objects
    rBuild( gmlSchema, gmlSchema );
    // II Step initialize the elements
    rInit( gmlSchema );
    return gmlSchema;
  }

  /**
   * initilize builded objects
   * 
   * @param gmlSchema
   *  
   */
  private void rInit( final GMLSchema gmlSchema )
  {
    final FeatureContentType[] featureContentTypes = gmlSchema.getAllFeatureContentTypes();
    for( int i = 0; i < featureContentTypes.length; i++ )
      featureContentTypes[i].init();

    final FeatureType[] featureTypes = gmlSchema.getAllFeatureTypes();
    for( int i = 0; i < featureTypes.length; i++ )
      featureTypes[i].init();

    final PropertyType[] propertyTypes = gmlSchema.getAllPropertyTypes();
    for( int i = 0; i < propertyTypes.length; i++ )
      propertyTypes[i].init();

    final IPropertyContentType[] propertyContentTypes = gmlSchema.getAllPropertyContentTypes();
    for( int i = 0; i < propertyContentTypes.length; i++ )
      propertyContentTypes[i].init();

    final RelationContentType[] relationContentTypes = gmlSchema.getAllRelationContentTypes();
    for( int i = 0; i < relationContentTypes.length; i++ )
      relationContentTypes[i].init();

    final IRelationType[] relationTypes = gmlSchema.getAllRelationTypes();
    for( int i = 0; i < relationTypes.length; i++ )
      relationTypes[i].init();
  }

  /**
   * @param urn
   * @param schemaLocationURL
   */
  private static URL getSchemaLocationForURN( String urn, URL schemaLocationURL )
  {
    // TODO use schema cache
    if( urn.equalsIgnoreCase( GMLSchemaConstants.NS_GML2 ) )
      return GMLSchemaBuilder.class.getResource( "../resources/feature.xsd" );
    if( urn.equalsIgnoreCase( GMLSchemaConstants.NS_XLINK ) )
      return GMLSchemaBuilder.class.getResource( "../resources/xlinks.xsd" );
    return schemaLocationURL;
  }

  private void rBuild( GMLSchema gmlSchema, Object object )
  {
    final IBuilder builder = getBuilderFor( gmlSchema, object, null );
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
  private void rbuild( GMLSchema gmlSchema, Object[] objects )
  {
    for( int i = 0; i < objects.length; i++ )
    {
      final Object object = objects[i];
      rBuild( gmlSchema, object );
    }
  }
}
