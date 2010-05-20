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
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.javax.xml.namespace.QNameUtilities;
import org.kalypso.gmlschema.GMLSchemaLoaderWithLocalCache;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * A {@link org.xml.sax.ContentHandler} implementation which parses GML Features. This content handler delegates the
 * parsing of any (non-feature) property-values of this feature to a {@link org.kalypso.gml.PropertyTypeHandler}s.
 * 
 * @author Andreas von Doemming
 * @author Felipe Maximino - Refaktoring
 */
public class FeatureContentHandler extends DelegatingContentHandler implements UnmarshallResultEater, IPropertyHandler
{
  private final URL m_context;

  private final GMLSchemaLoaderWithLocalCache m_schemaLoader;

  private Feature m_scopeFeature;

  private final Feature m_parentFeature;

  private IPropertyType m_scopeProperty;

  private final IFeatureHandler m_featureHandler;

  public FeatureContentHandler( final XMLReader xmlReader, final IFeatureHandler parentContentHandler, final GMLSchemaLoaderWithLocalCache schemaLoader, final URL context )
  {
    this( xmlReader, parentContentHandler, schemaLoader, context, null, null );
  }

  public FeatureContentHandler( final XMLReader xmlReader, final IFeatureHandler featureHandler, final GMLSchemaLoaderWithLocalCache schemaLoader, final URL context, final IPropertyType scopeProperty, final Feature parentFeature )
  {
    super( xmlReader, featureHandler );

    m_context = context;
    m_schemaLoader = schemaLoader;
    m_scopeFeature = null;
    m_parentFeature = parentFeature;
    m_scopeProperty = scopeProperty;
    m_featureHandler = featureHandler;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#endElement(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void endElement( final String uri, final String localName, final String qName ) throws SAXException
  {
    if( m_scopeFeature == null )
    {
      endDelegation();
      m_parentContentHandler.endElement( uri, localName, qName );
    }
    else if( QNameUtilities.equals( m_scopeFeature.getFeatureType().getQName(), uri, localName ) )
    {
      m_featureHandler.handle( m_scopeFeature );
      endDelegation();
      m_scopeFeature = null;
    }
    else
    {
      throw new SAXParseException( String.format( "Unexpected end element: {%s}%s = %s - should be {%s}%s", uri, localName, qName, m_scopeFeature.getQualifiedName().getNamespaceURI(), m_scopeFeature.getQualifiedName().getLocalPart() ), m_locator );
    }
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement( final String uri, final String localName, final String qName, final Attributes atts ) throws SAXException
  {
    final QName qname = new QName( uri, localName );

    /* each FeatureContentHandler parses one feature. So, if it already has a feature, what comes must be a property. */
    if( m_scopeFeature == null )
    {
      startFeature( qname, atts );
      delegate( new PropertyContentHandler( m_xmlReader, this, m_schemaLoader, m_context, m_scopeFeature ) );
    }
    else
    {
      delegate( new PropertyContentHandler( m_xmlReader, this, m_schemaLoader, m_context, m_scopeFeature ) );
      m_delegate.startElement( uri, localName, qName, atts );
    }
  }

  /**
   * @see org.kalypso.gmlschema.types.UnmarshallResultEater#unmarshallSuccesful(java.lang.Object)
   */
  @Override
  public void unmarshallSuccesful( final Object value )
  {
    // TODO: is this always correct? What about empty elements of list properties?
    // what about simple content properties with no value? e.g: <correction_ks/>
    if( value == null )
      return;

    if( m_scopeProperty.isList() )
    {
      final List<Object> list = (List<Object>) m_scopeFeature.getProperty( m_scopeProperty );
      list.add( value );
    }
    else
    {
      m_scopeFeature.setProperty( m_scopeProperty, value );
    }
  }

  private void startFeature( final QName qname, final Attributes atts ) throws SAXException
  {
    /* Root feature or new sub-feature. */
    final IGMLSchema schema = m_schemaLoader.findSchema( qname.getNamespaceURI() );

    final IFeatureType featureType = schema.getFeatureType( qname );
    if( featureType == null )
      throw new SAXException( "No feature type found for: " + qname );

    final String fid = idFromAttributes( atts );

    final Feature childFE = FeatureFactory.createFeature( m_parentFeature, (IRelationType) m_scopeProperty, fid, featureType, false );
    if( m_scopeFeature != null )
    {
      FeatureHelper.addChild( m_scopeFeature, (IRelationType) m_scopeProperty, childFE );
    }

    m_scopeFeature = childFE;
    m_scopeProperty = null;
  }

  private String idFromAttributes( final Attributes atts )
  {
    final int id1 = atts.getIndex( NS.GML2, "fid" );
    if( id1 != -1 )
      return atts.getValue( id1 );

    final int id2 = atts.getIndex( "fid" );
    if( id2 != -1 )
      return atts.getValue( id2 );

    final int id3 = atts.getIndex( NS.GML2, "id" );
    if( id3 != -1 )
      return atts.getValue( id3 );

    final int id4 = atts.getIndex( "id" );
    if( id4 != -1 )
      return atts.getValue( id4 );

    return null;
  }

  @Override
  public void setPropertyAsScope( final IPropertyType property )
  {
    m_scopeProperty = property;
  }
}
