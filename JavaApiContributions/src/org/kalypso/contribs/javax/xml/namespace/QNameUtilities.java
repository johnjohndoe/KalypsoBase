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

import java.util.ArrayList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * @author Gernot Belger
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
   * Tests if two qnames are equal in respect to their local part.
   */
  public static boolean equalsLocal( final QName name1, final QName name2 )
  {
    final String local1 = name1 == null ? null : name1.getLocalPart();
    final String local2 = name2 == null ? null : name2.getLocalPart();
    if( local1 == null )
      return local2 == null;

    return local1.equals( local2 );
  }

  /**
   * syntax of fragmentedFullQName :
   * 
   * <pre>
   *         &lt;namespace&gt;#&lt;localpart&gt;
   * </pre>
   * 
   * example: fragmentedFullQName = www.w3c.org#index.html<br/>
   * If no '#' is given, a qname with only a localPart is created.
   * 
   * @return qname from fragmentedFullQName
   */
  public static QName createQName( final String fragmentedFullQName )
  {
    final String[] parts = fragmentedFullQName.split( "#" );
    if( parts.length == 2 )
      return new QName( parts[0], parts[1] );

    return QName.valueOf( fragmentedFullQName );
  }

  public static QName[] createQNames( final String fragmentedFullQNameList, final String separator )
  {
    final ArrayList<QName> allQNames = new ArrayList<QName>();
    final String[] qNameStrings = fragmentedFullQNameList.split( separator );
    for( final String s : qNameStrings )
    {
      allQNames.add( createQName( s ) );
    }
    return allQNames.toArray( new QName[allQNames.size()] );
  }

  /**
   * Parses a {@link QName} from a string in xml-syntax using a prefix resolver. <br/> Syntax of the qname:
   * 
   * <pre>
   *    prefix:localPart
   * </pre>
   * 
   * <br/> For backwards compability, also the {@link QName#toString()} form (i.e. {namespace}localPart) form is
   * recognized.<br/> If either the prefix is empty or the namespaceContext is null, returns a qname with empty local
   * part (due to XML-Spec).
   */
  public static QName createQName( final String condition, final NamespaceContext namespaceContext )
  {
    // REMARK: First, check if it is formatted like {namespaceUri}localPart (which is not regular, but still recognized
    // for backwards compability
    if( condition.contains( "{" ) )
      return QName.valueOf( condition );

    final String[] split = condition.split( ":" );
    if( split.length == 0 || split.length > 2 )
      return null;

    if( split.length == 1 )
      return new QName( condition );

    final String prefix = split[0];
    final String localPart = split[1];

    if( prefix == null || prefix.length() == 0 || namespaceContext == null )
      return new QName( localPart );

    final String namespaceUri = namespaceContext.getNamespaceURI( prefix );
    return new QName( namespaceUri, localPart );
  }
}
