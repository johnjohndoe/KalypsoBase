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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.kalypso.commons.i18n.ResourceBundleUtils;
import org.kalypso.gmlschema.Debug;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.FeatureContentType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.relation.RelationContentType;

/**
 * This gml schema-builder generates the GML-Schema information ( {@link IFeatureType}s / {@link IPropertyType}s ) from
 * a xmlbeans-parsed schema.
 * <p>
 * Both gml-version (2.1.x and 3.1.1) are supported via a general builder-factory.
 * 
 * @author Andreas von D�mming
 */
public class GMLSchemaBuilder
{
  private final List<IBuilder> m_registeredBuilders = new ArrayList<IBuilder>();

  private final String m_gmlVersion;

  public GMLSchemaBuilder( final String gmlVersion )
  {
    m_gmlVersion = gmlVersion;
  }

  public String getGMLVersion( )
  {
    return m_gmlVersion;
  }

  public void registerBuilder( final IBuilder builder )
  {
    m_registeredBuilders.add( builder );
  }

  public GMLSchema buildGMLSchema( final SchemaDocument schemaDocument, final URL context ) throws GMLSchemaException
  {
    final String namespace = schemaDocument.getSchema().getTargetNamespace();

    // Read I18N Properties from the same location
    // REMARK: this is somewhat special, however is the most effective way
    // to do it. Maybe we should some day provide a general hook at this place to do such things?
    Debug.PARSING_VERBOSE.printf( "Searching I18N-property file: Start: %s%n", namespace ); //$NON-NLS-1$
    final ResourceBundle bundle = ResourceBundleUtils.loadResourceBundle( context );
    Debug.PARSING_VERBOSE.printf( "Searching I18N-property file: OK: %s%n", namespace ); //$NON-NLS-1$

    Debug.PARSING.printf( "Building GML-Schema: %s%n", namespace ); //$NON-NLS-1$

    Debug.PARSING_VERBOSE.printf( "Preparing: Start: %s%n", namespace ); //$NON-NLS-1$
    final GMLSchema gmlSchema = new GMLSchema( schemaDocument, context, m_gmlVersion, bundle );
    Debug.PARSING_VERBOSE.printf( "Preparing: OK: %s%n", namespace ); //$NON-NLS-1$

    // I Step build the objects
    Debug.PARSING_VERBOSE.printf( "Building GML-Schema: Start: %s%n", namespace ); //$NON-NLS-1$
    rBuild( gmlSchema, gmlSchema );
    Debug.PARSING_VERBOSE.printf( "Building GML-Schema: OK: %s%n", namespace ); //$NON-NLS-1$

    // II Step initialize the elements
    Debug.PARSING_VERBOSE.printf( "Initialising: Start: %s%n", namespace ); //$NON-NLS-1$
    rInit( gmlSchema );
    Debug.PARSING_VERBOSE.printf( "Initialising: Start: OK: %s%n", namespace ); //$NON-NLS-1$

    Debug.PARSING.printf( "GML-Schema has been built: %s%n", namespace ); //$NON-NLS-1$

    return gmlSchema;
  }

  private void rBuild( final GMLSchema gmlSchema, final Object object ) throws GMLSchemaException
  {
// if( object.toString().contains( "CatchmentFeatureType" ) )
// System.out.println( "stop" );
//
// if( object instanceof ElementWithOccurs )
// {
// final ElementWithOccurs elt = (ElementWithOccurs) object;
// if( "Ort".equals( elt.getElement().getName() ) )
// System.out.println( "stop" );
// }
//
    final IBuilder builder = getBuilderFor( gmlSchema, object );

    if( builder != null && !gmlSchema.hasBuildedObjectFor( object ) )
    {
      final Object[] buildedObjects = builder.build( gmlSchema, object );
      for( final Object object1 : buildedObjects )
        rBuild( gmlSchema, object1 );
    }
  }

  private IBuilder getBuilderFor( final GMLSchema gmlSchema, final Object object ) throws GMLSchemaException
  {
    IBuilder result = null;

    Debug.PARSING_VERBOSE.printf( "Searching builder for:%n%s%n", object ); //$NON-NLS-1$

    for( final IBuilder builder : m_registeredBuilders )
    {
      if( builder.isBuilderFor( gmlSchema, object, null ) )
      {
        // if already a builder found, new one must replace the old one
        if( result == null )
          result = builder;
        else if( builder.replaces( result ) )
          result = builder;
        else if( !result.replaces( builder ) )
        {
          final String message = String.format( "Concurrent builders for '%s' available: %s - %s ", object ); //$NON-NLS-1$
          System.out.println( "Object to build:\n" + object ); //$NON-NLS-1$
          System.out.println( "1 - " + builder ); //$NON-NLS-1$
          System.out.println( "2 - " + result ); //$NON-NLS-1$
          throw new GMLSchemaException( message );
        }
      }
    }

    Debug.PARSING_VERBOSE.printf( "Found builder:%s%n%n", result ); //$NON-NLS-1$

    return result;
  }

  /**
   * initilize builded objects
   * 
   * @param gmlSchema
   */
  private void rInit( final GMLSchema gmlSchema ) throws GMLSchemaException
  {
    for( final int initRun : IInitialize.INIT_ORDER )
    {
      Debug.PARSING.printf( "Init run: %d%n", initRun ); //$NON-NLS-1$

      final FeatureContentType[] featureContentTypes = gmlSchema.getAllFeatureContentTypes();
      for( final FeatureContentType element : featureContentTypes )
      {
        Debug.PARSING.printf( "Initalising FeatureContentType: %s%n", element ); //$NON-NLS-1$
        element.init( initRun );
      }

      final IFeatureType[] featureTypes = gmlSchema.getAllFeatureTypes();
      for( final IFeatureType element : featureTypes )
      {
        Debug.PARSING.printf( "Initalising FeatureType: %s%n", element ); //$NON-NLS-1$
        element.init( initRun );
      }

      final IPropertyType[] propertyTypes = gmlSchema.getAllPropertyTypes();
      for( final IPropertyType element : propertyTypes )
      {
        Debug.PARSING.printf( "Initalising PropertyType: %s%n", element ); //$NON-NLS-1$
        element.init( initRun );
      }

      final IPropertyContentType[] propertyContentTypes = gmlSchema.getAllPropertyContentTypes();
      for( final IPropertyContentType element : propertyContentTypes )
      {
        Debug.PARSING.printf( "Initalising PropertyContentType: %s%n", element ); //$NON-NLS-1$
        element.init( initRun );
      }

      // TODO: there will be no relationContentType, because they are no more registered
      final RelationContentType[] relationContentTypes = gmlSchema.getAllRelationContentTypes();
      for( final RelationContentType element : relationContentTypes )
      {
        Debug.PARSING.printf( "Initalising RelationContentType: %s%n", element ); //$NON-NLS-1$
        element.init( initRun );
      }

      final IRelationType[] relationTypes = gmlSchema.getAllRelationTypes();
      for( final IRelationType element : relationTypes )
      {
        Debug.PARSING.printf( "Initalising RelationType: %s%n", element ); //$NON-NLS-1$
        element.init( initRun );
      }
    }
  }
}
