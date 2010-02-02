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
import java.util.Map;

import org.kalypso.gmlschema.GMLSchemaLoaderWithLocalCache;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A {@link org.xml.sax.ContentHandler} implementation which parses GML fragment and produces a {@link Feature}
 * hierarchy from it.<br>
 * This content handler only delegates the parsing to a {@link org.kalypso.gml.FeatureContentHandler}
 *
 * @author Andreas von Doemming
 * @author Felipe Maximino
 */
public class GMLContentHandler extends DelegatingContentHandler implements IFeatureHandler, IRootFeatureProvider
{
  private final URL m_context;
 
  private final GMLSchemaLoaderWithLocalCache m_schemaLoader;
  
  private Feature m_rootFeature;  
  
  /**
   * @param schemaLocations
   *          If non-<code>null</code>, these locations will be used to load the corresponding schmema's (i.e. given to
   *          the catalog). If the schema is already cached or a location is registered, the location will probably
   *          ignored.
   */
  public GMLContentHandler( final XMLReader xmlReader, final ContentHandler parentContentHandler, final URL context, final GMLSchemaLoaderWithLocalCache schemaLoader )
  {
    super( xmlReader, parentContentHandler );
    
    m_context = context;
    
    m_schemaLoader = schemaLoader;
  }

  public GMLContentHandler( XMLReader xmlReader, final ContentHandler parentContentHandler, URL context, Map<String, URL> namespaces, Map<String, IGMLSchema> preFetchedSchemas )
  {
    // TODO Auto-generated constructor stub
    super( xmlReader, parentContentHandler );
    
    m_context = context;
    
    m_schemaLoader = new GMLSchemaLoaderWithLocalCache( namespaces, preFetchedSchemas );
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement( String uri, String localName, String qName )
  {
    endDelegation();
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement( String uri, String localName, String qName, Attributes atts ) throws SAXException
  {  
    delegate( new FeatureContentHandler ( m_xmlReader, this, m_schemaLoader, m_context ) );
    m_delegate.startElement( uri, localName, qName, atts );
  }
  
  public Feature getRootFeature( ) throws GMLException
  {
    if( m_rootFeature == null )
      throw new GMLException( "Could not load GML, Root-Feature was not created." );

    return m_rootFeature;
  }
  
  public void setRootFeature( final Feature rootFeature )
  {
    m_rootFeature = rootFeature;
  }

  public void handle( Feature feature )
  {
    setRootFeature( feature ); 
  }
}
