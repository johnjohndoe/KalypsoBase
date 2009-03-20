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
package org.kalypso.simulation.core.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.apache.commons.httpclient.util.URIUtil;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.internal.queued.ModelspecData;
import org.kalypso.simulation.core.simspec.DataType;

/**
 * @author kurzbach
 */
public abstract class AbstractSimulationDataProvider implements ISimulationDataProvider
{
  private final Map<String, String> m_idhash;

  private static final QName QNAME_ANY_URI = new QName( NS.XSD_SCHEMA, "anyURI" );

  protected final ModelspecData m_modelspec;

  public AbstractSimulationDataProvider( final ModelspecData modelspec, final SimulationDataPath[] input )
  {
    m_modelspec = modelspec;

    // wir interessieren uns nur f¸r id->pfad
    m_idhash = indexInput( input );
  }

  protected Map<String, String> indexInput( final SimulationDataPath[] input )
  {
    final Map<String, String> index = new HashMap<String, String>( input.length );
    for( final SimulationDataPath bean : input )
    {
      index.put( bean.getId(), bean.getPath() );
    }

    return index;
  }

  public boolean hasID( final String id )
  {
    return m_idhash.containsKey( id );
  }

  public Object getInputForID( final String id ) throws SimulationException
  {
    final String path = m_idhash.get( id );
    if( path == null )
      throw new NoSuchElementException( "Eingabedaten nicht vorhanden mit ID: " + id );

    final DataType inputType = m_modelspec == null ? null : m_modelspec.getInput( id );

    // default to xs:anyURI if no type is given
    final QName type = inputType == null ? QNAME_ANY_URI : inputType.getType();

    // URI types are treated as URLs,
    if( type.equals( QNAME_ANY_URI ) )
    {
      try
      {
        final URI baseURL = getBaseURL().toURI();
        final URI relativeURI = baseURL.resolve( URIUtil.encodePath( path ) );

        // try to silently convert the URI to a URL
        try
        {
          final URL url = relativeURI.toURL();
          return url;
        }
        catch( final MalformedURLException e )
        {
          // gobble
        }
        return relativeURI;
      }
      catch( final IOException e )
      {
        throw new SimulationException( "Problem reading input data.", e );
      }
      catch( final URISyntaxException e )
      {
        throw new SimulationException( "URI input does not contain a valid URI.", e );
      }
    }
    else
    {
      final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();
      final IMarshallingTypeHandler handler = typeRegistry.getTypeHandlerForTypeName( type );
      if( handler != null )
      {
        try
        {
          return handler.parseType( path );
        }
        catch( final ParseException e )
        {
          throw new SimulationException( "Could not parse " + path + " as an object of type " + type, e );
        }
      }
    }

    throw new SimulationException( "Unknown type " + type, null );
  }

  /**
   *
   */
  protected abstract URL getBaseURL( ) throws IOException;

  /**
   * This method does nothing. Override if needed
   * 
   * @see org.kalypso.simulation.core.ISimulationDataProvider#dispose()
   */
  public void dispose( )
  {
  }
}