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

import java.net.URL;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.javax.xml.namespace.QNameUtilities;
import org.kalypso.contribs.org.xml.sax.AttributesUtilities;
import org.kalypso.gmlschema.GMLSchemaLoaderWithLocalCache;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler2;
import org.kalypso.gmlschema.types.ISimpleMarshallingTypeHandler;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A {@link org.xml.sax.ContentHandler} implementation which parses GML properties.
 * 
 * This content handler delegates the parsing of any the property to its proper contentHandler.
 * 
 * @author Andreas von Doemming
 * @author Felipe Maximino - Refaktoring
 */
public class PropertyContentHandler extends DelegatingContentHandler implements UnmarshallResultEater, IFeatureHandler, ISimpleContentHandler, IPropertyHandler
{
  /* the feature that this property */
  private final Feature m_scopeFeature;
  
  private final IPropertyHandler m_propertyHandler;
  
  private final URL m_context;
  
  private final GMLSchemaLoaderWithLocalCache m_schemaLoader;
  
  private IPropertyType m_scopeProperty;
  
  public PropertyContentHandler( final XMLReader xmlReader, final IPropertyHandler propertyHandler, final GMLSchemaLoaderWithLocalCache schemaLoader, final URL context, final Feature parentFeature )
  {
    super( xmlReader, propertyHandler );
    
    m_scopeFeature = parentFeature;
    m_propertyHandler = propertyHandler;
    m_schemaLoader = schemaLoader;
    m_context = context;
    
    m_scopeProperty = null;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement( String uri, String localName, String qName ) throws SAXException
  {
    if( m_scopeProperty != null && QNameUtilities.equals( m_scopeProperty.getQName(), uri, localName ) )
    {
      m_propertyHandler.setPropertyAsScope( null );
      endDelegation();
    }
    else if( propertyHasAlreadyEnded() )
    {
      endDelegation();
      m_parentContentHandler.endElement( uri, localName, qName );
    }    
    else
    { 
      throw new SAXParseException( String.format( "Unexpected end element: {%s}%s = %s - should be {%s}%s", uri, localName, qName, m_scopeProperty.getQName().getNamespaceURI(), m_scopeProperty.getQName().getLocalPart() ), m_locator );
    }
  }

  private boolean propertyHasAlreadyEnded( )
  {
    if( m_scopeProperty == null ) // property was empty
      return true;    
    
    /* HACK: if we set a ValuePropertyContentHandler(JAXB binding) as delegate, the end tag of this property maybe was
     * already consumed during binding, maybe not. We should be able to verify if the end tag was correctly ended during binding. 
     */
    if( m_scopeProperty instanceof IValuePropertyType )
    {
      IValuePropertyType vpt = (IValuePropertyType) m_scopeProperty;
      
      if( vpt.getTypeHandler() instanceof IMarshallingTypeHandler )
        return true;
    }
    
    return false;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement( String uri, String localName, String qName, Attributes atts ) throws SAXException
  {    
    if(m_scopeProperty == null)
    {
      startProperty(uri, localName, qName, atts);
    }
    // HACK: this should not be needed (properties inside properties). But there are old gml Tin_Result files
    // that were wrong written, i.e, with gml properties inside another gml property.
    else
    { 
      delegate( new PropertyContentHandler( m_xmlReader, this, m_schemaLoader, m_context, m_scopeFeature ) );
      m_delegate.startElement( uri, localName, qName, atts );
    }
  }
  
  private void startProperty( String uri, String localName, String qName, Attributes atts ) throws SAXException
  {
    final QName qname = new QName( uri, localName );
    
    final Feature feature = m_scopeFeature;
    final IFeatureType featureType = feature.getFeatureType();
    final IPropertyType pt = featureType.getProperty( qname );
    if( pt == null )
    {
      final String msg = String.format( "Found unknwon property '%s' for FeatureType '%s'", qname, featureType.getQName() );
      throw new SAXException( msg );
    }

    /* Go into scope with that property */
    m_scopeProperty = pt;
    m_propertyHandler.setPropertyAsScope( m_scopeProperty );

    delegateToProperContentHandler( pt, uri, localName, qName, atts );    
  }

  private void delegateToProperContentHandler( final IPropertyType pt, final String uri, final String localName, final String qName, final Attributes atts ) throws SAXException
  { 
    if( pt instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType) pt;
      if( vpt.getTypeHandler() instanceof ISimpleMarshallingTypeHandler )
      {
        delegate( new SimpleContentContentHandler( m_xmlReader, this ) );
      }
      else
      { 
        delegateToValuePropertyContentHandler( vpt, uri, localName, qName, atts );
      }
    }
    else if( pt instanceof IRelationType )
    {
      delegateToRelationTypeContentHandler( uri, localName, qName, atts );      
    }
    else
      /* Should never happen. either its a value or a relation. */
      throw new SAXException( "Unknown IPropertyType instance: " + pt );
  }

  private void delegateToRelationTypeContentHandler( final String uri, final String localName, final String qName, final Attributes atts ) throws SAXException
  {
    final String href = AttributesUtilities.getAttributeValue( atts, NS.XLINK, "href", null );
    
    if( href != null )// its a xlink
    { 
      delegate( new XLinkedFeatureContentHandler ( m_xmlReader, this, m_scopeFeature, ( IRelationType ) m_scopeProperty ) );
      m_delegate.startElement( uri, localName, qName, atts );
    }
    else //its another feature
    {
      delegate( new FeatureContentHandler( m_xmlReader, this, m_schemaLoader, m_context, m_scopeProperty, m_scopeFeature ) );
    }
  }

  private void delegateToValuePropertyContentHandler( final IValuePropertyType pt, final String uri, final String localName, final String qName, final Attributes atts ) throws SAXException
  {
    final IMarshallingTypeHandler typeHandler = pt.getTypeHandler();

    if( typeHandler instanceof IMarshallingTypeHandler2 )
    {
      final IMarshallingTypeHandler2 th2 = (IMarshallingTypeHandler2) typeHandler;
      delegate( th2.createContentHandler( m_xmlReader, this, this, uri, localName, qName, atts ) );
    }
    else //else is a IMarshallingTypeHandler
    {
      delegate( new ValuePropertyContentHandler( m_xmlReader, this, typeHandler, m_schemaLoader, m_scopeProperty, m_context ) );
      m_delegate.startElement( uri, localName, qName, atts );      
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.UnmarshallResultEater#unmarshallSuccesful(java.lang.Object)
   */
  @Override
  public void unmarshallSuccesful( Object value ) throws SAXParseException
  { 
    ( ( UnmarshallResultEater )m_propertyHandler ).unmarshallSuccesful( value );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.IGMLElementHandler#handle(java.lang.Object)
   */
  @Override
  public void handle( StringBuffer simpleContent ) throws SAXParseException
  {
    final IValuePropertyType valuePT = (IValuePropertyType) m_scopeProperty;
    final ISimpleMarshallingTypeHandler< ? > simpleHandler = (ISimpleMarshallingTypeHandler< ? >) valuePT.getTypeHandler();
    final String simpleString = simpleContent.toString();
    
    final Object value = simpleHandler.convertToJavaValue( simpleString );    
    try
    {
      unmarshallSuccesful( value );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final String msg = String.format( "Failed to parsed simple type. Content was '%s' for property '%s'", simpleString, m_scopeProperty.getQName() );      
      throw new SAXParseException( msg, getLocator(), e );
    }
    finally
    {
      simpleContent = null;
    }
  }
  
  public void handle( Feature feature ) throws SAXParseException
  {
    unmarshallSuccesful( feature );
  }
  

  /**
   * @see org.kalypso.gml.IPropertyHandler#setPropertyAsScope(org.kalypso.gmlschema.property.IPropertyType)
   */
  @Override
  public void setPropertyAsScope( IPropertyType property )
  {
    m_propertyHandler.setPropertyAsScope( property );    
  }
}
