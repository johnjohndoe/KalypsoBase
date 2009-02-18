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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.ImportDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelElement;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelSimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.ImportDocument.Import;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.kalypso.commons.java.net.UrlResolver;
import org.kalypso.gmlschema.feature.FeatureContentType;
import org.kalypso.gmlschema.feature.FeatureType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.PropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.relation.RelationContentType;
import org.kalypso.gmlschema.visitor.IGMLSchemaVisitor;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.Reference;
import org.kalypso.gmlschema.xml.SimpleTypeReference;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * represents a gml schema
 * 
 * @author doemming
 */
public class GMLSchema
{
  private final Schema m_schema;

  private final Hashtable<QName, Reference> m_refHash = new Hashtable<QName, Reference>();

  private final URL m_context;

  private final UrlResolver m_urlResolver;

  private final Hashtable<String, GMLSchema> m_importedSchemasHash = new Hashtable<String, GMLSchema>();

  private final Hashtable<Object, Object> m_buildedObjectHash = new Hashtable<Object, Object>();

  private final List<FeatureType> m_featureTypesList = new ArrayList<FeatureType>();

  private final List<PropertyType> m_propertyTypeList = new ArrayList<PropertyType>();

  private final List<IRelationType> m_relationTypeList = new ArrayList<IRelationType>();

  private final Hashtable<ComplexType, FeatureContentType> m_featureContentTypes = new Hashtable<ComplexType, FeatureContentType>();

  private final Hashtable<SimpleType, IPropertyContentType> m_propertyContentTypes = new Hashtable<SimpleType, IPropertyContentType>();

  private final Hashtable<ComplexType, RelationContentType> m_relationContentTypes = new Hashtable<ComplexType, RelationContentType>();

  private final String m_gmlVersion;

  private HashMap<QName, IFeatureType> m_featuretypeMap = null;

  public GMLSchema( final Schema schema, final URL context, final String gmlVersion ) throws GMLSchemaException
  {
    m_gmlVersion = gmlVersion;
    m_schema = schema;
    m_context = context;
    m_urlResolver = new UrlResolver();
    init();
  }

  public String getGMLVersion( )
  {
    return m_gmlVersion;
  }

  public String getVersion( )
  {
    return m_schema.getVersion();
  }

  /**
   * initialize gmlschema, create all featuretypes, featurepropertytypes, parse all included and imported schemas
   * 
   * @throws GMLSchemaException
   */
  private void init( ) throws GMLSchemaException
  {
    final TopLevelComplexType[] complexTypeArray = m_schema.getComplexTypeArray();
    for( int i = 0; i < complexTypeArray.length; i++ )
    {
      final TopLevelComplexType complexType = complexTypeArray[i];

      final QName key = new QName( getTargetNamespace(), complexType.getName() );
      final ComplexTypeReference ref = new ComplexTypeReference( this, complexType );
      m_refHash.put( key, ref );
    }
    // simple types
    final TopLevelSimpleType[] simpleTypeArray = m_schema.getSimpleTypeArray();
    for( int i = 0; i < simpleTypeArray.length; i++ )
    {
      final TopLevelSimpleType simpleType = simpleTypeArray[i];

      final QName key = new QName( getTargetNamespace(), simpleType.getName() );
      final SimpleTypeReference reference = new SimpleTypeReference( this, simpleType );
      m_refHash.put( key, reference );
    }

    // elements
    final TopLevelElement[] elementArray = m_schema.getElementArray();
    for( int i = 0; i < elementArray.length; i++ )
    {
      final TopLevelElement element = elementArray[i];
      final QName key = new QName( getTargetNamespace(), element.getName() );
      final ElementReference reference = new ElementReference( this, element );
      m_refHash.put( key, reference );
    }
    // import
    final Import[] importArray = m_schema.getImportArray();
    for( int i = 0; i < importArray.length; i++ )
    {
      final ImportDocument.Import import_ = importArray[i];
      String schemaLocation = import_.getSchemaLocation();
      GMLSchema gmlSchema = GMLSchemaCatalog.getSchema( import_.getNamespace() );
      if( gmlSchema == null )
      {
        final URL url;
        try
        {
          url = m_urlResolver.resolveURL( m_context, schemaLocation );
        }
        catch( MalformedURLException e )
        {
          if( schemaLocation != null )
            throw new GMLSchemaException( "could not import schema from " + schemaLocation, e );
          throw new GMLSchemaException( "could not import schema, no schemaLocation", e );
        }
        gmlSchema = GMLSchemaCatalog.getSchema( url );
      }
      m_importedSchemasHash.put( gmlSchema.getTargetNamespace(), gmlSchema );
    }

    // include
    // TODO merge arrays with the ones from m_schema
    // final Include[] includeArray = m_schema.getIncludeArray();
    // final GMLSchema[] includedSchemas = new
    // GMLSchema[includeArray.length];
    //   
    // for( int i = 0; i < includeArray.length; i++ )
    // {
    // final IncludeDocument.Include include = includeArray[i];
    // final URL includeURL = m_urlResolver.resolveURL( m_context,
    // include.getSchemaLocation() );
    // m_logger.info( "\nINCLUDE: \n" + toString() + "\n includes " +
    // includeURL );
    // includedSchemas[i] = GMLSchemaBuilder.generateGMLSchema(
    // getTargetNamespace(), includeURL );
    // }
    // generate elements

  }

  /**
   * @param qName
   */
  public Reference resolveReference( final QName qName )
  {
    final String namespaceURI = qName.getNamespaceURI();
    if( getTargetNamespace().equals( namespaceURI ) )
      return m_refHash.get( qName );
    final GMLSchema gmlschema = getGMLSchemaForNamespaceURI( namespaceURI );
    return gmlschema.resolveReference( qName );
  }

  /**
   * @param namespaceURI
   * @return gmlschema
   */
  private GMLSchema getGMLSchemaForNamespaceURI( String namespaceURI )
  {
    // TODO make a nice visitor
    // first level ?
    if( namespaceURI.equals( getTargetNamespace() ) )
      return this;
    if( m_importedSchemasHash.containsKey( namespaceURI ) )
      return m_importedSchemasHash.get( namespaceURI );
    // other level....

    final Collection collection = m_importedSchemasHash.values();
    for( Iterator iter = collection.iterator(); iter.hasNext(); )
    {
      final GMLSchema gmlSchema = (GMLSchema) iter.next();
      final GMLSchema schemaForNamespaceURI = gmlSchema.getGMLSchemaForNamespaceURI( namespaceURI );
      if( schemaForNamespaceURI != null )
        return schemaForNamespaceURI;
    }
    return null;
  }

  public String getTargetNamespace( )
  {
    return m_schema.getTargetNamespace();
  }

  public Schema getSchema( )
  {
    return m_schema;
  }

  /**
   * @param buildedObject
   */
  public void register( final Object xmlObject, final Object buildedObject )
  {
    if( buildedObject == null )
      throw new UnsupportedOperationException( "can not register null-object" );
    m_buildedObjectHash.put( xmlObject, buildedObject );

    // use QName as key:
    if( buildedObject instanceof FeatureType )
    {
      final FeatureType ft = (FeatureType) buildedObject;
      m_featureTypesList.add( ft );
    }
    else if( buildedObject instanceof IRelationType )
    {
      final IRelationType rt = (IRelationType) buildedObject;
      m_relationTypeList.add( rt );
    }
    else if( buildedObject instanceof PropertyType )
    {
      final PropertyType pt = (PropertyType) buildedObject;
      m_propertyTypeList.add( pt );
    }
    // use type as key
    else if( buildedObject instanceof FeatureContentType )
    {
      final FeatureContentType fct = (FeatureContentType) buildedObject;
      final ComplexType complexType = fct.getComplexType();
      m_featureContentTypes.put( complexType, fct );
    }
    else if( buildedObject instanceof RelationContentType )
    {
      final RelationContentType rct = (RelationContentType) buildedObject;
      final ComplexType complexType = rct.getComplexType();
      m_relationContentTypes.put( complexType, rct );
    }
    // else if( buildedObject instanceof IPropertyContentType )
    // {
    // final IPropertyContentType pct = (IPropertyContentType) buildedObject;
    // final SimpleType simpleType = pct.getSimpleType();
    // m_propertyContentTypes.put( simpleType, pct );
    // }
  }

  /**
   * @param element
   */
  public boolean hasBuildedObjectFor( Object element )
  {
    return m_buildedObjectHash.containsKey( element );
  }

  /**
   * @param element
   */
  public Object getBuildedObjectFor( Element element )
  {
    return m_buildedObjectHash.get( element );
  }

  /**
   * @param simpleType
   */
  public Object getBuildedObjectFor( SimpleType simpleType )
  {
    return m_buildedObjectHash.get( simpleType );
  }

  /**
   * @param complexType
   */
  public Object getBuildedObjectFor( ComplexType complexType )
  {
    return m_buildedObjectHash.get( complexType );
  }

  public FeatureContentType getFeatureContentTypeFor( final ComplexType complexType )
  {
    return m_featureContentTypes.get( complexType );
  }

  /**
   * @param complexType
   */
  public RelationContentType getRelationContentTypeFor( ComplexType complexType )
  {
    return m_relationContentTypes.get( complexType );
  }

  /**
   * @param simpleType
   */
  public IPropertyContentType getFeaturePropertyContentTypeFor( SimpleType simpleType )
  {
    return m_propertyContentTypes.get( simpleType );
  }

  /**
   * 
   */
  public IFeatureType[] getAllFeatureTypes( )
  {

    return m_featureTypesList.toArray( new IFeatureType[m_featureTypesList.size()] );
  }

  /**
   * 
   */
  public PropertyType[] getAllPropertyTypes( )
  {
    return m_propertyTypeList.toArray( new PropertyType[m_propertyTypeList.size()] );
  }

  /**
   * 
   */
  public FeatureContentType[] getAllFeatureContentTypes( )
  {
    final Collection<FeatureContentType> collection = m_featureContentTypes.values();
    return collection.toArray( new FeatureContentType[collection.size()] );
  }

  /**
   * 
   */
  public IPropertyContentType[] getAllPropertyContentTypes( )
  {
    final Collection<IPropertyContentType> collection = m_propertyContentTypes.values();
    return collection.toArray( new IPropertyContentType[collection.size()] );
  }

  /**
   */
  public RelationContentType[] getAllRelationContentTypes( )
  {
    final Collection<RelationContentType> collection = m_relationContentTypes.values();
    return collection.toArray( new RelationContentType[collection.size()] );
  }

  /**
   * 
   */
  public IRelationType[] getAllRelationTypes( )
  {
    return m_relationTypeList.toArray( new IRelationType[m_relationTypeList.size()] );
  }

  /**
   * @param visitor
   */
  public void accept( final IGMLSchemaVisitor visitor )
  {
    if( visitor.visit( this ) )
    {
      final GMLSchema[] imports = getImports();
      for( int i = 0; i < imports.length; i++ )
      {
        final GMLSchema schema = imports[i];
        schema.accept( visitor );
      }
    }
  }

  /**
   * @return
   */
  private GMLSchema[] getImports( )
  {
    final Collection<GMLSchema> collection = m_importedSchemasHash.values();
    return collection.toArray( new GMLSchema[collection.size()] );
  }

  public Map getNamespaceMap( )
  {
    throw new UnsupportedOperationException();
  }

  public Document getXMLDocument( )
  {
    final Node domNode = m_schema.getDomNode();
    return domNode.getOwnerDocument();
    // return (Document) domNode;
  }

  public IFeatureType getFeatureType( final QName qName )
  {
    if( m_featuretypeMap == null )
    {
      // TODO move this to a init methode
      m_featuretypeMap = new HashMap<QName, IFeatureType>();
      final IFeatureType[] allFeatureTypes = getAllFeatureTypes();
      for( int i = 0; i < allFeatureTypes.length; i++ )
      {
        final IFeatureType ft = allFeatureTypes[i];
        m_featuretypeMap.put( ft.getQName(), ft );
      }
    }
    final String namespaceURI = qName.getNamespaceURI();
    if( namespaceURI == null || namespaceURI.equals( getTargetNamespace() ) )
      return m_featuretypeMap.get( qName );
    else
    {
      final GMLSchema schemaForNamespaceURI = getGMLSchemaForNamespaceURI( namespaceURI );
      return schemaForNamespaceURI.getFeatureType( qName );
    }
  }

  /**
   * @deprecated use getFeatureType(QName qName)
   */
  @Deprecated
  public IFeatureType getFeatureType( final String nameLocalPart )
  {
    final QName name = new QName( getTargetNamespace(), nameLocalPart );
    return getFeatureType( name );
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return "XML-Schema: " + getTargetNamespace();
  }
}
