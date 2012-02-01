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
package org.kalypso.gml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.kalypso.commons.xml.NS;
import org.kalypso.commons.xml.NSPrefixProvider;
import org.kalypso.commons.xml.NSUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ISimpleMarshallingTypeHandler;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.XLinkedFeature_Impl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Writes an GMLWorkspace into a {@link XMLReader}.
 * 
 * @author Andreas von Dömming
 */
public class GMLSAXFactory
{
  private final NSPrefixProvider m_nsMapper = NSUtilities.getNSProvider();

  /** Namespace -> prefix: Contains the namespaces, whose prefix was already registered in the content handler. */
  private final Map<String, String> m_usedPrefixes = new HashMap<String, String>();

  private final XMLReader m_reader;

  private final String m_gmlVersion;

  private final QName m_xlinkAttributeName;

  private final QName m_schemaLocationAttributeName;

  public GMLSAXFactory( final XMLReader reader, final String gmlVersion )
  {
    m_reader = reader;
    m_gmlVersion = gmlVersion;

    // Initialise after handler is set
    m_xlinkAttributeName = getPrefixedQName( new QName( NS.XLINK, "href" ) );
    m_schemaLocationAttributeName = getPrefixedQName( new QName( NS.XSD, "schemaLocation" ) ); //$NON-NLS-1$
  }

  public void process( final GMLWorkspace workspace ) throws SAXException
  {
    final Feature rootFeature = workspace.getRootFeature();

    final IFeatureType rootFT = rootFeature.getFeatureType();
    final QName rootQName = rootFT.getQName();

    // REMARK: always force the root element to have the empty namespace prefix
    final String rootNamespace = rootQName.getNamespaceURI();
    m_usedPrefixes.put( rootNamespace, XMLConstants.DEFAULT_NS_PREFIX );

    forcePrefixes( workspace, rootNamespace );

    // Add schemalocation string
    final AttributesImpl a = new AttributesImpl();
    final String schemaLocationString = workspace.getSchemaLocationString();
    if( schemaLocationString != null && schemaLocationString.length() > 0 )
    {

      addAttribute( a, m_schemaLocationAttributeName, "CDATA", schemaLocationString );
    }

    processFeature( rootFeature, a );
  }

  private void forcePrefixes( final GMLWorkspace workspace, final String rootNamespace ) throws SAXException
  {
    final Set<String> forcedTypes = new TreeSet<String>();

    final String[] knownNamespaces = forceFeatureTypePrefixes( workspace );
    final String[] additionalNamespaces = forceAdditionalSchemaPrefixes( workspace );

    forcedTypes.addAll( Arrays.asList( knownNamespaces ) );
    forcedTypes.addAll( Arrays.asList( additionalNamespaces ) );

    forcedTypes.remove( rootNamespace );
    // REMARK: remove namespaces of some known simple types: the namespace definition is not needed for them
    forcedTypes.remove( XMLConstants.W3C_XML_SCHEMA_NS_URI );

    /* Add them all */
    final ContentHandler contentHandler = m_reader.getContentHandler();
    for( final String namespace : forcedTypes )
    {
      final String preferedPrefix = getPreferedPrefix( namespace );
      contentHandler.startPrefixMapping( preferedPrefix, namespace );
    }
  }

  private void addAttribute( final AttributesImpl a, final QName name, final String type, final String value ) throws SAXException
  {
    final String namespaceURI = name.getNamespaceURI();
    final String localName = name.getLocalPart();

    m_reader.getContentHandler().startPrefixMapping( name.getPrefix(), namespaceURI );

    final String qName = formatPrefixedQName( name );

    a.addAttribute( namespaceURI, localName, qName, type, value );
  }

  private static String formatPrefixedQName( final QName name )
  {
    final String prefix = name.getPrefix();
    final String localName = name.getLocalPart();

    if( StringUtils.isBlank( prefix ) )
      return localName;

    return prefix + ":" + localName; //$NON-NLS-1$
  }

  private String[] forceAdditionalSchemaPrefixes( final GMLWorkspace workspace )
  {
    final Collection<String> namespaces = new ArrayList<String>();

    // TODO: bug... this may cause too much namespaces to bee written into the gml-file
    // Either, we must only write what we really have, or
    // we should have another look at the import of substituting namespaces

    final IGMLSchema gmlSchema = workspace.getGMLSchema();

    // we may have additional schema, but no features using them (now)
    // We save these namespaces as prefixes, so if we reload the gml
    // the additional schema will also be loaded
    final IGMLSchema[] additionalSchemas = gmlSchema.getAdditionalSchemas();
    for( final IGMLSchema additionalSchema : additionalSchemas )
      namespaces.add( additionalSchema.getTargetNamespace() );

    return namespaces.toArray( new String[namespaces.size()] );
  }

  /**
   * Forces all known prefixes to be defined in the root element else we get to much prefix definitions later
   */
  private String[] forceFeatureTypePrefixes( final GMLWorkspace workspace )
  {
    final Collection<String> namespaces = new HashSet<String>();
    final IGMLSchema gmlSchema = workspace.getGMLSchema();

    final IFeatureType[] featureTypes = gmlSchema.getAllFeatureTypes();
    for( final IFeatureType featureType : featureTypes )
    {
      final QName qName = featureType.getQName();
      namespaces.add( qName.getNamespaceURI() );

      final IPropertyType[] properties = featureType.getProperties();
      for( final IPropertyType propertyType : properties )
      {
        if( !propertyType.isVirtual() )
          namespaces.add( propertyType.getQName().getNamespaceURI() );

        if( propertyType instanceof IValuePropertyType )
        {
          if( !propertyType.isVirtual() )
          {
            final QName valueQName = ((IValuePropertyType) propertyType).getValueQName();
            if( valueQName != null )
              namespaces.add( valueQName.getNamespaceURI() );
          }
        }
        else if( propertyType instanceof IRelationType )
        {
          if( ((IRelationType) propertyType).isInlineAble() )
          {
            final IFeatureType targetType = ((IRelationType) propertyType).getTargetFeatureType();
            namespaces.add( targetType.getQName().getNamespaceURI() );
          }
        }
      }
    }

    return namespaces.toArray( new String[namespaces.size()] );
  }

  private String getPreferedPrefix( final String namespace )
  {
    if( m_usedPrefixes.containsKey( namespace ) )
      return m_usedPrefixes.get( namespace );
    else
    {
      // Create new prefix and register it
      final String prefix = m_nsMapper.getPreferredPrefix( namespace, null );
      // FIXME: before: startPrefixMapping here
      m_usedPrefixes.put( namespace, prefix );
      return prefix;
    }
  }

  private QName getPrefixedQName( final QName qName )
  {
    final String uri = qName.getNamespaceURI();
    // Check if already registered; return the qname from the map, because qName is not prefixed
    final String prefix = getPreferedPrefix( uri );

    return new QName( qName.getNamespaceURI(), qName.getLocalPart(), prefix );
  }

  private void processFeature( final Feature feature, final AttributesImpl attributes ) throws SAXException
  {
    final ContentHandler contentHandler = m_reader.getContentHandler();

    final IFeatureType featureType = feature.getFeatureType();

    /* Add gml:id or fid attribute */
    final String id = feature.getId();
    if( id != null && id.length() > 0 )
    {
      final String version = featureType.getGMLSchema().getGMLVersion();
      final QName idQName = getPrefixedQName( GMLSchemaUtilities.getIdAttribute( version ) );
      addAttribute( attributes, idQName, "CDATA", id );
    }

    /* Write opening tag */
    final QName prefixedQName = getPrefixedQName( feature.getFeatureType().getQName() );
    final String localPart = prefixedQName.getLocalPart();
    final String uri = prefixedQName.getNamespaceURI();
    final String qname = elementQName( prefixedQName );
    contentHandler.startElement( uri, localPart, qname, attributes );

    /* Write properties */
    final IPropertyType[] properties = featureType.getProperties();
    for( final IPropertyType pt : properties )
    {
      // Virtual properties are properties which do not get serialized...
      if( pt.isVirtual() )
        continue;

      // REMARK: this only works for sequences of the property!
      // If the content of (i.e. the element itself has the maxOccurs > 1 this will not
      // work. In order to support this, we need a FeatureProperty object as value object (as in deegree2)
      final Object value = feature.getProperty( pt );
      if( pt.isList() )
      {
        final List< ? > values = (List< ? >) value;
        if( values != null )
        {
          /* FIXME: ConcurrentModificationExceptions happens sometimes here */
          for( final Object propertyValue : values )
            processProperty( pt, propertyValue );
        }
      }
      else
      {
        // If value == null && minOccurs == 0 do not write an element
        if( value != null || pt.getMinOccurs() > 0 )
          processProperty( pt, value );
      }
    }

    /* Write closing tag */
    contentHandler.endElement( uri, localPart, qname );
  }

  private String elementQName( final QName prefixedQName )
  {
    final String prefix = prefixedQName.getPrefix();
    if( prefix.isEmpty() )
      return prefixedQName.getLocalPart();

    return prefix + ":" + prefixedQName.getLocalPart();
  }

  /**
   * Writes one single property
   */
  private void processProperty( final IPropertyType pt, final Object propertyValue ) throws SAXException
  {
    final ContentHandler contentHandler = m_reader.getContentHandler();

    final QName name = pt.getQName();
    final QName prefixedQName = getPrefixedQName( name );
    final String uri = prefixedQName.getNamespaceURI();
    final String localPart = prefixedQName.getLocalPart();

    // Find attributes for the current property
    final Attributes atts = attributeForProperty( pt, propertyValue );

    /* Write starting tag */
    final String qname = elementQName( prefixedQName );
    contentHandler.startElement( uri, localPart, qname, atts );

    if( pt instanceof IRelationType )
    {
      // Write the feature as content. If it is a reference (i.e. no feature), nothing is written, as the href was
      // already set as an attribute
      if( propertyValue instanceof Feature && !(propertyValue instanceof XLinkedFeature_Impl) )
        processFeature( (Feature) propertyValue, new AttributesImpl() );
    }
    else if( pt instanceof IValuePropertyType )
      processValueType( (IValuePropertyType) pt, propertyValue, prefixedQName );
    else
      throw new UnsupportedOperationException();

    /* Write ending tag */
    contentHandler.endElement( uri, localPart, qname );
  }

  private Attributes attributeForProperty( final IPropertyType pt, final Object propertyValue ) throws SAXException
  {
    final AttributesImpl atts = new AttributesImpl();

    if( pt instanceof IRelationType )
    {
      final String href;
      if( propertyValue instanceof String )
        href = "#" + (String) propertyValue;
      else if( propertyValue instanceof XLinkedFeature_Impl )
        href = ((XLinkedFeature_Impl) propertyValue).getHref();
      else
        href = null;

      if( href != null )
        addAttribute( atts, m_xlinkAttributeName, "CDATA", href );
    }

    return atts;
  }

  private String printSimpleValue( final IValuePropertyType pt, final ISimpleMarshallingTypeHandler<Object> th, final Object propertyValue ) throws SAXException
  {
    if( pt.isFixed() )
      return pt.getFixed();

    if( propertyValue == null )
      return null;

    try
    {
      return th.convertToXMLString( propertyValue );
    }
    catch( final Exception e )
    {
      final String msg = String.format( "Could not convert value '%s' for property '%s'", propertyValue, pt.getQName() );
      throw new SAXException( msg, e );
    }
  }

  private void processValueType( final IValuePropertyType pt, final Object propertyValue, final QName prefixedQName ) throws SAXException
  {
    final IMarshallingTypeHandler th = pt.getTypeHandler();

    if( th instanceof ISimpleMarshallingTypeHandler< ? > )
    {
      final String xmlString = printSimpleValue( pt, (ISimpleMarshallingTypeHandler<Object>) th, propertyValue );
      if( xmlString != null )
      {
        // FIXME: this is the right place to write CDATA stuff, but of course now it is a wild hack
        // to look for a specific value. This must of course be decided in a more general way.
        // Maybe we register extensions for specific qnames?
        // TODO: also, it should be only done for String, i.e. in the XsdBaseTypeHandlerString
        final boolean doCData = prefixedQName.equals( new QName( NS.OM, "result" ) );
        final LexicalHandler lexicalHandler = doCData ? (LexicalHandler) m_reader.getProperty( "http://xml.org/sax/properties/lexical-handler" ) : null;
        if( doCData )
          lexicalHandler.startCDATA();

        m_reader.getContentHandler().characters( xmlString.toCharArray(), 0, xmlString.length() );

        if( doCData )
          lexicalHandler.endCDATA();
      }

      return;
    }

    if( propertyValue != null )
    {
      try
      {
        th.marshal( propertyValue, m_reader, null, m_gmlVersion );
      }
      catch( final Exception e )
      {
        // Catch any exception here: we should always continue to write data in order to minimise data loss here

        // TODO: we need an error handler! Else the user does not get any information about errors

        // TODO Distinguish between normal exceptions and SaxParseException
        final ErrorHandler errorHandler = m_reader.getErrorHandler();
        if( errorHandler == null )
          KalypsoDeegreePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
        else
          errorHandler.error( new SAXParseException( "Failed to write property: " + pt.getQName(), null, e ) );
      }
    }
  }
}
