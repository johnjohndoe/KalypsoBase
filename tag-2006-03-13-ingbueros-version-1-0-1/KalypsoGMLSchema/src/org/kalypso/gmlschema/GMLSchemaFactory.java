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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.builder.AdvElement2RelationTypeBuilder;
import org.kalypso.gmlschema.builder.ComplexType2FeatureContentTypeBuilder;
import org.kalypso.gmlschema.builder.ComplexType2PropertyContentFromTypeHandlerTypeBuilder;
import org.kalypso.gmlschema.builder.ComplexType2RelationContentTypeBuilder;
import org.kalypso.gmlschema.builder.Element2FeatureTypeBuilder;
import org.kalypso.gmlschema.builder.Element2PropertyTypeBuilder;
import org.kalypso.gmlschema.builder.FeatureContentType2ElementBuilder;
import org.kalypso.gmlschema.builder.FeatureType2ComplexTypeBuilder;
import org.kalypso.gmlschema.builder.GMLSchema2SchemaBasicsBuilder;
import org.kalypso.gmlschema.builder.GMLSchemaBuilder;
import org.kalypso.gmlschema.builder.PropertyType2SimpleTypeBuilder;
import org.kalypso.gmlschema.builder.RelationType2ComplexTypeBuilder;
import org.kalypso.gmlschema.feature.CustomFeatureType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.CustomPropertyContentType;
import org.kalypso.gmlschema.property.CustomValueProperType;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.ITypeHandler;

/**
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public class GMLSchemaFactory
{

  private static List m_register = new ArrayList();

  static
  {
    final GMLSchemaBuilder builder = new GMLSchemaBuilder( "2.1.2" );
    builder.registerBuilder( new GMLSchema2SchemaBasicsBuilder() );
    // test
    builder.registerBuilder( new ComplexType2PropertyContentFromTypeHandlerTypeBuilder() );

    // complex type ...
    builder.registerBuilder( new ComplexType2FeatureContentTypeBuilder() );
    builder.registerBuilder( new FeatureContentType2ElementBuilder() );
    // element ...
    builder.registerBuilder( new Element2FeatureTypeBuilder() );
    builder.registerBuilder( new FeatureType2ComplexTypeBuilder() );
    // element ...
    builder.registerBuilder( new Element2PropertyTypeBuilder() );
    builder.registerBuilder( new PropertyType2SimpleTypeBuilder() );
    // simple type...
//    builder.registerBuilder( new SimpleType2PropertyContentTypeBuilder() );
    // relations
    // m_instance.registerBuilder( new Element2RelationTypeBuilder() );
    builder.registerBuilder( new RelationType2ComplexTypeBuilder() );
    builder.registerBuilder( new ComplexType2RelationContentTypeBuilder() );
    // test
//    builder.registerBuilder( new ComplexType2GeometryPropertyContentTypeBuilder() );
    builder.registerBuilder( new AdvElement2RelationTypeBuilder() );
    m_register.add( builder );
  }

  /**
   * @param schemaLocationURL
   * @throws GMLSchemaException
   */
  public static GMLSchema createGMLSchema( String urn, URL schemaLocationURL ) throws GMLSchemaException
  {
    final URL urlSchemaLocation = GMLSchemaBuilder.getSchemaLocationForURN( urn, schemaLocationURL );
    return createGMLSchema( urlSchemaLocation );
    // return createGMLSchema( schemaLocationURL );
  }

  public static GMLSchema createGMLSchema( URL schemaLocationURL ) throws GMLSchemaException
  {
    try
    {
      // TODO: close the stream!
      final InputStream inputStream = schemaLocationURL.openStream();
      return createGMLSchema( inputStream, schemaLocationURL );
    }
    catch( Exception e )
    {
      throw new GMLSchemaException( "could not parse schema: " + e.getMessage(), e );
    }
  }

  public static GMLSchema createGMLSchema( final InputStream inputStream, final URL context ) throws GMLSchemaException
  {
    final GMLSchemaBuilder builder = getBuilderForVersion( "2.1." );
    return builder.buildGMLSchema( inputStream, context );
  }

  private static GMLSchemaBuilder getBuilderForVersion( String gmlVersion )
  {
    for( Iterator iter = m_register.iterator(); iter.hasNext(); )
    {
      final GMLSchemaBuilder builder = (GMLSchemaBuilder) iter.next();
      if( gmlVersion == null || builder.getGMLVersion().startsWith( gmlVersion ) )
        return builder;
    }
    throw new UnsupportedOperationException( "GML Version " + gmlVersion + " not supported" );
  }

  public static IFeatureType createFeatureType( final QName qName, final IPropertyType[] properties )
  {
    return new CustomFeatureType( qName, properties );
  }

  public static IValuePropertyType createValuePropertyType( final QName name, final QName valueQName, final ITypeHandler typeHandler, final int minOccurs, final int maxOccurs )
  {
    final IPropertyContentType pct = new CustomPropertyContentType( valueQName, typeHandler );
    return new CustomValueProperType( name, pct, minOccurs, maxOccurs );
  }

  public static IRelationType createRelationType( final QName qName, final IFeatureType[] targetFTs, final int minOccurs, final int maxOccurs )
  {
    return new CustomRelationType( qName, targetFTs, minOccurs, maxOccurs );
  }
}
