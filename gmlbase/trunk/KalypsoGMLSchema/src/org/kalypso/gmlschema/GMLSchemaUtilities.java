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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.common.JarHelper;
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
import org.kalypso.commons.java.net.UrlResolver;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.contribs.java.net.UrlUtilities;
import org.kalypso.contribs.javax.xml.namespace.ListQName;
import org.kalypso.contribs.javax.xml.namespace.MixedQName;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.ITypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
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

  private static UrlUtilities m_urlUtitilies = new UrlUtilities();

  /**
   * @param gmlSchema
   * @param complexType
   */
  public static QName findBaseType( GMLSchema gmlSchema, ComplexType complexType )
  {
    final ExplicitGroup sequence = complexType.getSequence();
    final String name = complexType.getName();

    // 1. check if it is a named type
    // propably a typehanlder exists for this
    if( name != null )
    {
      final QName typeQName = new QName( gmlSchema.getTargetNamespace(), name );
      if( isKnownType( typeQName ) )
        return getKnownTypeFor( typeQName );
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
      System.out.println( "unknown type:" + complexType.getName() );
      return null;
      // throw new UnsupportedOperationException( "unknown base type for " + complexType.toString() );
    }

    // propably a typehanlder exists for this
    if( isKnownType( base ) )
      return getKnownTypeFor( base );

    // 3. handle a reference
    final TypeReference reference = gmlSchema.resolveTypeReference( base );
    if( reference instanceof ComplexTypeReference )
      return findBaseType( (ComplexTypeReference) reference );
    else if( reference instanceof SimpleTypeReference )
      return findBaseType( (SimpleTypeReference) reference );
    else
      throw new UnsupportedOperationException();
  }

  private static boolean isXDSAnyType( QName base )
  {
    if( !base.getNamespaceURI().equals( GMLSchemaConstants.NS_XMLSCHEMA ) )
      return false;
    return base.getLocalPart().equals( "anyType" );
  }

  /**
   * @param schema
   * @param simpleType
   */
  public static QName findBaseType( GMLSchema schema, SimpleType simpleType )
  {

    // 1. check if it is a named type
    // propably a typehanlder exists for this
    final String name = simpleType.getName();
    if( name != null )
    {
      final QName namedBase = new QName( schema.getTargetNamespace(), name );
      if( isKnownType( namedBase ) )
        return getKnownTypeFor( namedBase );
    }

    // 2. check for base
    // propably a typehanlder exists for this
    final Restriction restriction = simpleType.getRestriction();
    if( restriction != null )
      return findBaseType( schema, restriction );

    final Union union = simpleType.getUnion();
    if( union != null )
      return findBaseType( schema, union );
    final org.apache.xmlbeans.impl.xb.xsdschema.ListDocument.List list = simpleType.getList();
    if( list != null )
      return findBaseType( schema, list );
    // should never happen !
    throw new UnsupportedOperationException();
  }

  private static QName findBaseType( GMLSchema schema, org.apache.xmlbeans.impl.xb.xsdschema.ListDocument.List list )
  {
    final LocalSimpleType simpleType = list.getSimpleType();
    final QName itemType = list.getItemType();

    QName base = null;
    if( simpleType != null )
      base = findBaseType( schema, simpleType );
    else if( itemType != null )
    {
      if( isKnownType( itemType ) )
        base = getKnownTypeFor( itemType );
      else
      {
        final SimpleTypeReference reference = schema.resolveSimpleTypeReference( itemType );
        base = findBaseType( reference );
      }
    }
    if( base == null )
      return null;
    return new ListQName( base );
  }

  private static QName findBaseType( GMLSchema schema, Union union )
  {
    final Set<QName> mixedSet = new HashSet<QName>();
    // embeddded simple types
    final LocalSimpleType[] simpleTypeArray = union.getSimpleTypeArray();
    if( simpleTypeArray.length > 0 )
    {
      for( int i = 0; i < simpleTypeArray.length; i++ )
      {
        final QName base = findBaseType( schema, simpleTypeArray[i] );
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
        if( isKnownType( typeQName ) )
          mixedSet.add( getKnownTypeFor( typeQName ) );
        else
        {
          final SimpleTypeReference reference = schema.resolveSimpleTypeReference( typeQName );
          mixedSet.add( findBaseType( reference ) );
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

  private static QName findBaseType( GMLSchema schema, Restriction restriction )
  {
    final QName base = restriction.getBase();
    if( isKnownType( base ) )
      return getKnownTypeFor( base );

    // 3. handle a reference
    final TypeReference reference = schema.resolveTypeReference( base );
    // final ElementReference reference = (ElementReference) resolveReference;
    return findBaseType( reference );

  }

  public static QName findBaseType( final GMLSchema schema, final Element element )
  {
    final QName qName;
    final int constructionType = getConstructionType( element );
    switch( constructionType )
    {
      case CONSTRUCTION_ANONYMOUS_SIMPLE_TYPE:
        final LocalSimpleType simpleType = element.getSimpleType();
        return findBaseType( schema, simpleType );
      case CONSTRUCTION_ANONYMOUS_COMPLEX_TYPE:
        final LocalComplexType complexType = element.getComplexType();
        return findBaseType( schema, complexType );
      case CONSTRUCTION_REFERENCED_TYPE:
        qName = element.getRef();
        break;
      case CONSTRUCTION_NAMED_TYPE:
        qName = element.getType();
        break;
      case CONSTRUCTION_ABSTRACT_ANYTYPE:
      case CONSTRUCTION_ANYTYPE:
        return new QName( NS.NS_XSD_SCHEMA, "anyType" ); // no type
      default:
        throw new UnsupportedOperationException();
    }

    if( isKnownType( qName ) )
      return getKnownTypeFor( qName );

    switch( constructionType )
    {
      case CONSTRUCTION_REFERENCED_TYPE:
        final ElementReference elementReference = schema.resolveElementReference( qName );
        if( elementReference == null )
        {
          System.out.println( "could not resolve element reference to " + qName );
          return null;
        }
        return findBaseType( elementReference );
      case CONSTRUCTION_NAMED_TYPE:
        final TypeReference typeReference = schema.resolveTypeReference( qName );
        return findBaseType( typeReference );
      default:
        throw new UnsupportedOperationException( "unknown base type for " + element.toString() );
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
  private static QName findBaseType( SimpleTypeReference reference )
  {
    return findBaseType( reference.getGMLSchema(), reference.getSimpleType() );
  }

  /**
   * @param reference
   */
  private static QName findBaseType( TypeReference reference )
  {
    if( reference instanceof ComplexTypeReference )
      return findBaseType( (ComplexTypeReference) reference );
    else if( reference instanceof SimpleTypeReference )
      return findBaseType( (SimpleTypeReference) reference );
    throw new UnsupportedOperationException( "unknown refrence" );
  }

  /**
   * @param reference
   */
  private static QName findBaseType( ComplexTypeReference reference )
  {
    return findBaseType( reference.getGMLSchema(), reference.getComplexType() );
  }

  /**
   * known types are all types that should be builded to something e.g. featuretype, propertytype or relationtype
   * 
   * @param qName
   * @return true is qName is a known type
   */
  private static boolean isKnownType( final QName qName )
  {
    return getKnownTypeFor( qName ) != null;
  }

  private static QName findBaseType( final ElementReference reference )
  {
    final Element element = reference.getElement();
    final GMLSchema schema = reference.getGMLSchema();
    return findBaseType( schema, element );
  }

  /**
   * @param qName
   */
  private static QName getKnownTypeFor( final QName qName )
  {
    final ITypeRegistry typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    final ITypeHandler typeHandler = typeRegistry.getTypeHandlerForTypeName( qName );
    if( typeHandler != null )
      return qName;

    final QName result = qName;
    final String namespaceURI = qName.getNamespaceURI();
    final String localPart = qName.getLocalPart();
    if( GMLSchemaConstants.NS_GML2.equals( namespaceURI ) )
    {

      // IMPORTANT:
      // - AbstractFeatureType is here to support GML2-Schema, do not remove it !!
      // - Do not change order of AbstractFeatureType and AbstractGMLType !!
      // 1. AbstractFeatureType
      // 2. AbstractGMLType
      if( "AbstractFeatureType".equals( localPart ) ) // -> build a FeatureType
        return result;
      if( "AbstractGMLType".equals( localPart ) ) // -> build a FeatureType also
        return result;
      else if( "FeatureAssociationType".equals( localPart ) ) // -> build a RelationType
        return result;
      else if( "FeaturePropertyType".equals( localPart ) ) // -> build a RelationType
        return result;
    }
    return null;
  }

  /**
   * @param gmlSchema
   * @param element
   * @return complextype
   */
  public static ComplexTypeReference getComplexTypeReferenceFor( final GMLSchema gmlSchema, final Element element )
  {
    final QName type = element.getType();
    if( type != null )
      return gmlSchema.resolveComplexTypeReference( type );
    final ComplexType complexType = element.getComplexType();
    return new ComplexTypeReference( gmlSchema, complexType );
  }

  public static String[] createFeaturePathes( final GMLSchema gmlSchma, final String pathToHere, IFeatureType featureType )
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
        final IFeatureType[] targetFeatureTypes = targetFeatureType.getSubstituts( gmlSchma, true, true );

        for( int j = 0; j < targetFeatureTypes.length; j++ )
        {
          final IFeatureType type = targetFeatureTypes[j];
          if( rt.isList() && type.getAllGeomteryProperties().length > 0 )
            result.add( newPath + "[" + type.getQName().getLocalPart() + "]" );
          createFeaturePathes( gmlSchma, newPath + "[" + type.getQName().getLocalPart() + "]", type );
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

    final SchemaDocument schemaDocument = SchemaDocument.Factory.parse( schemaURL );

    createSchemaDir( schemaURL, schemaDocument, tmpBase );

    JarHelper helper = new JarHelper();
    helper.jarDir( tmpBase, archiveFile );

    FileUtilities.deleteRecursive( tmpBase );
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
      final URL absolteSchemaLocation = urlResolver.resolveURL( oldSchemaLocation );
      import_.setSchemaLocation( absolteSchemaLocation.toString() );
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

  public static List<LocalElement> collectElements( GMLSchema schema, ExtensionType extension, List<LocalElement> collector )
  {
    if( collector == null )
      collector = new ArrayList<LocalElement>();
    final ExplicitGroup sequence = extension.getSequence();
    if( sequence != null )
      collectElements( schema, sequence, collector );
    final GroupRef group = extension.getGroup();
    if( group != null )
      collectElements( schema, group, collector );
    return collector;
  }

  public static List<LocalElement> collectElements( GMLSchema schema, ComplexRestrictionType restriction, List<LocalElement> collector )
  {
    if( collector == null )
      collector = new ArrayList<LocalElement>();
    final ExplicitGroup sequence = restriction.getSequence();
    if( sequence != null )
      collectElements( schema, sequence, collector );
    final GroupRef group = restriction.getGroup();
    if( group != null )
      collectElements( schema, group, collector );
    return collector;
  }

  public static List<LocalElement> collectElements( GMLSchema schema, Group group, List<LocalElement> collector )
  {
    if( collector == null )
      collector = new ArrayList<LocalElement>();
    // elements
    final LocalElement[] elementArray = group.getElementArray();
    if( elementArray != null )
    {
      for( int i = 0; i < elementArray.length; i++ )
        collector.add( elementArray[i] );
    }
    final ExplicitGroup[] sequenceArray = group.getSequenceArray();
    // sequence
    if( sequenceArray != null )
    {
      for( int i = 0; i < sequenceArray.length; i++ )
      {
        final ExplicitGroup group2 = sequenceArray[i];
        collectElements( schema, group2, collector );
      }
    }
    // all
    final All[] allArray = group.getAllArray();
    if( allArray != null )
    {
      for( int i = 0; i < allArray.length; i++ )
      {
        final All all = allArray[i];
        collectElements( schema, all, collector );
      }
    }
    // groups
    final GroupRef[] groupArray = group.getGroupArray();
    if( groupArray != null )
    {
      for( int i = 0; i < groupArray.length; i++ )
      {
        GroupRef ref = groupArray[i];
        collectElements( schema, ref, collector );
      }
    }
    // group reference
    final QName ref = group.getRef();
    if( ref != null )
    {
      final GroupReference reference = schema.resolveGroupReference( ref );
      final NamedGroup refGroup = reference.getGroup();
      final GMLSchema refSchema = reference.getGMLSchema();
      return collectElements( refSchema, refGroup, collector );
    }
    return collector;
  }

}
