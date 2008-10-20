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
package org.kalypso.service.wps.client.simulation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.wps.client.WPSRequest;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.simulation.core.simspec.Modeldata.Input;
import org.kalypso.simulation.core.simspec.Modeldata.Output;

/**
 * Provides functions, that should help handling simulations with the WPS.
 * 
 * @author Holger Albert
 */
public class SimulationUtilities
{
  /**
   * The constructor.
   */
  private SimulationUtilities( )
  {
  }

  /**
   * This function checks, if the path given is a remote URL.
   * 
   * @return An URL, if the path is an URL (and additionally no file URL), otherwise null.
   */
  public static URL isURL( String path )
  {
    try
    {
      /* Check if it is an real URL. */
      URL url = new URL( path );

      /* If it is a file URL, it is no remote URL. */
      if( url.getProtocol().equals( "file" ) )
      {
        Debug.println( "Input '" + path + "' was a local file URL ..." );
        return null;
      }

      /* It is a remote URL. */
      Debug.println( "Input '" + path + "' was a remote URL ..." );

      return url;
    }
    catch( MalformedURLException e )
    {
      /* No need to give feedback on this error, because it was expected, if the path was no real URL. */
    }

    /* If it was no parseable URL, it is probably a file path. */
    Debug.println( "Input '" + path + "' was no URL ..." );

    return null;
  }

  /**
   * Reads the contents of an URL into an object suitable as value for the ComplexValueType.
   * 
   * @param url
   *          The url.
   * @return The contents of the URL as hex string.
   */
  public static Object toComplexValueContent( URL url ) throws IOException
  {
    byte[] bytes = UrlUtilities.toByteArray( url );
    return DatatypeConverter.printHexBinary( bytes );
  }

  /**
   * This function returns the default service endpoint, if defined. Otherwise it throws an exception.
   * 
   * @return The default service endpoint.
   */
  public static String getDefaultServiceEndpoint( ) throws CoreException
  {
    String endPoint = System.getProperty( WPSRequest.SYSTEM_PROP_WPS_ENDPOINT );
    if( endPoint == null )
      throw new CoreException( StatusUtilities.createStatus( IStatus.ERROR, "No default service endpoint is set. Use property '" + WPSRequest.SYSTEM_PROP_WPS_ENDPOINT
          + "' to define a default WPS-Endpoint.", null ) );

    return endPoint;
  }

  /**
   * This function searches an input in the model data.
   * 
   * @param ID
   *          The ID of the input to search for.
   * @param inputList
   *          The input of the model data.
   * @return The found input or null, if none is found for that ID.
   */
  public static Input findInput( String ID, List<Input> inputList )
  {
    for( int i = 0; i < inputList.size(); i++ )
    {
      Input input = inputList.get( i );
      if( input.getId().equals( ID ) )
        return input;
    }

    return null;
  }

  /**
   * This function searches an output out in the model data.
   * 
   * @param ID
   *          The ID of the output to search for.
   * @param outputList
   *          The output of the model data.
   * @return The found output or null, if none is found for that ID.
   */
  public static Output findOutput( String ID, List<Output> outputList )
  {
    for( int i = 0; i < outputList.size(); i++ )
    {
      Output output = outputList.get( i );
      if( output.getId().equals( ID ) )
        return output;
    }

    return null;
  }

  /**
   * This function returns the protocol of a given path or null.
   * 
   * @return The protocol of the given path or null.
   */
  public static String getProtocol( String path )
  {
    int index = path.indexOf( ':' );
    if( index == -1 )
      return null;

    return path.substring( 0, index );
  }
}