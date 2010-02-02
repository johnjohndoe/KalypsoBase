/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import org.kalypso.contribs.org.xml.sax.AppendingContentHandler;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.io.sax.parser.DelegatingContentHandler;
import org.kalypsodeegree_impl.model.feature.IFeatureProviderFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content handler that parses a response of a WFS request.
 * <p>
 * If the request is gml, all parsing is delegated to the {@link GMLContentHandler}.
 * </p>
 * <p>
 * If the request is an exception, justs logs the content into a string and throws it as an GmlException.
 * </p>
 * 
 * @author Gernot Belger
 * @author Andreas von D�mming
 * @author Felipe Maximino - Refaktoring
 */
public class GMLorExceptionContentHandler extends DelegatingContentHandler
{
  // TODO: where to put this constant?
  final QName QNAME_OWS_EXCEPTION_REPORT = new QName( "http://www.opengis.net/ows", "ExceptionReport" );
  
  private final XMLReader m_xmlReader;
  
  private final URL m_schemaLocationHint;
  
  private final URL m_context;
  
  private final IFeatureProviderFactory m_providerFactory;
  
  public GMLorExceptionContentHandler( final XMLReader xmlReader, final URL schemaLocationHint, final URL context, final IFeatureProviderFactory providerFactory )
  {
    super( xmlReader, null );

    m_xmlReader = xmlReader;
    m_schemaLocationHint = schemaLocationHint;
    m_context = context;
    m_providerFactory = providerFactory;
  }

  /**
   * If the first element of the document is the exception element, changes the delegate.
   * 
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
   *      org.xml.sax.Attributes)
   */
  @Override
  public void startElement( final String uri, final String localName, final String qName, final Attributes atts ) throws SAXException
  { 
    // handle OGC Exceptions
    // Handle degree1 + deegree2 exepctions.
    // deegree1-service: ...Exception
    // deegree2-service: ServiceExceptionReport
    // TODO: we should test for the namespace, what happens if we ever have a gml with root element 'exception'?
    final QName eltQName = new QName( uri, localName );
    if( localName.endsWith( "Exception" ) || localName.equals( "ServiceExceptionReport" ) )
    {
      delegate( new AppendingContentHandler( new StringBuffer() ) );
    }
    else if( eltQName.equals( QNAME_OWS_EXCEPTION_REPORT ) )
    {
      delegate( new AppendingContentHandler( new StringBuffer() ) );
    }
    else
    { 
      delegate( new GMLDocumentContentHandler( m_xmlReader, this, m_schemaLocationHint, m_context, m_providerFactory ) );
      m_delegate.startElement( uri, localName, qName, atts );
    }
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement( final String uri, final String localName, final String qName )
  {
    endDelegation();
  }

  public GMLWorkspace getWorkspace( ) throws GMLException
  {
    if( m_delegate instanceof AppendingContentHandler )
    {
      final Appendable buffer = ((AppendingContentHandler) m_delegate).getAppendable();
      throw new GMLException( buffer.toString() );
    }

    return ((IWorkspaceProvider) m_delegate).getWorkspace();
  }
}
