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
package org.kalypso.contribs.javax.xml.namespace;

import javax.xml.namespace.QName;

/**
 * Helper class for { }
 * 
 * @author belger
 */
public class QNameUtilities
{
  private QNameUtilities( )
  {
    // never instantiate
  }

  public static boolean equals( final QName qname, final String namespace, final String localPart )
  {
    return qname.equals( new QName( namespace, localPart ) );
  }

  /**
   * syntax of fragmentedFullQName :
   * 
   * <pre>
   *  &lt;namespace&gt;#&lt;localpart&gt;
   * <br>
   * </pre>
   * 
   * example: fragmentedFullQName = www.w3c.org#index.html
   * 
   * @return qname from fragmentedFullQName
   */
  public static QName createQName( String fragmentedFullQName )
  {
    final String[] parts = fragmentedFullQName.split( "#" );
    return new QName( parts[0], parts[1] );
  }
}
