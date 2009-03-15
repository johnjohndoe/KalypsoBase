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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.NamedGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelElement;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelSimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.ImportDocument.Import;
import org.apache.xmlbeans.impl.xb.xsdschema.IncludeDocument.Include;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.gmlschema.feature.FeatureContentType;
import org.kalypso.gmlschema.feature.FeatureType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.relation.RelationContentType;
import org.kalypso.gmlschema.visitor.IGMLSchemaVisitor;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.ElementWithOccurs;
import org.kalypso.gmlschema.xml.GroupReference;
import org.kalypso.gmlschema.xml.SimpleTypeReference;
import org.kalypso.gmlschema.xml.TypeReference;

/**
 * represents a gml schema
 *
 * @author doemming
 */
public class GMLSchema implements IGMLSchema
{
  private final SchemaDocument m_schemaDocument;

  private final Hashtable<QName, ElementReference> m_refElementHash = new Hashtable<QName, ElementReference>();

  private final Hashtable<QName, GroupReference> m_refGroupHash = new Hashtable<QName, GroupReference>();

  private final Hashtable<QName, SimpleTypeReference> m_refSimpleTypeHash = new Hashtable<QName, SimpleTypeReference>();

  private final Hashtable<QName, ComplexTypeReference> m_refComplexTypeHash = new Hashtable<QName, ComplexTypeReference>();

  private final URL m_context;

  private final UrlResolver m_urlResolver;

  private final Hashtable<String, GMLSchema> m_importedSchemasHash = new Hashtable<String, GMLSchema>();

  // TODO comment
  private final Map<Object, Object> m_buildedObjectHash = new IdentityHashMap<Object, Object>();

  private final List<IPropertyType> m_propertyTypeList = new ArrayList<IPropertyType>();

  private final List<IRelationType> m_relationTypeList = new ArrayList<IRelationType>();

  private final Hashtable<ComplexType, FeatureContentType> m_featureContentTypes = new Hashtable<ComplexType, FeatureContentType>();

  private final Hashtable<SimpleType, IPropertyContentType> m_propertyContentTypes = new Hashtable<SimpleType, IPropertyContentType>();

  private final Hashtable<ComplexType, RelationContentType> m_relationContentTypes = new Hashtable<ComplexType, RelationContentType>();

  private final String m_gmlVersion;

  private final Map<QName, IFeatureType> m_featureTypeMap = new HashMap<QName, IFeatureType>();

  /**
   * we do not support circular imports at the moment, these are allowed from W3C, only circular element definitions are
   * not allowed. Here is a buildin list of schemas that have circular imports and will be ignored at the moment <br>
   * TODO implement support of circular schema imports: suggesting of concept: move time of import processing to after
   * parsing but befoe initilazing ask doemming.
   */
  private final List<SchemaDocument> m_includedSchemas = new ArrayList<SchemaDocument>();

  /**
   * Additional schemas, which are not known as imported schemas. They are loaded implicitly while searching for certain
   * namespaces. This happens probably only, if the gml instance contains some substituting types.
   * <p>
   * We need to remember these schemas, to resolve substituting feature types.
   * </p>
   */
  private final Map<String, GMLSchema> m_additionalSchemas = new HashMap<String, GMLSchema>();

  /** Ignored namespaces: these two schemas import each other, we don't know how to handle this. */
  private final List<String> m_ignoreNameSpaces = new ArrayList<String>();
  {
    m_ignoreNameSpaces.add( "http://www.w3.org/2001/SMIL20/" );
    m_ignoreNameSpaces.add( "http://www.w3.org/2001/SMIL20/Language" );
  }

  /* list of schemas that are referenced by include and have allready been processed */
  private final List<URL> m_processedIncludeUrls = new ArrayList<URL>();

  private final Properties m_i18nProperties;

  public GMLSchema( final SchemaDocument schemaDocument, final URL context, final String gmlVersion, final Properties i18nProperties ) throws GMLSchemaException
  {
    m_gmlVersion = gmlVersion;
    m_schemaDocument = schemaDocument;
    m_context = context;
    m_i18nProperties = i18nProperties;
    m_urlResolver = new UrlResolver();
    init( schemaDocument );
  }

  public String getGMLVersion( )
  {
    return m_gmlVersion;
  }

  public String getVersion( )
  {
    return m_schemaDocument.getSchema().getVersion();
  }

  /**
   * initializes this schema, loads all included and imported schemas
   *
   * @throws GMLSchemaException
   */
  private void init( final SchemaDocument schemaDocument ) throws GMLSchemaException
  {
    try
    {
      final Schema schema = schemaDocument.getSchema();
      // complex types
      final TopLevelComplexType[] complexTypeArray = schema.getComplexTypeArray();
      for( final TopLevelComplexType complexType : complexTypeArray )
      {
        final QName key = new QName( getTargetNamespace(), complexType.getName() );
        final ComplexTypeReference ref = new ComplexTypeReference( this, complexType );
        m_refComplexTypeHash.put( key, ref );
      }
      // groups
      final NamedGroup[] groupArray = schema.getGroupArray();
      for( final NamedGroup group : groupArray )
      {
        final QName key = new QName( getTargetNamespace(), group.getName() );
        final GroupReference reference = new GroupReference( this, group );
        m_refGroupHash.put( key, reference );
      }
      // simple types
      final TopLevelSimpleType[] simpleTypeArray = schema.getSimpleTypeArray();
      for( final TopLevelSimpleType simpleType : simpleTypeArray )
      {
        final QName key = new QName( getTargetNamespace(), simpleType.getName() );
        final SimpleTypeReference reference = new SimpleTypeReference( this, simpleType );
        m_refSimpleTypeHash.put( key, reference );
      }

      // elements
      final TopLevelElement[] elementArray = schema.getElementArray();
      for( final TopLevelElement element : elementArray )
      {
        final QName key = new QName( getTargetNamespace(), element.getName() );
        final ElementReference reference = new ElementReference( this, element );
        m_refElementHash.put( key, reference );
      }
      // import
      final Import[] importArray = schema.getImportArray();
      for( final Import import_ : importArray )
      {
        final String schemaLocation = import_.getSchemaLocation();
        final String namespaceToImport = import_.getNamespace();
        if( !m_ignoreNameSpaces.contains( namespaceToImport ) )
        {
          final URL schemaLocationURL;
          if( schemaLocation == null )
          {
            schemaLocationURL = null;
          }
          else
          {
            // this may throw an MalformedURLException, but this is ok
            // because no schema should contain a malformed url!
            schemaLocationURL = m_urlResolver.resolveURL( m_context, schemaLocation );
          }

          final GMLSchemaCatalog schemaCatalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();
          GMLSchema gmlSchema = schemaCatalog.getSchema( namespaceToImport, getGMLVersion(), schemaLocationURL );
          if( gmlSchema == null )
            gmlSchema = GMLSchemaFactory.createGMLSchema( getGMLVersion(), schemaLocationURL );

          if( gmlSchema != null )
            m_importedSchemasHash.put( gmlSchema.getTargetNamespace(), gmlSchema );
          else
            throw new GMLSchemaException( "Could not import schema: " + namespaceToImport + " with schemalocation: " + schemaLocation );
        }
      }

      // include
      final Include[] includeArray = schema.getIncludeArray();

      for( final Include include : includeArray )
      {
        final URL includeURL = m_urlResolver.resolveURL( m_context, include.getSchemaLocation() );
        if( !m_processedIncludeUrls.contains( includeURL ) )
        {
          final SchemaDocument includedSchemaDocument = SchemaDocument.Factory.parse( includeURL );
          m_processedIncludeUrls.add( includeURL );
          m_includedSchemas.add( includedSchemaDocument );
          init( includedSchemaDocument );
        }
      }
    }
    catch( Throwable e )
    {
      /* unpack exception if inside InvocationTargetException */
      if( e instanceof InvocationTargetException )
      {
        e = ((InvocationTargetException) e).getTargetException();
      }

      if( e instanceof GMLSchemaException )
        throw (GMLSchemaException) e;

      throw new GMLSchemaException( e );
    }
  }

  public ElementReference resolveElementReference( final QName qName ) throws GMLSchemaException
  {
    final String namespaceURI = qName.getNamespaceURI();
    if( getTargetNamespace().equals( namespaceURI ) )
      return m_refElementHash.get( qName );
    final GMLSchema gmlschema = getGMLSchemaForNamespaceURI( namespaceURI );
    // beware of recursion
    if( gmlschema == null || gmlschema == this )
      // TODO: move to caller
// if( Debug.traceSchemaParsing() )
// {
// final IStatus status = StatusUtilities.createErrorStatus( "Could not resolve element reference to " + qName );
// System.out.println( status.getMessage() );
// KalypsoGMLSchemaPlugin.getDefault().getLog().log( status );
// }
      return null;
    return gmlschema.resolveElementReference( qName );
  }

  public GroupReference resolveGroupReference( final QName qName ) throws GMLSchemaException
  {
    final String namespaceURI = qName.getNamespaceURI();
    if( getTargetNamespace().equals( namespaceURI ) )
      return m_refGroupHash.get( qName );
    final GMLSchema gmlschema = getGMLSchemaForNamespaceURI( namespaceURI );
    // beware of recursion
    if( gmlschema == null || gmlschema == this )
    {
      final IStatus status = StatusUtilities.createErrorStatus( "could not resolve group reference to " + qName );
      KalypsoGMLSchemaPlugin.getDefault().getLog().log( status );
      return null;
    }
    return gmlschema.resolveGroupReference( qName );
  }

  public SimpleTypeReference resolveSimpleTypeReference( final QName qName ) throws GMLSchemaException
  {
    final String namespaceURI = qName.getNamespaceURI();
    if( getTargetNamespace().equals( namespaceURI ) )
      return m_refSimpleTypeHash.get( qName );
    final GMLSchema gmlschema = getGMLSchemaForNamespaceURI( namespaceURI );
    // beware of recursion
    if( gmlschema == null || gmlschema == this )
      return null;
    return gmlschema.resolveSimpleTypeReference( qName );
  }

  public ComplexTypeReference resolveComplexTypeReference( final QName qName ) throws GMLSchemaException
  {
    final String namespaceURI = qName.getNamespaceURI();
    if( getTargetNamespace().equals( namespaceURI ) )
      return m_refComplexTypeHash.get( qName );
    final GMLSchema gmlschema = getGMLSchemaForNamespaceURI( namespaceURI );
    if( gmlschema != null )
      return gmlschema.resolveComplexTypeReference( qName );

    return null;
  }

  /**
   * @param qName
   */
  public TypeReference resolveTypeReference( final QName qName ) throws GMLSchemaException
  {
    final SimpleTypeReference reference = resolveSimpleTypeReference( qName );
    if( reference != null )
      return reference;
    return resolveComplexTypeReference( qName );
  }

  /** public in order to allow force dependency from outside. */
  public GMLSchema getGMLSchemaForNamespaceURI( final String namespaceURI ) throws GMLSchemaException
  {
    /* The XML-Schema will never be loaded. */
    if( NS.XSD_SCHEMA.equals( namespaceURI ) )
      return null;

    if( m_ignoreNameSpaces.contains( namespaceURI ) )
      return null;

    // TODO make a nice visitor
    // first level ?
    if( namespaceURI.equals( getTargetNamespace() ) )
      return this;

    if( m_importedSchemasHash.containsKey( namespaceURI ) )
      return m_importedSchemasHash.get( namespaceURI );

    if( m_additionalSchemas.containsKey( namespaceURI ) )
      return m_additionalSchemas.get( namespaceURI );

    // other level....
    for( final GMLSchema gmlSchema : m_importedSchemasHash.values() )
    {
      final GMLSchema schemaForNamespaceURI = gmlSchema.getGMLSchemaForNamespaceURI( namespaceURI );
      if( schemaForNamespaceURI != null )
      {
        // also remember to schema here, used as shortcut for later search
        m_additionalSchemas.put( namespaceURI, schemaForNamespaceURI );
        return schemaForNamespaceURI;
      }
    }

    // if schema is not implemented we have to ask GMLSchemaCache, maybe it is a well known schema like wfs.xsd
    try
    {
      // load all imported schema with same gml version
      final GMLSchemaCatalog schemaCatalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();
      final GMLSchema schema = schemaCatalog.getSchema( namespaceURI, getGMLVersion() );
      if( schema != null )
      {
        m_additionalSchemas.put( namespaceURI, schema );
      }

      // REAMRK: we load schemas here, which are not known as imported schemes
      // They are probably used by the gml instance via substitution
      // We need to remember those schemes for later (e.g. for substitution resolution)
      return schema;
    }
    catch( final InvocationTargetException e )
    {
      throw new GMLSchemaException( "Unable to load schema: " + namespaceURI, e.getTargetException() );
    }
  }

  public String getTargetNamespace( )
  {
    return m_schemaDocument.getSchema().getTargetNamespace();
  }

  public SchemaDocument getSchema( )
  {
    return m_schemaDocument;
  }

  /**
   * @see org.kalypso.gmlschema.IGMLSchema#getContext()
   */
  public URL getContext( )
  {
    return m_context;
  }

  /**
   * @param buildedObject
   */
  public void register( final Object xmlObject, final Object buildedObject )
  {
    if( buildedObject == null )
      throw new UnsupportedOperationException( "can not register null-object" );

    m_buildedObjectHash.put( xmlObject, buildedObject );

    if( xmlObject instanceof ElementWithOccurs )
      throw new UnsupportedOperationException();

    // use QName as key:
    if( buildedObject instanceof FeatureType )
    {
      final FeatureType ft = (FeatureType) buildedObject;
      // m_featureTypesList.add( ft );
      m_featureTypeMap.put( ft.getQName(), ft );
    }
    else if( buildedObject instanceof IRelationType )
    {
      final IRelationType rt = (IRelationType) buildedObject;
      m_relationTypeList.add( rt );
    }
    else if( buildedObject instanceof IPropertyType )
    {
      final IPropertyType pt = (IPropertyType) buildedObject;
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
  }

  /**
   * @param element
   */
  public boolean hasBuildedObjectFor( final Object element )
  {
    return m_buildedObjectHash.containsKey( element );
  }

  /**
   * @param element
   */
  public Object getBuildedObjectFor( final Element element )
  {
    return m_buildedObjectHash.get( element );
  }

  /**
   * @param simpleType
   */
  public Object getBuildedObjectFor( final SimpleType simpleType )
  {
    return m_buildedObjectHash.get( simpleType );
  }

  /**
   * @param complexType
   */
  public Object getBuildedObjectFor( final ComplexType complexType )
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
  public RelationContentType getRelationContentTypeFor( final ComplexType complexType )
  {
    return m_relationContentTypes.get( complexType );
  }

  /**
   * @param simpleType
   */
  public IPropertyContentType getFeaturePropertyContentTypeFor( final SimpleType simpleType )
  {
    return m_propertyContentTypes.get( simpleType );
  }

  public IFeatureType[] getAllFeatureTypes( )
  {
    // return m_featureTypesList.toArray( new IFeatureType[m_featureTypesList.size()] );
    return m_featureTypeMap.values().toArray( new IFeatureType[m_featureTypeMap.size()] );
  }

  public IPropertyType[] getAllPropertyTypes( )
  {
    return m_propertyTypeList.toArray( new IPropertyType[m_propertyTypeList.size()] );
  }

  public FeatureContentType[] getAllFeatureContentTypes( )
  {
    final Collection<FeatureContentType> collection = m_featureContentTypes.values();
    return collection.toArray( new FeatureContentType[collection.size()] );
  }

  public IPropertyContentType[] getAllPropertyContentTypes( )
  {
    final Collection<IPropertyContentType> collection = m_propertyContentTypes.values();
    return collection.toArray( new IPropertyContentType[collection.size()] );
  }

  public RelationContentType[] getAllRelationContentTypes( )
  {
    final Collection<RelationContentType> collection = m_relationContentTypes.values();
    return collection.toArray( new RelationContentType[collection.size()] );
  }

  public IRelationType[] getAllRelationTypes( )
  {
    return m_relationTypeList.toArray( new IRelationType[m_relationTypeList.size()] );
  }

  /**
   * @param visitor
   */
  public void accept( final IGMLSchemaVisitor visitor )
  {
    innerAccept( visitor, new ArrayList<GMLSchema>() );
  }

  /**
   * @param schemasToIgnore
   *            list of schemas not to visit
   * @param visitor
   */
  private void innerAccept( final IGMLSchemaVisitor visitor, final List<GMLSchema> schemasToIgnore )
  {
    if( visitor.visit( this ) && !schemasToIgnore.contains( this ) )
    {
      schemasToIgnore.add( this );
      for( final GMLSchema schema : getImports() )
      {
        schema.innerAccept( visitor, schemasToIgnore );
      }
      for( final GMLSchema schema : getAdditionalSchemas() )
      {
        schema.innerAccept( visitor, schemasToIgnore );
      }
    }
  }

  public GMLSchema[] getAdditionalSchemas( )
  {
    return m_additionalSchemas.values().toArray( new GMLSchema[m_additionalSchemas.values().size()] );
  }

  public GMLSchema[] getImports( )
  {
    final Collection<GMLSchema> collection = m_importedSchemasHash.values();
    return collection.toArray( new GMLSchema[collection.size()] );
  }

  public IFeatureType getFeatureType( final QName qName )
  {
    final String namespaceURI = qName.getNamespaceURI();
    if( namespaceURI == null || "".equals( namespaceURI ) || namespaceURI.equals( getTargetNamespace() ) )
    {
      final IFeatureType ft = m_featureTypeMap.get( qName );
      if( ft != null )
        return ft;
      // test for unqualified name
      for( final QName ftQName : m_featureTypeMap.keySet() )
      {
        if( qName.getLocalPart().equals( ftQName.getLocalPart() ) )
          return m_featureTypeMap.get( ftQName );
      }
      return null;
    }
    else
    {
      final IGMLSchema schemaForNamespaceURI;
      try
      {
        schemaForNamespaceURI = getGMLSchemaForNamespaceURI( namespaceURI );
        // beware for recursion
        // if we get this, don't search again
        if( schemaForNamespaceURI == this )
          return null;

      }
      catch( final GMLSchemaException e )
      {
        e.printStackTrace();

        // odl behaviour, but it should not happen anyway? because the schema should all
        // be loaded by now
        return null;
      }

      if( schemaForNamespaceURI == null )
        return null;

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

  public SchemaDocument[] getIncludedSchemas( )
  {
    return m_includedSchemas.toArray( new SchemaDocument[m_includedSchemas.size()] );
  }

  public void addAdditionalSchema( final GMLSchema schema )
  {
    Assert.isNotNull( schema );

    m_additionalSchemas.put( schema.getTargetNamespace(), schema );
  }

  /**
   * Returns the properties that serve to provide language specific labels/tooltips for this schema.<br>
   * Not in interface, as this is only used during the creation of feature/property-types.<br>
   * Not intended to be used outside this framework.
   */
  public Properties getI18nProperties( )
  {
    return m_i18nProperties;
  }
}
