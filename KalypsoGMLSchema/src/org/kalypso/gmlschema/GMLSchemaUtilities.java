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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.common.JarHelper;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.apache.xmlbeans.impl.xb.xsdschema.All;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexRestrictionType;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.ExtensionType;
import org.apache.xmlbeans.impl.xb.xsdschema.Group;
import org.apache.xmlbeans.impl.xb.xsdschema.GroupRef;
import org.apache.xmlbeans.impl.xb.xsdschema.LocalComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.LocalElement;
import org.apache.xmlbeans.impl.xb.xsdschema.LocalSimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.NamedGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleExtensionType;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleRestrictionType;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.AnnotationDocument.Annotation;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexContentDocument.ComplexContent;
import org.apache.xmlbeans.impl.xb.xsdschema.DocumentationDocument.Documentation;
import org.apache.xmlbeans.impl.xb.xsdschema.ImportDocument.Import;
import org.apache.xmlbeans.impl.xb.xsdschema.IncludeDocument.Include;
import org.apache.xmlbeans.impl.xb.xsdschema.RestrictionDocument.Restriction;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleContentDocument.SimpleContent;
import org.apache.xmlbeans.impl.xb.xsdschema.UnionDocument.Union;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.contribs.java.net.UrlUtilities;
import org.kalypso.contribs.javax.xml.namespace.ListQName;
import org.kalypso.contribs.javax.xml.namespace.MixedQName;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.visitor.FindSubstitutesGMLSchemaVisitor;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.GroupReference;
import org.kalypso.gmlschema.xml.SimpleTypeReference;
import org.kalypso.gmlschema.xml.TypeReference;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Utilities around GMLSchema-processing
 * 
 * @author doemming
 */
public class GMLSchemaUtilities
{
  public static final String BASE_SCHEMA_IN_JAR = "base.xsd";

  private static final int CONSTRUCTION_REFERENCED_TYPE = 1;

  private static final int CONSTRUCTION_NAMED_TYPE = 2;

  private static final int CONSTRUCTION_ANONYMOUS_SIMPLE_TYPE = 3;

  private static final int CONSTRUCTION_ANONYMOUS_COMPLEX_TYPE = 4;

  private static final int CONSTRUCTION_ABSTRACT_ANYTYPE = 5;

  private static final int CONSTRUCTION_ANYTYPE = 6;

  public static final boolean OCCURS_MIN = false;

  public static final boolean OCCURS_MAX = true;

  private static UrlUtilities m_urlUtitilies = new UrlUtilities();

  /**
   * @param substitueeName
   *          Name of the type which may or may not be substituted by type
   */
  public static boolean substitutes( final IFeatureType type, final QName substitueeName )
  {
    // everyone is substituting himself
    if( type.getQName().equals( substitueeName ) )
      return true;

    final IFeatureType substitutionGroupFT = type.getSubstitutionGroupFT();
    if( substitutionGroupFT == null )
      return false;

    return substitutes( substitutionGroupFT, substitueeName );
  }

  /**
   * @param gmlSchema
   * @param complexType
   */
  public static QName findBaseType( final GMLSchema gmlSchema, ComplexType complexType, String gmlVersion ) throws GMLSchemaException
  {
    final ExplicitGroup sequence = complexType.getSequence();
    final String name = complexType.getName();

    // 1. check if it is a named type
    // propably a typehanlder exists for this
    if( name != null )
    {
      final QName typeQName = new QName( gmlSchema.getTargetNamespace(), name );
      if( isKnownType( typeQName, gmlVersion ) )
        return getKnownTypeFor( typeQName, gmlVersion );
    }

    if( sequence != null )
    {
      return null;
    }
    // 2. check for base (extension or restriction)
    final QName base;
    final SimpleContent simpleContent = complexType.getSimpleContent();
    final ComplexContent complexContent = complexType.getComplexContent();
    if( simpleContent != null )
    {
      final SimpleExtensionType extension = simpleContent.getExtension();
      final SimpleRestrictionType restriction = simpleContent.getRestriction();
      if( extension != null )
        base = extension.getBase();
      else if( restriction != null )
        base = restriction.getBase();
      else
        throw new UnsupportedOperationException( "unknown base type for " + simpleContent.toString() );
    }
    else if( complexContent != null )
    {

      final ExtensionType extension = complexContent.getExtension();
      final ComplexRestrictionType restriction = complexContent.getRestriction();
      if( extension != null )
        base = extension.getBase();
      else if( restriction != null )
        base = restriction.getBase();
      else
        throw new UnsupportedOperationException( "unknown base type for " + complexContent.toString() );
      if( isXDSAnyType( base ) )
        return null;
    }
    else
    {
      if( KalypsoGmlSchemaTracing.traceSchemaParsing() )
        System.out.println( "unknown type:" + complexType.getName() );
      return null;
      // throw new UnsupportedOperationException( "unknown base type for " + complexType.toString() );
    }

    // propably a typehandler exists for this
    if( isKnownType( base, gmlVersion ) )
      return getKnownTypeFor( base, gmlVersion );

    // 3. handle a reference
    final TypeReference reference = gmlSchema.resolveTypeReference( base );
    if( reference == null )
    {
      if( KalypsoGmlSchemaTracing.traceSchemaParsing() )
        System.out.println( "unknown name: " + base );
      return null;
    }

    return findBaseType( reference, gmlVersion );
  }

  private static boolean isXDSAnyType( QName base )
  {
    if( !base.getNamespaceURI().equals( NS.XSD_SCHEMA ) )
      return false;
    return base.getLocalPart().equals( "anyType" );
  }

  /**
   * @param schema
   * @param simpleType
   */
  public static QName findBaseType( GMLSchema schema, SimpleType simpleType, String gmlVersion ) throws GMLSchemaException
  {
    // 1. check if it is a named type
    // propably a typehanlder exists for this
    final String name = simpleType.getName();
    if( name != null )
    {
      final QName namedBase = new QName( schema.getTargetNamespace(), name );
      if( isKnownType( namedBase, gmlVersion ) )
        return getKnownTypeFor( namedBase, gmlVersion );
    }

    // 2. check for base
    // propably a typehanlder exists for this
    final Restriction restriction = simpleType.getRestriction();
    if( restriction != null )
      return findBaseType( schema, restriction, gmlVersion );

    final Union union = simpleType.getUnion();
    if( union != null )
      return findBaseType( schema, union, gmlVersion );
    final org.apache.xmlbeans.impl.xb.xsdschema.ListDocument.List list = simpleType.getList();
    if( list != null )
      return findBaseType( schema, list, gmlVersion );
    // should never happen !
    throw new UnsupportedOperationException();
  }

  private static QName findBaseType( GMLSchema schema, org.apache.xmlbeans.impl.xb.xsdschema.ListDocument.List list, String gmlVersion ) throws GMLSchemaException
  {
    final LocalSimpleType simpleType = list.getSimpleType();
    final QName itemType = list.getItemType();

    QName base = null;
    if( simpleType != null )
      base = findBaseType( schema, simpleType, gmlVersion );
    else if( itemType != null )
    {
      if( isKnownType( itemType, gmlVersion ) )
        base = getKnownTypeFor( itemType, gmlVersion );
      else
      {
        final SimpleTypeReference reference = schema.resolveSimpleTypeReference( itemType );
        base = findBaseType( reference, gmlVersion );
      }
    }
    if( base == null )
      return null;
    return new ListQName( base );
  }

  private static QName findBaseType( GMLSchema schema, Union union, String gmlVersion ) throws GMLSchemaException
  {
    final Set<QName> mixedSet = new HashSet<QName>();
    // embeddded simple types
    final LocalSimpleType[] simpleTypeArray = union.getSimpleTypeArray();
    if( simpleTypeArray.length > 0 )
    {
      for( int i = 0; i < simpleTypeArray.length; i++ )
      {
        final QName base = findBaseType( schema, simpleTypeArray[i], gmlVersion );
        if( base != null )
          mixedSet.add( base );
      }
    }
    // attribute membertypes
    final List<QName> memberTypes = union.getMemberTypes();
    if( memberTypes != null )
    {
      for( Iterator<QName> iter = memberTypes.iterator(); iter.hasNext(); )
      {
        final QName typeQName = iter.next();
        if( isKnownType( typeQName, gmlVersion ) )
          mixedSet.add( getKnownTypeFor( typeQName, gmlVersion ) );
        else
        {
          final SimpleTypeReference reference = schema.resolveSimpleTypeReference( typeQName );
          mixedSet.add( findBaseType( reference, gmlVersion ) );
        }
      }
    }
    final QName[] bases = mixedSet.toArray( new QName[mixedSet.size()] );
    if( bases.length == 0 )
      return null;
    else if( bases.length == 1 )
      return bases[0];
    else
      return new MixedQName( bases );
  }

  private static QName findBaseType( GMLSchema schema, Restriction restriction, String gmlVersion ) throws GMLSchemaException
  {
    final QName base = restriction.getBase();
    if( isKnownType( base, gmlVersion ) )
      return getKnownTypeFor( base, gmlVersion );

    // 3. handle a reference
    final TypeReference reference = schema.resolveTypeReference( base );
    // final ElementReference reference = (ElementReference) resolveReference;
    return findBaseType( reference, gmlVersion );

  }

  public static QName findBaseType( final GMLSchema schema, final Element element, String gmlVersion ) throws GMLSchemaException
  {
    final QName qName;
    final int constructionType = getConstructionType( element );
    switch( constructionType )
    {
      case CONSTRUCTION_ANONYMOUS_SIMPLE_TYPE:
        final LocalSimpleType simpleType = element.getSimpleType();
        return findBaseType( schema, simpleType, gmlVersion );
      case CONSTRUCTION_ANONYMOUS_COMPLEX_TYPE:
        final LocalComplexType complexType = element.getComplexType();
        return findBaseType( schema, complexType, gmlVersion );
      case CONSTRUCTION_REFERENCED_TYPE:
        qName = element.getRef();
        break;
      case CONSTRUCTION_NAMED_TYPE:
        qName = element.getType();
        break;
      case CONSTRUCTION_ABSTRACT_ANYTYPE:
      case CONSTRUCTION_ANYTYPE:
        return new QName( NS.XSD_SCHEMA, "anyType" ); // no type
      default:
        throw new UnsupportedOperationException();
    }

    if( isKnownType( qName, gmlVersion ) )
      return getKnownTypeFor( qName, gmlVersion );

    switch( constructionType )
    {
      case CONSTRUCTION_REFERENCED_TYPE:
        final ElementReference elementReference = schema.resolveElementReference( qName );
        if( elementReference == null )
        {
          if( KalypsoGmlSchemaTracing.traceSchemaParsing() )
            System.out.println( "could not resolve element reference to " + qName );
          return null;
        }
        return findBaseType( elementReference, gmlVersion );
      case CONSTRUCTION_NAMED_TYPE:
        final TypeReference typeReference = schema.resolveTypeReference( qName );
        if( typeReference == null )
          throw new GMLSchemaException( "Could not find type reference for: " + qName );
        return findBaseType( typeReference, gmlVersion );
      default:
        throw new GMLSchemaException( "Unknown base type for " + element.toString() );
    }
  }

  /**
   * @param element
   * @return constructiontyped integer
   */
  private static int getConstructionType( Element element )
  {
    final QName ref = element.getRef();
    if( ref != null )
      return CONSTRUCTION_REFERENCED_TYPE;
    final String name = element.getName();
    final QName type = element.getType();
    if( name != null && type != null )
      return CONSTRUCTION_NAMED_TYPE;
    final LocalSimpleType simpleType = element.getSimpleType();
    if( name != null && simpleType != null )
      return CONSTRUCTION_ANONYMOUS_SIMPLE_TYPE;
    final LocalComplexType complexType = element.getComplexType();
    if( name != null && complexType != null )
      return CONSTRUCTION_ANONYMOUS_COMPLEX_TYPE;
    // this element constructs nothing, perhaps it is an abstract head f a substitutiongroup
    if( element.isSetAbstract() )
      return CONSTRUCTION_ABSTRACT_ANYTYPE;
    return CONSTRUCTION_ANYTYPE;
    // throw new UnsupportedOperationException( "unknown construction type:\n" + element.toString() );
  }

  /**
   * @param reference
   */
  private static QName findBaseType( SimpleTypeReference reference, String gmlVersion ) throws GMLSchemaException
  {
    return findBaseType( reference.getGMLSchema(), reference.getSimpleType(), gmlVersion );
  }

  /**
   * @param reference
   */
  private static QName findBaseType( final TypeReference reference, final String gmlVersion ) throws GMLSchemaException
  {
    if( reference instanceof ComplexTypeReference )
      return findBaseType( (ComplexTypeReference) reference, gmlVersion );
    else if( reference instanceof SimpleTypeReference )
      return findBaseType( (SimpleTypeReference) reference, gmlVersion );
    throw new UnsupportedOperationException( "unknown reference: " + reference.getGMLSchema() + " - Version: " + gmlVersion );
  }

  /**
   * @param reference
   */
  private static QName findBaseType( ComplexTypeReference reference, String gmlVersion ) throws GMLSchemaException
  {
    return findBaseType( reference.getGMLSchema(), reference.getComplexType(), gmlVersion );
  }

  /**
   * known types are all types that should be builded to something e.g. featuretype, propertytype or relationtype
   * 
   * @param qName
   * @return true is qName is a known type
   */
  public static boolean isKnownType( final QName qName, String gmlVersion )
  {
    return getKnownTypeFor( qName, gmlVersion ) != null;
  }

  private static QName findBaseType( final ElementReference reference, String gmlVersion ) throws GMLSchemaException
  {
    final Element element = reference.getElement();
    final GMLSchema schema = reference.getGMLSchema();
    return findBaseType( schema, element, gmlVersion );
  }

  /**
   * @param qName
   */
  private static QName getKnownTypeFor( final QName qName, String gmlVersion )
  {
    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    final IMarshallingTypeHandler typeHandler = typeRegistry.getTypeHandlerForTypeName( qName );
    if( typeHandler != null )
      return qName;

    final QName result = qName;
    final String namespaceURI = qName.getNamespaceURI();
    final String localPart = qName.getLocalPart();
    if( NS.GML2.equals( namespaceURI ) )
    {
      if( GMLSchemaUtilities.getBaseOfFeatureType( gmlVersion ).equals( localPart ) )
        return result;

      if( GMLSchemaUtilities.getBaseOfGeometriesType().equals( localPart ) )
        return result;

      if( GMLSchemaUtilities.getBaseOfRelationType( gmlVersion ).equals( localPart ) )
        return result;
    }
    return null;
  }

  /**
   * @param gmlSchema
   * @param element
   * @return complextype
   */
  public static ComplexTypeReference getComplexTypeReferenceFor( final GMLSchema gmlSchema, final Element element ) throws GMLSchemaException
  {
    final QName type = element.getType();
    if( type != null )
      return gmlSchema.resolveComplexTypeReference( type );
    final ComplexType complexType = element.getComplexType();
    return new ComplexTypeReference( gmlSchema, complexType );
  }

  public static String[] createFeaturePathes( final IGMLSchema gmlSchma, final String pathToHere, IFeatureType featureType )
  {
    final List<String> result = new ArrayList<String>();
    if( featureType.getAllGeomteryProperties().length > 0 )
      result.add( pathToHere );
    final IPropertyType[] props = featureType.getProperties();
    for( int i = 0; i < props.length; i++ )
    {
      final IPropertyType pt = props[i];
      if( pt instanceof IRelationType )
      {
        final IRelationType rt = (IRelationType) pt;
        final String newPath = pathToHere + "/" + rt.getQName().getLocalPart();

        final IFeatureType targetFeatureType = rt.getTargetFeatureType();
        final IFeatureType[] targetFeatureTypes = GMLSchemaUtilities.getSubstituts( targetFeatureType, gmlSchma, true, true );

        for( int j = 0; j < targetFeatureTypes.length; j++ )
        {
          final IFeatureType type = targetFeatureTypes[j];
          if( rt.isList() && type.getAllGeomteryProperties().length > 0 )
            result.add( newPath + "[" + type.getQName().getLocalPart() + "]" );
          String[] strings = createFeaturePathes( gmlSchma, newPath + "[" + type.getQName().getLocalPart() + "]", type );
          // for( String string : strings )
          // {
          // // TODO complete
          // }
        }
      }
    }

    return null;
  }

  /**
   * creates schema archive, that recursive includes all xs:include-schemas
   */
  public static void createSchemaArchive( final URL schemaURL, final File archiveFile ) throws XmlException, IOException
  {
    final File tmpBase = FileUtilities.createNewTempDir( "kalypsoSchemaZip" );

    try
    {
      final SchemaDocument schemaDocument = SchemaDocument.Factory.parse( schemaURL );

      createSchemaDir( schemaURL, schemaDocument, tmpBase );

      JarHelper helper = new JarHelper();
      helper.jarDir( tmpBase, archiveFile );
    }
    finally
    {
      FileUtilities.deleteRecursive( tmpBase );
    }
  }

  /**
   * Similiar to
   * {@link #createSchemaArchive(URL, File), but the schema is already loaded and the files are not zipped but put simply into the given directory.  }
   */
  public static void createSchemaDir( final URL schemaURL, final SchemaDocument schemaDocument, final File archiveDir ) throws XmlException, IOException
  {
    final UrlResolver resolver = new UrlResolver();
    final IUrlResolver2 resolver2 = new IUrlResolver2()
    {
      public URL resolveURL( final String relativeOrAbsolute ) throws MalformedURLException
      {
        return resolver.resolveURL( schemaURL, relativeOrAbsolute );
      }
    };

    final Map<URL, String> map = new Hashtable<URL, String>();
    final String baseName = BASE_SCHEMA_IN_JAR;
    map.put( schemaURL, baseName );
    serializeToArchiveDir( schemaDocument, archiveDir, baseName, resolver2, map );
  }

  public static void serializeToArchiveDir( final SchemaDocument schemaDocument, final File baseDir, final String newSchemaLocation, final IUrlResolver2 urlResolver, final Map<URL, String> knownlocations ) throws XmlException, IOException
  {
    final UrlResolver resolver = new UrlResolver();
    // imports
    final Import[] importArray = schemaDocument.getSchema().getImportArray();
    for( int i = 0; i < importArray.length; i++ )
    {
      final Import import_ = importArray[i];
      final String oldSchemaLocation = import_.getSchemaLocation();
      if( oldSchemaLocation != null )
      {
        final URL absolteSchemaLocation = urlResolver.resolveURL( oldSchemaLocation );
        import_.setSchemaLocation( absolteSchemaLocation.toString() );
      }
    }
    // includes
    final Include[] includeArray = schemaDocument.getSchema().getIncludeArray();
    for( int i = 0; i < includeArray.length; i++ )
    {
      final Include include = includeArray[i];
      final String oldSchemaLocation = include.getSchemaLocation();
      final URL originalIncludeURL = urlResolver.resolveURL( oldSchemaLocation );

      // document original schemalocation in schema as annotation
      final Annotation annotation = include.addNewAnnotation();
      final Documentation documentation = annotation.addNewDocumentation();
      documentation.setLang( "en" );
      documentation.setSource( "www.kalypso.wb.tu-harburg.de" );
      final Node domNode = documentation.getDomNode();
      final Document ownerDocument = domNode.getOwnerDocument();
      final Text text = ownerDocument.createTextNode( "original absolute schemaLocation was here: " + originalIncludeURL.toExternalForm() );
      domNode.appendChild( text );

      // find new schemalocation in archive
      final String newIncludeSchemaLocation;
      final boolean doRecursive;
      if( knownlocations.containsKey( originalIncludeURL ) )
      {
        newIncludeSchemaLocation = knownlocations.get( originalIncludeURL );
        doRecursive = false;
      }
      else
      {
        newIncludeSchemaLocation = createSchemaLocation( knownlocations, oldSchemaLocation );
        knownlocations.put( originalIncludeURL, newIncludeSchemaLocation );
        doRecursive = true;
      }
      include.setSchemaLocation( newIncludeSchemaLocation );

      // process included schema is not allready in archive
      if( doRecursive )
      {
        // TODO: maybe the schema is already in the schema cache?
        // wouldn't it be better to fetch it from there?
        final SchemaDocument includedSchemaDocument = SchemaDocument.Factory.parse( originalIncludeURL );

        final IUrlResolver2 newResolver = new IUrlResolver2()
        {
          public URL resolveURL( String relativeOrAbsolute ) throws MalformedURLException
          {
            return resolver.resolveURL( originalIncludeURL, relativeOrAbsolute );
          }
        };
        serializeToArchiveDir( includedSchemaDocument, baseDir, newIncludeSchemaLocation, newResolver, knownlocations );
      }
    }
    // serialize schema itself into archive
    final File newSchemaFile = new File( baseDir, newSchemaLocation );
    final File parent = newSchemaFile.getParentFile();
    if( !parent.exists() )
      parent.mkdirs();
    schemaDocument.save( newSchemaFile );
  }

  private static String createSchemaLocation( final Map<URL, String> knownlocations, final String oldSchemaLocation )
  {
    // first try on expeced format
    String newSchemaLocation = oldSchemaLocation.replaceAll( "\\.xsd$", "" );
    newSchemaLocation = newSchemaLocation.replaceAll( "\\.XSD$", "" );
    newSchemaLocation = newSchemaLocation.replaceAll( ".*\\\\", "" );
    newSchemaLocation = newSchemaLocation.replaceAll( ".*/", "" );
    newSchemaLocation = newSchemaLocation.replaceAll( "\\.", "_" );
    newSchemaLocation = newSchemaLocation + ".xsd";
    if( newSchemaLocation.length() > 4 && !knownlocations.containsValue( newSchemaLocation ) )
      return newSchemaLocation;
    // generate generic result
    String location = "include";
    int i = 1;
    String result;
    do
    {
      result = location + i + ".xsd";
      i++;
    }
    while( knownlocations.containsValue( result ) );
    return result;
  }

  public static URL getSchemaURLForArchive( final URL schemaJarArchive ) throws MalformedURLException
  {
    return m_urlUtitilies.resolveURL( new URL( "jar:" + schemaJarArchive.toString() + "!/" ), BASE_SCHEMA_IN_JAR );
  }

  public static List<ElementWithOccurs> collectElements( GMLSchema schema, ExtensionType extension, List<ElementWithOccurs> collector, Occurs occurs ) throws GMLSchemaException
  {
    if( collector == null )
      collector = new ArrayList<ElementWithOccurs>();
    if( occurs == null )
      occurs = new Occurs( 1, 1 );
    if( extension == null )
      return collector;
    final ExplicitGroup sequence = extension.getSequence();
    collectElements( schema, sequence, collector, occurs );
    final GroupRef group = extension.getGroup();
    collectElements( schema, group, collector, occurs );
    final ExplicitGroup choice = extension.getChoice();
    collectElements( schema, choice, collector, occurs );
    final All all = extension.getAll();
    collectElements( schema, all, collector, occurs );
    return collector;
  }

  public static List<ElementWithOccurs> collectElements( GMLSchema schema, ComplexType complexType, List<ElementWithOccurs> collector, Occurs occurs ) throws GMLSchemaException
  {
    if( collector == null )
      collector = new ArrayList<ElementWithOccurs>();
    if( occurs == null )
      occurs = new Occurs( 1, 1 );
    if( complexType == null )
      return collector;
    final ExplicitGroup sequence = complexType.getSequence();
    collectElements( schema, sequence, collector, occurs );
    final GroupRef group = complexType.getGroup();
    collectElements( schema, group, collector, occurs );
    final ExplicitGroup choice = complexType.getChoice();
    collectElements( schema, choice, collector, occurs );
    final All all = complexType.getAll();
    collectElements( schema, all, collector, occurs );
    return collector;
  }

  public static List<ElementWithOccurs> collectElements( GMLSchema schema, ComplexRestrictionType restriction, List<ElementWithOccurs> collector, Occurs occurs ) throws GMLSchemaException
  {
    if( collector == null )
      collector = new ArrayList<ElementWithOccurs>();
    if( occurs == null )
      occurs = new Occurs( 1, 1 );
    if( restriction == null )
      return collector;
    final ExplicitGroup sequence = restriction.getSequence();
    collectElements( schema, sequence, collector, occurs );
    final GroupRef group = restriction.getGroup();
    collectElements( schema, group, collector, occurs );
    final ExplicitGroup choice = restriction.getChoice();
    collectElements( schema, choice, collector, occurs );
    final All all = restriction.getAll();
    collectElements( schema, all, collector, occurs );

    return collector;
  }

  public static List<ElementWithOccurs> collectElements( GMLSchema schema, Group group, List<ElementWithOccurs> collector, Occurs occurs ) throws GMLSchemaException
  {
    if( collector == null )
      collector = new ArrayList<ElementWithOccurs>();
    if( occurs == null )
      occurs = new Occurs( 1, 1 );
    if( group == null )
      return collector;
    occurs = getOccursFromGroup( group ).merge( occurs );

    // elements
    final LocalElement[] elementArray = group.getElementArray();
    if( elementArray != null )
    {
      for( int i = 0; i < elementArray.length; i++ )
        collector.add( new ElementWithOccurs( elementArray[i], occurs ) );
    }
    // sequence
    final ExplicitGroup[] sequenceArray = group.getSequenceArray();
    if( sequenceArray != null )
    {
      for( int i = 0; i < sequenceArray.length; i++ )
        collectElements( schema, sequenceArray[i], collector, occurs );
    }
    // all
    final All[] allArray = group.getAllArray();
    if( allArray != null )
    {
      for( int i = 0; i < allArray.length; i++ )
        collectElements( schema, allArray[i], collector, occurs );
    }
    // groups
    final GroupRef[] groupArray = group.getGroupArray();
    if( groupArray != null )
    {
      for( int i = 0; i < groupArray.length; i++ )
        collectElements( schema, groupArray[i], collector, occurs );
    }
    // group reference
    final QName ref = group.getRef();
    if( ref != null )
    {
      final GroupReference reference = schema.resolveGroupReference( ref );
      final NamedGroup refGroup = reference.getGroup();
      final GMLSchema refSchema = reference.getGMLSchema();
      collectElements( refSchema, refGroup, collector, occurs );
    }
    // TODO choices are now handled as sequence, this is not correct -> implement better choice
    // support or concept of validator

    final ExplicitGroup[] choiceArray = group.getChoiceArray();
    if( choiceArray != null )
    {
      for( int i = 0; i < choiceArray.length; i++ )
        collectElements( schema, choiceArray[i], collector, occurs );
    }
    return collector;
  }

  /**
   * GML-Version switch
   */
  public static String getBaseOfFeatureType( final String gmlVersion )
  {
    if( gmlVersion.startsWith( "2" ) )
      return GMLSchemaConstants.GML2_FeatureTypeBaseType;
    if( gmlVersion.startsWith( "3" ) )
      return GMLSchemaConstants.GML3_FeatureTypeBaseType;
    throw new UnsupportedOperationException( "GML-schema version '" + gmlVersion + "' is not supported" );
  }

  public static String getBaseOfRelationType( String gmlVersion )
  {
    if( gmlVersion.startsWith( "2" ) )
      return GMLSchemaConstants.GML2_RelationBaseType;
    if( gmlVersion.startsWith( "3" ) )
      return GMLSchemaConstants.GML3_RelationBaseType;
    throw new UnsupportedOperationException( "GML-schema version '" + gmlVersion + "' is not supported" );
  }

  public static QName getIdAttribute( final String gmlVersion )
  {
    if( gmlVersion.startsWith( "2" ) )
      return new QName( NS.GML3, "fid" );

    if( gmlVersion.startsWith( "3" ) )
      return new QName( NS.GML3, "id" );

    throw new UnsupportedOperationException( "GML-schema version '" + gmlVersion + "' is not supported" );
  }

  public static String getBaseOfGeometriesType( )
  {
    return "AbstractGeometryType";
  }

  /** Splitts a schema location string into pairs of namespace-uri's and URLs. Errors in the schmeaLocation are ignored. */
  public static Map<String, URL> parseSchemaLocation( final String schemaLocation, final URL context )
  {
    final int schemaCount = schemaLocation == null ? 0 : schemaLocation.length() / 2 + 1;
    final Map<String, URL> map = new HashMap<String, URL>( schemaCount );

    if( schemaLocation == null )
      return map;

    final String[] splittetSchemaLocation = schemaLocation.split( "\\s+" );

    for( int i = 0; i < splittetSchemaLocation.length; i++ )
    {
      final String namespace = splittetSchemaLocation[i];
      i++;
      if( i < splittetSchemaLocation.length )
      {
        final String location = splittetSchemaLocation[i];
        try
        {
          final URL url = new URL( context, location );
          map.put( namespace, url );
        }
        catch( final MalformedURLException ignores )
        {
          // do not check for parse errors, ust ignore them for backwards compability
          // throw new GMLSchemaException( "Error in schemaLocation. This is no URL: " + location, e );
        }
      }
      // do not check for parse errors, ust ignore them for backwards compability
      // else
      // throw new GMLSchemaException( "Syntax error in schemaLocation, must be pairs of namespace-url: " +
      // schemaLocation );
    }

    return map;
  }

  public static Occurs getOccursFromElement( final Element element )
  {
    final int min;
    final int max;
    if( !element.isSetMinOccurs() )
      min = 1;
    else
      min = getOccursFromObject( element.getMinOccurs() );
    if( !element.isSetMaxOccurs() )
      max = 1;
    else
      max = getOccursFromObject( element.getMaxOccurs() );
    return new Occurs( min, max );
  }

  private static Occurs getOccursFromGroup( final Group group )
  {
    final int min;
    final int max;
    if( group.isSetMinOccurs() )
      min = getOccursFromObject( group.getMinOccurs() );
    else
      min = 1;
    if( group.isSetMaxOccurs() )
      max = getOccursFromObject( group.getMaxOccurs() );
    else
      max = 1;
    return new Occurs( min, max );

  }

  private static int getOccursFromObject( final Object occursValue )
  {
    if( occursValue == null )
      return 1;
    if( occursValue instanceof Number )
      return ((Number) occursValue).intValue();
    if( "unbounded".equals( occursValue ) )
      return IPropertyType.UNBOUND_OCCURENCY;
    throw new UnsupportedOperationException( "unknown occurency in schema: " + occursValue.toString() );
  }

  /**
   * Reads the Gml-Version from the annotation of the schema document
   * <p>
   * <code>
   * &lt;xs:annotation&gt;
   *   &lt;xs:appinfo xmlns:kapp="org.kalypso.appinfo"&gt;
   *     &lt;kapp:gmlVersion>3.1.1&lt;/kapp:gmlVersion&gt;
   *   &lt;/xs:appinfo&gt;
   * &lt;/xs:annotation&gt;
   * </code>
   * </p>
   */
  public static String parseGmlVersion( final SchemaDocument schemaDocument )
  {
    final String namespaceDecl = "declare namespace xs='" + NS.XSD_SCHEMA + "' " + "declare namespace kapp" + "='org.kalypso.appinfo' ";
    final String xpath = "xs:schema/xs:annotation/xs:appinfo/kapp:gmlVersion";

    final String fullXpath = namespaceDecl + xpath;

    final XmlObject[] xmlObjects = schemaDocument.selectPath( fullXpath );

    switch( xmlObjects.length )
    {
      case 0:
        return null;

      case 1:
        final XmlAnyTypeImpl anyType = (XmlAnyTypeImpl) xmlObjects[0];
        return anyType.stringValue();

      default:
        throw new UnsupportedOperationException( "can not handle multi 'kapp:gmlVersion' fragments in schema: " + schemaDocument.getSchema().getTargetNamespace() );
    }
  }

  public static IFeatureType[] getSubstituts( final IFeatureType ft, IGMLSchema contextSchema, final boolean includeAbstract, final boolean inclusiveThis )
  {
    if( contextSchema == null )
      contextSchema = ft.getGMLSchema();

    // query for it
    final FindSubstitutesGMLSchemaVisitor visitor = new FindSubstitutesGMLSchemaVisitor( ft, includeAbstract, inclusiveThis );
    contextSchema.accept( visitor );
    return visitor.getSubstitutes();
  }

}
