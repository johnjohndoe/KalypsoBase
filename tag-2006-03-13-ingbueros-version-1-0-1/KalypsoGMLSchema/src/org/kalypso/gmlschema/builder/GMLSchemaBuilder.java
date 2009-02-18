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

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaConstants;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.feature.FeatureContentType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.PropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.relation.RelationContentType;

/**
 * schema-builder for a special gml version (e.g. gml_2.1.x)
 * 
 * @author doemming
 */
public class GMLSchemaBuilder
{
  final private Set<IBuilder> m_registeredBuilders = new HashSet<IBuilder>();

  private final String m_gmlVersion;

  public String getGMLVersion( )
  {
    return m_gmlVersion;
  }

  public GMLSchemaBuilder( final String gmlVersion )
  {
    m_gmlVersion = gmlVersion;
  }

  public void registerBuilder( final IBuilder builder )
  {
    m_registeredBuilders.add( builder );
  }

  private IBuilder getBuilderFor( final GMLSchema gmlSchema, final Object object, final String namedPass )
  {
    for( Iterator iterator = m_registeredBuilders.iterator(); iterator.hasNext(); )
    {
      final IBuilder builder = (IBuilder) iterator.next();
      if( builder.isBuilderFor( gmlSchema, object, null ) )
        return builder;
    }
    return null;
  }

  public GMLSchema buildGMLSchema( final InputStream inputStream, final URL context ) throws GMLSchemaException
  {
    SchemaDocument document;
    try
    {
      document = SchemaDocument.Factory.parse( inputStream );
    }
    catch( Exception e )
    {
      throw new GMLSchemaException( "could not parse schema: " + e.getMessage(), e );
    }
    final Schema schema = document.getSchema();
    if( schema == null )
    {
      // this is crap, apache catches the exception and keeps it :-(
      // TODO send bugreport/featurerequest to apache
      // documentation of errors:
      // - namespace not set (e.g. xmlns:adv="...")
      // TODO use a schema-validator to report
      throw new GMLSchemaException( "invalid schema! (no error message from apache ), find cause with debug... or use schema-validator " );
    }
    final GMLSchema gmlSchema = new GMLSchema( schema, context, m_gmlVersion );
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
   */
  private void rInit( final GMLSchema gmlSchema )
  {
    for( int n = 0; n < IInitialize.INIT_ORDER.length; n++ )
    {
      int initRun = IInitialize.INIT_ORDER[n];

      final FeatureContentType[] featureContentTypes = gmlSchema.getAllFeatureContentTypes();
      for( int i = 0; i < featureContentTypes.length; i++ )
        featureContentTypes[i].init( initRun );

      final IFeatureType[] featureTypes = gmlSchema.getAllFeatureTypes();
      for( int i = 0; i < featureTypes.length; i++ )
        featureTypes[i].init( initRun );

      final PropertyType[] propertyTypes = gmlSchema.getAllPropertyTypes();
      for( int i = 0; i < propertyTypes.length; i++ )
        propertyTypes[i].init( initRun );

      final IPropertyContentType[] propertyContentTypes = gmlSchema.getAllPropertyContentTypes();
      for( int i = 0; i < propertyContentTypes.length; i++ )
        propertyContentTypes[i].init( initRun );

      final RelationContentType[] relationContentTypes = gmlSchema.getAllRelationContentTypes();
      for( int i = 0; i < relationContentTypes.length; i++ )
        relationContentTypes[i].init( initRun );

      final IRelationType[] relationTypes = gmlSchema.getAllRelationTypes();
      for( int i = 0; i < relationTypes.length; i++ )
        relationTypes[i].init( initRun );
    }
  }

  private void rBuild( GMLSchema gmlSchema, Object object )
  {
    final IBuilder builder = getBuilderFor( gmlSchema, object, null );
    if( builder != null && !gmlSchema.hasBuildedObjectFor( object ) )
    {
      final Object[] buildedObjects = builder.build( gmlSchema, object );
      rbuild( gmlSchema, buildedObjects );
    }
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

  /**
   * @param urn
   * @param schemaLocationURL
   */
  public static URL getSchemaLocationForURN( String urn, URL schemaLocationURL )
  {
    // TODO use url catalog
    if( urn.equalsIgnoreCase( GMLSchemaConstants.NS_GML2 ) )
      return GMLSchemaBuilder.class.getResource( "../resources/feature.xsd" );
    if( urn.equalsIgnoreCase( GMLSchemaConstants.NS_XLINK ) )
      return GMLSchemaBuilder.class.getResource( "../resources/xlinks.xsd" );
    return schemaLocationURL;
  }
}
