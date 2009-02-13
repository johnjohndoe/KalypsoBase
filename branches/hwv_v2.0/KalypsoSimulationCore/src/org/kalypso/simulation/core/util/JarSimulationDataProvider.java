/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.activation.DataHandler;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.net.UrlUtilities;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.SimulationException;
import org.kalypso.simulation.core.internal.queued.ModelspecData;
import org.kalypso.simulation.core.simspec.DataType;

/**
 * @author belger
 */
public class JarSimulationDataProvider implements ISimulationDataProvider
{
  private final UrlUtilities m_urlUtitilies = new UrlUtilities();

  private final DataHandler m_zipHandler;

  private final Map<String, String> m_idhash;

  private URL m_baseURL;

  private File m_jarfile;

  private static final QName QNAME_ANY_URI = new QName( NS.XSD_SCHEMA, "anyURI" );

  private final ModelspecData m_modelspec;

  /**
   * @param modelspec
   *          The modelspec of the simulation. Used to determine the type of each input. If null, the default type is
   *          URL for backwards compability.
   */
  public JarSimulationDataProvider( final DataHandler zipHandler, final ModelspecData modelspec, final SimulationDataPath[] input )
  {
    m_zipHandler = zipHandler;
    m_modelspec = modelspec;

    // wir interessieren uns nur für id->pfad
    m_idhash = indexInput( input );
  }

  public void dispose( )
  {
    if( m_jarfile != null )
      m_jarfile.delete();

  }

  /** Parse the input data and put it into the hash. */
  private Map<String, String> indexInput( final SimulationDataPath[] input )
  {
    final Map<String, String> index = new HashMap<String, String>( input.length );
    for( final SimulationDataPath bean : input )
      index.put( bean.getId(), bean.getPath() );

    return index;
  }

  /**
   * @throws CalcJobServiceException
   * @see org.kalypso.services.calculation.job.ICalcDataProvider#getURLForID(java.lang.String)
   */
  public Object getInputForID( final String id ) throws SimulationException
  {
    final String path = m_idhash.get( id );
    if( path == null )
      throw new NoSuchElementException( "Eingabedaten nicht vorhanden mit ID: " + id );

    final DataType inputType = m_modelspec == null ? null : m_modelspec.getInput( id );
    final QName type = inputType == null ? QNAME_ANY_URI : inputType.getType();

    if( type.equals( QNAME_ANY_URI ) )
    {
      try
      {
        final URL baseURL = getBaseURL();
        return m_urlUtitilies.resolveURL( baseURL, path );
      }
      catch( final MalformedURLException e )
      {
        e.printStackTrace();

        throw new SimulationException( "Ungültiger Pfad in Eingangsdaten", e );
      }
      catch( final IOException e )
      {
        e.printStackTrace();

        throw new SimulationException( "Konnte Eingangsdaten nicht lesen", e );
      }
    }
    else if( type.getNamespaceURI().equals( NS.XSD_SCHEMA ) )
    {
      final String localPart = type.getLocalPart();
      // TODO: maybe better to use type registry to parse the values
      // draw back: this would introduce a dependency on KalypsoGMLSchema
      if( "string".equals( localPart ) )
        return DatatypeConverter.parseString( path );
      else if( "int".equals( localPart ) )
        DatatypeConverter.parseInt( path );
      else if( "double".equals( localPart ) )
        DatatypeConverter.parseDouble( path );
      else if( "boolean".equals( localPart ) )
        DatatypeConverter.parseBoolean( path );
    }

    throw new SimulationException( "Unbekannter Typ für 'path':" + type, null );
  }

  private URL getBaseURL( ) throws IOException
  {
    if( m_baseURL == null )
    {
      final File jarfile = getJarFile();
      if( jarfile == null )
        return null;

      m_baseURL = new URL( "jar:" + jarfile.toURL().toString() + "!/" );
    }

    return m_baseURL;
  }

  private File getJarFile( ) throws IOException
  {
    if( m_jarfile == null )
    {
      FileOutputStream jarstream = null;
      InputStream handlerStream = null;
      try
      {
        handlerStream = m_zipHandler.getInputStream();
        m_jarfile = File.createTempFile( "CalcJobInputData", ".jar" );
        m_jarfile.deleteOnExit();

        jarstream = new FileOutputStream( m_jarfile );
        IOUtils.copy( handlerStream, jarstream );
      }
      finally
      {
        IOUtils.closeQuietly( jarstream );
        IOUtils.closeQuietly( handlerStream );
      }
    }

    return m_jarfile;
  }

  /**
   * @see org.kalypso.services.calculation.job.ICalcDataProvider#hasID(java.lang.String)
   */
  public boolean hasID( final String id )
  {
    return m_idhash.containsKey( id );
  }
}
