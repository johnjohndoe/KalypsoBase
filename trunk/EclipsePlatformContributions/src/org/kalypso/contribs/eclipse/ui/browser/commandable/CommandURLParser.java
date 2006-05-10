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
package org.kalypso.contribs.eclipse.ui.browser.commandable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Parser to make a CommandURL from a string (e.g. from a html-link)
 * 
 * @author kuepfer
 */
public class CommandURLParser
{
  private boolean m_hasProtocol = false;

  private boolean m_isCommandUrl = false;

  private URL m_url;

  /**
   * Constructor that gets the URL to parse.
   */
  public CommandURLParser( String url )
  {
    // create a URL instance, and parser it for parameters.
    parseUrl( url );
  }

  private void parseUrl( String url )
  {
    if( url == null )
      return;
    m_url = null;
    try
    {
      m_url = new URL( url );
    }
    catch( MalformedURLException e )
    {
      // not a valid URL. set state.
      return;
    }

    if( m_url.getProtocol() != null )
    {
      // URL has some valid protocol. Check to see if it is an intro url.
      m_hasProtocol = true;
      m_isCommandUrl = isCommandUrl( m_url );
      return;
    }

    // not an Intro URL. do nothing.
    return;
  }

  /**
   * @return Returns the hasProtocol.
   */
  public boolean hasProtocol( )
  {
    return m_hasProtocol;
  }

  /**
   * @return Returns the isIntroUrl.
   */
  public boolean hasCommandUrl( )
  {
    return m_isCommandUrl;
  }

  /**
   * @return Returns the currebt url Protocol.
   */
  public String getProtocol( )
  {
    return m_url.getProtocol();
  }

  /**
   * @return Returns the current url Protocol.
   */
  public String getHost( )
  {
    return m_url.getHost();
  }

  /**
   * Checks to see if tha passed URL is an Browser URL. An browser URL is an http URL that has the intro plugin id as a
   * host. eg: "http://org.kalypso.portal.protocol/runAction".
   * 
   * @param url
   * @return true if url is an browser URL.
   */
  private boolean isCommandUrl( URL url )
  {
    if( !url.getProtocol().equalsIgnoreCase( CommandURL.BROWSER_PROTOCOL ) )
      // quick exit. If it is not http, url is not an browser url.
      return false;

    if( url.getHost().equalsIgnoreCase( CommandURL.BROWSER_HOST_ID ) )
      return true;

    return false;
  }

  /**
   * @return Returns the introURL. Will be null if the parsed URL is not an Intro URL.
   */
  public CommandURL getCommandURL( )
  {
    CommandURL browserURL = null;
    if( m_isCommandUrl )
    {
      // valid intro URL. Extract the action and parameters.
      String action = getPathAsAction( m_url );
      Properties parameters = getQueryParameters( m_url );

      // class instance vars are already populated by now.
      browserURL = new CommandURL( action, parameters );
    }
    return browserURL;
  }

  /**
   * Retruns the path attribute of the passed URL, stripped out of the leading "/". Returns null if the url does not
   * have a path.
   * 
   * @param url
   * @return
   */
  private String getPathAsAction( URL url )
  {
    // get possible action.
    String action = url.getPath();
    // remove leading "/" from path.
    if( action != null )
      action = action.substring( 1 );
    return action;
  }

  /**
   * Retruns the Query part of the URL as an instance of a Properties class.
   * 
   * @param url
   * @return
   */
  public Properties getQueryParameters( URL url )
  {
    // parser all query parameters.
    Properties properties = new Properties();
    String query = url.getQuery();
    if( query == null )
      // we do not have any parameters in this URL, return an empty
      // Properties instance.
      return properties;

    // now extract the key/value pairs from the query.
    String[] params = query.split( "&" ); //$NON-NLS-1$
    for( int i = 0; i < params.length; i++ )
    {
      // for every parameter, ie: key=value pair, create a property
      // entry. we know we have the key as the first string in the array,
      // and the value as the second array.
      String[] keyValuePair = params[i].split( "=" ); //$NON-NLS-1$
      if( keyValuePair.length != 2 )
      {
        // Log.warning("Ignoring the following Intro URL parameter: " //$NON-NLS-1$
        // + params[i]);
        continue;
      }
      properties.setProperty( keyValuePair[0], keyValuePair[1] );
    }
    return properties;
  }
}
