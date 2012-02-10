/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author doemming
 */
public class GMLWorkspaceReader implements XMLReader
{
  private final List<String> m_enabledFeatures = new ArrayList<String>();

  private final Map<String, Object> m_propMap = new HashMap<String, Object>();

  private EntityResolver m_entityResolver;

  private DTDHandler m_dtdHandler;

  private ContentHandler m_contentHandler;

  private ErrorHandler m_errorHandler;

  @Override
  public void parse( final InputSource input ) throws SAXException
  {
    if( input == null || !(input instanceof GMLWorkspaceInputSource) )
      throw new SAXException( "inputSource is null or not of type: " + GMLWorkspaceInputSource.class.getName() );

    final ContentHandler handler = getContentHandler();
    final GMLWorkspace workspace = ((GMLWorkspaceInputSource) input).getGMLWorkspace();

    final GMLSAXFactory factory = new GMLSAXFactory( this, workspace.getGMLSchema().getGMLVersion() );

    handler.startDocument();
    factory.process( workspace );
    handler.endDocument();
  }

  @Override
  public boolean getFeature( final String name )
  {
    return m_enabledFeatures.contains( name );
  }

  @Override
  public void setFeature( final String name, final boolean value )
  {
    if( value )
      m_enabledFeatures.add( name );
    else
      m_enabledFeatures.remove( name );
  }

  @Override
  public Object getProperty( final String name )
  {
    return m_propMap.get( name );
  }

  @Override
  public void setProperty( final String name, final Object value )
  {
    m_propMap.put( name, value );
  }

  @Override
  public void setEntityResolver( final EntityResolver resolver )
  {
    m_entityResolver = resolver;
  }

  @Override
  public EntityResolver getEntityResolver( )
  {
    return m_entityResolver;
  }

  @Override
  public void setDTDHandler( final DTDHandler handler )
  {
    m_dtdHandler = handler;
  }

  @Override
  public DTDHandler getDTDHandler( )
  {
    return m_dtdHandler;
  }

  @Override
  public void setContentHandler( final ContentHandler handler )
  {
    m_contentHandler = handler;
  }

  @Override
  public ContentHandler getContentHandler( )
  {
    return m_contentHandler;
  }

  @Override
  public void setErrorHandler( final ErrorHandler handler )
  {
    m_errorHandler = handler;
  }

  @Override
  public ErrorHandler getErrorHandler( )
  {
    return m_errorHandler;
  }

  @Override
  public void parse( final String systemId )
  {
    throw new UnsupportedOperationException();
  }
}
