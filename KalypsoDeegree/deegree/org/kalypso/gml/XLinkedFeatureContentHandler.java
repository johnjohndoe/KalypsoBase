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

import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.org.xml.sax.AttributesUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.AbstractGmlContentHandler;
import org.kalypso.gmlschema.types.IGmlContentHandler;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.XLinkedFeature_Impl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A {@link org.xml.sax.ContentHandler} implementation which parses xlinked features
 * 
 * @author Andreas von Doemming
 * @author Felipe Maximino - Refaktoring
 */
public class XLinkedFeatureContentHandler extends AbstractGmlContentHandler
{
  private final Feature m_scopeFeature;

  private final IRelationType m_scopeProperty;

  public XLinkedFeatureContentHandler( final XMLReader reader, final IGmlContentHandler parentContentHandler, final Feature scopeFeature, final IRelationType scopeProperty )
  {
    super( reader, parentContentHandler );

    m_scopeFeature = scopeFeature;
    m_scopeProperty = scopeProperty;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement( final String uri, final String localName, final String qName ) throws SAXException
  {
    activateParent();
    getParentContentHandler().endElement( uri, localName, qName );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement( final String uri, final String localName, final String qName, final Attributes atts )
  {
    final String href = AttributesUtilities.getAttributeValue( atts, NS.XLINK, "href", null );

    // REMARK: for backwards compability, we still set the href as property-value
    // for internal links. This should be changed soon...
    if( href.startsWith( "#" ) )
    {
      final String refID2 = href.substring( 1 );
      FeatureHelper.addChild( m_scopeFeature, m_scopeProperty, refID2 );
    }
    else
    {
      final String role = AttributesUtilities.getAttributeValue( atts, NS.XLINK, "role", null );
      final String arcrole = AttributesUtilities.getAttributeValue( atts, NS.XLINK, "arcrole", null );
      final String title = AttributesUtilities.getAttributeValue( atts, NS.XLINK, "title", null );
      final String show = AttributesUtilities.getAttributeValue( atts, NS.XLINK, "show", "replace" );
      final String actuate = AttributesUtilities.getAttributeValue( atts, NS.XLINK, "actuate", "onRequest" );

      final IFeatureType targetFeatureType = m_scopeProperty.getTargetFeatureType();
      final Feature childFeature = new XLinkedFeature_Impl( m_scopeFeature, m_scopeProperty, targetFeatureType, href, role, arcrole, title, show, actuate );
      FeatureHelper.addChild( m_scopeFeature, m_scopeProperty, childFeature );
    }    
  }
}
