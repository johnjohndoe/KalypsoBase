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
package org.kalypso.contribs.java.net;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * A configurable <code>URLStreamHandlerFactory</code>. You can set various <code>URLStreamHandler</code> that match
 * specific protocols.
 *
 * @author schlienger
 */
public class ConfigurableURLStreamHandlerFactory implements URLStreamHandlerFactory
{
  final Map<String, URLStreamHandler> m_map = new HashMap<>();

  public void setHandler( final String protocol, final URLStreamHandler handler )
  {
    m_map.put( protocol, handler );
  }

  /**
   * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
   */
  @Override
  public URLStreamHandler createURLStreamHandler( final String protocol )
  {
    if( !m_map.containsKey( protocol ) )
      return null; // let default Java implementation decide what to do...

    final URLStreamHandler handler = m_map.get( protocol );
    return handler;
  }
}