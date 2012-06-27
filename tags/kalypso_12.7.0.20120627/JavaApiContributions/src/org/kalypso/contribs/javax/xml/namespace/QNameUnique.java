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

import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * A {@link QName} implementation that guarantees that every qname only exists once.<br>
 * Needed for performance, these qname can be compared with == for greater speed-
 */
public class QNameUnique extends QName
{
  private static final Map<QNameUnique, QNameUnique> m_names = new HashMap<QNameUnique, QNameUnique>();

  public static synchronized QNameUnique create( final QNameUnique qname )
  {
    if( m_names.containsKey( qname ) )
      return m_names.get( qname );

    m_names.put( qname, qname );

    return qname;
  }

  public static QNameUnique create( final QName qname )
  {
    final QNameUnique qName = new QNameUnique( qname );
    return create( qName );
  }

  public static QNameUnique create( final String namespace, final String localPart )
  {
    final QNameUnique qName = new QNameUnique( namespace, localPart );
    return create( qName );
  }

  private QNameUnique( final String namespaceURI, final String localPart, final String prefix )
  {
    super( namespaceURI, localPart, prefix );
  }

  private QNameUnique( final String namespaceURI, final String localPart )
  {
    super( namespaceURI, localPart );
  }

  private QNameUnique( final String localPart )
  {
    super( localPart );
  }

  private QNameUnique( final QName qname )
  {
    super( qname.getNamespaceURI(), qname.getLocalPart(), qname.getPrefix() );
  }

  public QNameUnique asLocal( )
  {
    return QNameUnique.create( XMLConstants.NULL_NS_URI, getLocalPart() );
  }
}
