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
package org.kalypso.mapserver.servlets;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Platform;
import org.mortbay.servlet.CGI;

/**
 * A CGI servlet, which can be configured.
 * 
 * @author Holger Albert
 */
public class CGIServlet extends CGI
{
  /**
   * The constructor.
   */
  public CGIServlet( )
  {
  }

  /**
   * @see javax.servlet.GenericServlet#getInitParameter(java.lang.String)
   */
  @Override
  public String getInitParameter( String name )
  {
    /* If the parameter has a value, always return it. */
    String initParameter = super.getInitParameter( name );
    if( initParameter != null )
      return initParameter;

    /* If not (==null) return null, except if it is one of the following parameter. */
    if( !"cgibinResourceBase".equals( name ) && !"Path".equals( name ) && !"ENV_PROJ_LIB".equals( name ) )
      return null;

    try
    {
      URL cgibinResourceBase = Platform.getInstallLocation().getURL();
      File file = FileUtils.toFile( cgibinResourceBase );

      if( !"ENV_PROJ_LIB".equals( name ) )
        return file.toString();

      return new File( file, "cgi-bin" ).toString();

    }
    catch( Exception ex )
    {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * @see javax.servlet.GenericServlet#getInitParameterNames()
   */
  @Override
  public Enumeration< ? > getInitParameterNames( )
  {
    Set<String> results = new HashSet<String>();

    Enumeration< ? > initParameterNames = super.getInitParameterNames();
    while( initParameterNames.hasMoreElements() )
    {
      results.add( (String) initParameterNames.nextElement() );
    }

    if( !results.contains( "ENV_PROJ_LIB" ) )
      results.add( "ENV_PROJ_LIB" );

    return new IteratorEnumeration( results.iterator() );
  }
}