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
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.IFeatureProviderFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This {@link ContentHandler} implementation parsed a full gml-document.
 * <p>
 * This handler only parses the first line from a gml document and creates the appropriate gml schema from that.
 * <p>
 * All the rest of parsing is delegated to the {@link GMLContentHandler} content handler.
 * </p>
 * 
 * @author Gernot Belger
 * @author Felipe Maximino - Refactoring
 */
public class GMLDocumentContentHandler extends DelegatingContentHandler implements IWorkspaceProvider
{
  private final XMLReader m_xmlReader;

  private final URL m_schemaLocationHint;

  private final URL m_context;

  private final IFeatureProviderFactory m_providerFactory;

  private String m_schemaLocationString;

  /** Schema of root feature */
  private IGMLSchema m_rootSchema;

  public GMLDocumentContentHandler( final XMLReader xmlReader, final ContentHandler parentContentHandler, final URL schemaLocationHint, final URL context, final IFeatureProviderFactory providerFactory )
  {
    super( xmlReader, parentContentHandler );

    m_xmlReader = xmlReader;
    m_schemaLocationHint = schemaLocationHint;
    m_context = context;
    m_providerFactory = providerFactory;

    m_schemaLocationString = null;
    m_rootSchema = null;
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#endElement(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void endElement( final String uri, final String localName, final String qName )
  {
    endDelegation();
  }

  /**
   * @see org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement( final String uri, final String localName, final String qName, final Attributes atts ) throws SAXException
  {
    final GMLSchemaLoaderWithLocalCache schemaLoader = new GMLSchemaLoaderWithLocalCache();
    loadDocumentSchema( schemaLoader, uri, atts );

    delegate( new GMLContentHandler( m_xmlReader, this, m_context, schemaLoader ) );
    m_delegate.startElement( uri, localName, qName, atts );
  }

  private void loadDocumentSchema( final GMLSchemaLoaderWithLocalCache schemaLoader, final String uri, final Attributes atts ) throws SAXException
  {
    // first element may have schema-location
    m_schemaLocationString = GMLSchemaUtilities.getSchemaLocation( atts );

    m_rootSchema = schemaLoader.loadMainSchema( uri, atts, m_schemaLocationString, m_schemaLocationHint, m_context );

    final Map<String, URL> namespaces = GMLSchemaUtilities.parseSchemaLocation( m_schemaLocationString, m_context );
    /* If a localtionHint is given, this precedes any schemaLocation in the GML-File */
    if( m_schemaLocationHint != null )
      namespaces.put( uri, m_schemaLocationHint );

    schemaLoader.setSchemaLocation( namespaces );
  }

  public GMLWorkspace getWorkspace( ) throws GMLException
  {
    final Feature rootFeature = ((IRootFeatureProvider) m_delegate).getRootFeature();

    return FeatureFactory.createGMLWorkspace( m_rootSchema, rootFeature, m_context, m_schemaLocationString, m_providerFactory, null );
  }
}
