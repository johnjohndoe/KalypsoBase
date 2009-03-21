/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.gmlschema.annotation;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.Facet;
import org.apache.xmlbeans.impl.xb.xsdschema.AnnotationDocument.Annotation;
import org.apache.xmlbeans.impl.xb.xsdschema.DocumentationDocument.Documentation;
import org.eclipse.core.runtime.Platform;
import org.kalypso.commons.xml.NS;
import org.kalypso.commons.xml.NSPrefixProvider;
import org.kalypso.commons.xml.NSUtilities;
import org.kalypso.contribs.java.net.URNUtilities;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.xml.ElementReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Adreas von Dömming
 */
public class AnnotationUtilities
{
  private static final char SEPARATOR = '_';

  public static IAnnotation parseAnnontation( final String key, final NodeList listAnnontation )
  {
    if( listAnnontation == null || listAnnontation.getLength() == 0 )
      return null;

    for( int i = 0; i < listAnnontation.getLength(); i++ )
    {
      final Node item = listAnnontation.item( i );
      if( item instanceof org.w3c.dom.Element )
      {
        final NodeList languageNodes = item.getChildNodes();

        for( int ln = 0; ln < languageNodes.getLength(); ln++ )
        {
          final Node itmLang = languageNodes.item( ln );

          if( itmLang instanceof org.w3c.dom.Element )
          {
            final DefaultAnnotation annotation = computeDefaultAnnontation( key, itmLang );
            if( AnnotationUtilities.isCurrentLang( annotation.getLang() ) )
              return annotation;
          }
        }
      }
    }

    return null;
  }

  private static DefaultAnnotation computeDefaultAnnontation( final String key, final Node itmLang )
  {
    final NamedNodeMap attributes = itmLang.getAttributes();
    final Node langNode = attributes.getNamedItem( "xml:lang" );
    final String language = langNode.getTextContent();

    final DefaultAnnotation annotation = new DefaultAnnotation( language, key );

    final NodeList childNodes = itmLang.getChildNodes();
    for( int i = 0; i < childNodes.getLength(); i++ )
    {
      final Node item = childNodes.item( i );

      final String name = item.getLocalName();
      final String value = item.getTextContent();

      annotation.putValue( name, value );
    }

    return annotation;
  }

  /**
   * Create an IAnnotation from an xsd-annotation element.
   * <p>
   * All inner elements from the document element are regarded as annotations.
   * </p>
   *
   * @param createDefaultAnnotation
   *            If true, even if no annotation data is found, return a default annotation based on the defaultName; if
   *            false it returns null in this case.
   */
  public static IAnnotation annotationForElement( final Annotation xsdAnnotation, final String defaultName, final boolean createDefaultAnnotation )
  {
    // TODO: check for annotation from catalog

    final String platformLang = Platform.getNL();

    if( xsdAnnotation == null )
      return createDefaultAnnotation( platformLang, defaultName, createDefaultAnnotation );
    final Documentation[] documentationArray = xsdAnnotation.getDocumentationArray();
    if( documentationArray == null )
      return createDefaultAnnotation( platformLang, defaultName, createDefaultAnnotation );

    IAnnotation defaultAnnotation = createDefaultAnnotation( platformLang, defaultName, createDefaultAnnotation );
    for( final Documentation documentation : documentationArray )
    {
      final String lang = documentation.getLang();
      if( lang == null )
      {
        // the default annotation; keep if no specific language is defined
        final IAnnotation defaultLangAnnotation = buildAnnotation( documentation, defaultName, false );
        if( defaultLangAnnotation != null )
          defaultAnnotation = defaultLangAnnotation;
      }
      else if( isCurrentLang( lang ) )
      {
        final IAnnotation annotation = buildAnnotation( documentation, defaultName, createDefaultAnnotation );
        if( annotation != null )
          return annotation;

        // keep going even if language was found, maybe we still have a default entry
      }
    }

    return defaultAnnotation;
  }

  private static IAnnotation buildAnnotation( final Documentation documentation, final String defaultName, final boolean createDefaultAnnotation )
  {
    final String lang = documentation.getLang();
    final DefaultAnnotation annotation = new DefaultAnnotation( lang, defaultName );

    // Put all inner elements into the annotation-hash
    boolean empty = true;
    final Node domNode = documentation.getDomNode();
    final NodeList childNodes = domNode.getChildNodes();
    for( int n = 0; n < childNodes.getLength(); n++ )
    {
      final Node node = childNodes.item( n );
      if( NS.XSD_SCHEMA.equals( node.getNamespaceURI() ) )
      {
        final String name = node.getLocalName();
        final String value = getStringValue( node );
        annotation.putValue( name, value );

        empty = false;
      }
    }

    if( empty && !createDefaultAnnotation )
      return null;

    return annotation;
  }

  public static IAnnotation annotationForFacet( final GMLSchema gmlSchema, final Facet facet, final QName[] qnames, final String enumString )
  {
    final QName[] qnamesWithEnum = new QName[qnames.length + 1];
    System.arraycopy( qnames, 0, qnamesWithEnum, 0, qnames.length );
    qnamesWithEnum[qnames.length] = new QName( null, enumString );

    final IAnnotation annotationFromProperties = AnnotationUtilities.annotationFromProperties( gmlSchema.getI18nProperties(), qnamesWithEnum, enumString );
    if( annotationFromProperties != null )
      return annotationFromProperties;

    return AnnotationUtilities.annotationForElement( facet.getAnnotation(), enumString, true );
  }

  /**
   * Creates the annotation for a property element.<br>
   * The search order is:
   * <ul>
   * <li>from properties</li>
   * <li>from reference definition (element within the feature definition)</li>
   * <li>from referenced element definition (if it is a reference)</li>
   * </ul>
   */
  public static IAnnotation createAnnotation( final QName qname, final IFeatureType featureType, final Element element, final ElementReference reference )
  {
    // HACK: if null, we are in the first cycle, so we do not yet need to parse the annotation
    // Only if the featureType is set, the real property is created
    if( featureType == null )
      return null;

    final IAnnotation annotationFromProperties = annotationFromProperties( qname, featureType );
    if( annotationFromProperties != null )
      return annotationFromProperties;

    if( element == null )
      return new DefaultAnnotation( Platform.getNL(), qname.getLocalPart() );

    // Now from the annotation if its defining element
    final IAnnotation directAnnotation = AnnotationUtilities.annotationForElement( element.getAnnotation(), element.getName(), false );
    if( directAnnotation != null )
      return directAnnotation;

    if( reference == null )
      return new DefaultAnnotation( Platform.getNL(), qname.getLocalPart() );

    // Now from the annotation if its referenced element
    final Element referencedElement = reference.getElement();
    return AnnotationUtilities.annotationForElement( referencedElement.getAnnotation(), referencedElement.getName(), true );
  }

  public static IAnnotation annotationFromProperties( final QName qname, final IFeatureType featureType )
  {
    if( featureType == null )
      return null;

    // REMARK: see below; It would be nice to break the recursion here, as soon as property is no more contained in
    // substituting feature. But: the feature about to be build does not know its properties yet...
    // if( featureType.getProperty( qname ) == null )
    // return null;

    final QName featureQName = featureType.getQName();
    // REMARK: first, we are using the schema of the enclosing feature type, as we want to consider the annotation in
    // respect to its feature
    final GMLSchema featureSchema = (GMLSchema) featureType.getGMLSchema();
    final IAnnotation annotation = AnnotationUtilities.annotationFromProperties( featureSchema.getI18nProperties(), new QName[] { featureQName, qname }, qname.getLocalPart() );
    if( annotation != null )
      return annotation;

    // REMARK: recurse into substitution hierarchy. Allows definition of annotations in the substituted feature
    return annotationFromProperties( qname, featureType.getSubstitutionGroupFT() );
  }

  /**
   * Returns the text contained in the specified element. The returned value is trimmed by calling the trim() method of
   * java.lang.String
   * <p>
   *
   * @param node
   *            current element
   * @return the textual contents of the element or null, if it is missing
   */
  private static String getStringValue( final Node node )
  {
    final NodeList children = node.getChildNodes();

    final StringBuffer sb = new StringBuffer( children.getLength() * 500 );
    for( int i = 0; i < children.getLength(); i++ )
    {
      if( children.item( i ).getNodeType() == Node.TEXT_NODE || children.item( i ).getNodeType() == Node.CDATA_SECTION_NODE )
        sb.append( children.item( i ).getNodeValue() );
    }

    return sb.toString().trim();
  }

  /**
   * @param createDefaultAnnotation
   *            If false, return null.
   */
  private static IAnnotation createDefaultAnnotation( final String lang, final String defaultName, final boolean createDefaultAnnotation )
  {
    if( !createDefaultAnnotation )
      return null;

    return new DefaultAnnotation( lang, defaultName );
  }

  public static IAnnotation annotationFromProperties( final Properties properties, final QName[] qnames, final String defaultName )
  {
    final String[] keys = createKeys( properties, qnames );
    for( final String key : keys )
    {
      final IAnnotation annotation = findAnnotation( properties, key, defaultName );
      if( annotation != null )
        return annotation;
    }

    return null;
  }

  private static String[] createKeys( final Properties props, final QName[] qnames )
  {
    /* Produce the different types of keys */
    final Set<String> keys = new HashSet<String>( 5 );

    for( final KeyTypes type : KeyTypes.values() )
      keys.add( buildKey( type, props, qnames ) );

    return keys.toArray( new String[keys.size()] );
  }

  private enum KeyTypes
  {
    full,
    prefixFromProperties,
    prefixFromCatalog,
    onlyNames,
// onlyFeatureNamespaceFull,
// onlyFeatureNamespaceProperties,
// onlyFeatureNamespaceCatalog
  }

  private static String buildKey( final KeyTypes type, final Properties properties, final QName[] qnames )
  {
    final StringBuffer sb = new StringBuffer();

    for( final QName qname : qnames )
    {
      final NSPrefixProvider nsMapper = NSUtilities.getNSProvider();

      /* Produce all parts */
      final String namespace = qname == null ? null : qname.getNamespaceURI();
      final boolean namespaceIsNull = namespace == null || namespace.length() == 0;
      final String localPart = qname == null ? null : qname.getLocalPart();
      final String shortnameFromProperties = namespaceIsNull ? null : properties.getProperty( namespace, null );
      final String shortnameFromCatalog;
      // Allow for known prefix, but avoid creation of generated prefix
      if( namespaceIsNull || !nsMapper.hasPrefix( namespace ) )
        shortnameFromCatalog = null;
      else
        shortnameFromCatalog = nsMapper.getPreferredPrefix( namespace, null );

      final String ns = namespaceIsNull ? null : URNUtilities.convertURN( namespace );

      switch( type )
      {
        case full:
          appendNonNull( sb, ns );
          break;

        case prefixFromCatalog:
          if( shortnameFromCatalog == null )
            appendNonNull( sb, shortnameFromProperties );
          else
            appendNonNull( sb, shortnameFromCatalog );
          break;

        case prefixFromProperties:
          if( shortnameFromProperties == null )
            appendNonNull( sb, shortnameFromCatalog );
          else
            appendNonNull( sb, shortnameFromProperties );
          break;

        case onlyNames:
          break;
      }

      appendNonNull( sb, localPart );
    }

    return sb.toString();
  }

  private static void appendNonNull( final StringBuffer sb, final String toAppend )
  {
    if( toAppend != null )
    {
      sb.append( toAppend );
      sb.append( SEPARATOR );
    }
  }

  private static IAnnotation findAnnotation( final Properties props, final String key, final String defaultName )
  {
    final String lang = Platform.getNL();

    final String nameValue = props.getProperty( key + "name", null );
    final String labelValue = props.getProperty( key + "label" );
    final String tooltipValue = props.getProperty( key + "tooltip" );
    final String descriptionValue = props.getProperty( key + "description" );

    if( nameValue == null && labelValue == null && tooltipValue == null && descriptionValue == null )
      return null;

    final DefaultAnnotation annotation = new DefaultAnnotation( lang, defaultName );
    if( nameValue != null )
      annotation.putValue( IAnnotation.ANNO_NAME, nameValue );
    if( labelValue != null )
      annotation.putValue( IAnnotation.ANNO_LABEL, labelValue );
    if( tooltipValue != null )
      annotation.putValue( IAnnotation.ANNO_TOOLTIP, tooltipValue );
    if( descriptionValue != null )
      annotation.putValue( IAnnotation.ANNO_DESCRIPTION, descriptionValue );

    return annotation;
  }

  /**
   * Check if the given language string is the current platform lang.<br>
   * The check is somewhat lax in order to allow simplified language string ('de' instead of 'de_DE') in schema files.
   */
  public static boolean isCurrentLang( final String lang )
  {
    final String nl = Platform.getNL();
    return nl.startsWith( lang );
  }

}
