/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/exse/
 lat/lon Fitzke/Fretter/Poth GbR
 http://www.lat-lon.de

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

 Andreas Poth
 lat/lon Fitzke/Fretter/Poth GbR
 Meckenheimer Allee 176
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Jens Fitzke
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: jens.fitzke@uni-bonn.de

 
 ---------------------------------------------------------------------------*/

// $Id$
package org.deegree_impl.enterprise.control;

// JDK 1.3
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.deegree.enterprise.control.RequestUser;

/**
 * Encapsulates all client information.
 * <P>
 * 
 * @author <a href="mailto:friebe@gmx.net">Torsten Friebe </a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * 
 * @version $Revision$ $Date$
 */
public class RequestUser_Impl implements RequestUser
{
  private Properties userData;

  /**
   * Creates an empty object.
   */
  protected RequestUser_Impl()
  {
    this.userData = new Properties();
  }

  /**
   * Creates a request user object with client information retrieved out of the
   * request object.
   * 
   * @param request
   *          the request object containing user and client data
   */
  public RequestUser_Impl( HttpServletRequest request )
  {
    this.userData = new Properties();
    this.parseRequest( request );
  }

  /**
   * Remote user
   */
  public String getRemoteUser()
  {
    return this.userData.getProperty( "RemoteUser" );
  }

  /**
   * Remote address
   */
  public String getRemoteAddr()
  {
    return this.userData.getProperty( "RemoteAddr" );
  }

  /**
   * Remote host
   */
  public String getRemoteHost()
  {
    return this.userData.getProperty( "RemoteHost" );
  }

  /**
   * Authorization scheme
   */
  public String getAuthType()
  {
    return this.userData.getProperty( "AuthType" );
  }

  /**
   * Authenticated user
   */
  public String getUserPrincipal()
  {
    Object _obj = userData.get( "UserPrincipal" );

    if( _obj instanceof java.security.Principal )
    {
      java.security.Principal _principal = (java.security.Principal)_obj;
      return _principal.getName();
    }
    else if( _obj instanceof String )
    {
      return (String)_obj;
    }

    return _obj.toString();
  }

  /**
   * Parse request object for user specific attributes.
   */
  protected void parseRequest( HttpServletRequest request )
  {
    try
    {
      this.userData.setProperty( "RemoteUser", (String)getRequestValue( request, "getRemoteUser",
          "[unknown]" ) );

      this.userData.setProperty( "RemoteAddr", (String)getRequestValue( request, "getRemoteAddr",
          "[unknown]" ) );

      this.userData.setProperty( "RemoteHost", (String)getRequestValue( request, "getRemoteHost",
          "[unknown]" ) );

      this.userData.setProperty( "AuthType", (String)getRequestValue( request, "getAuthType",
          "[unknown]" ) );

      this.userData.put( "UserPrincipal",
          getRequestValue( request, "getUserPrincipal", "[unknown]" ) );
    }
    catch( Exception ex )
    {
      ex.printStackTrace();
    }
  }

  /**
   * 
   * 
   * @param request
   * @param methodName
   * @param defaultValue
   * 
   * @return @throws
   *         NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  protected Object getRequestValue( HttpServletRequest request, String methodName,
      Object defaultValue ) throws NoSuchMethodException, InvocationTargetException,
      IllegalAccessException
  {
    if( ( request != null ) && ( methodName != null ) && !methodName.equals( "" ) )
    {
      //      System.err.println( "looking for :" + methodName );
      // use refection for method
      Method _objmethod = request.getClass().getMethod( methodName, null );

      //      System.err.println( "got :" + _objmethod.getName() );
      // get the result of the method invocation
      Object _result = _objmethod.invoke( request, null );

      //      System.err.println( "returns :" + _result );
      if( _result != null )
      {
        return _result;
      }
      return defaultValue;
    }

    return null;
  }
}