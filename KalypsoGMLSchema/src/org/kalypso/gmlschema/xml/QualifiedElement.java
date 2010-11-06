/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.gmlschema.xml;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.contribs.javax.xml.namespace.QNameUnique;
import org.kalypso.gmlschema.IGMLSchema;

/**
 * @author doemming
 */
public class QualifiedElement
{
  public static QName createQName( final IGMLSchema gmlSchema, final Element element )
  {
    final String targetNamespace = gmlSchema.getTargetNamespace();
    final String name = element.getName();
    return new QName( targetNamespace, name );
  }

  private final IGMLSchema m_gmlSchema;

  private final Element m_element;

  private final QNameUnique m_qName;

  private final QNameUnique m_localQName;

  public QualifiedElement( final IGMLSchema gmlSchema, final Element element, final QName qName )
  {
    m_gmlSchema = gmlSchema;
    m_element = element;

    m_qName = QNameUnique.create( qName );
    m_localQName = QNameUnique.create( XMLConstants.NULL_NS_URI, qName.getLocalPart() );
  }

  public final IGMLSchema getGMLSchema( )
  {
    return m_gmlSchema;
  }

  public QNameUnique getQName( )
  {
    return m_qName;
  }

  /**
   * Returns the {@link QName} of this element as localPart (i.e. namepspace is empty)
   */
  public QNameUnique getLocalQName( )
  {
    return m_localQName;
  }

  /**
   * @deprecated No more used, so we can delete it?
   */
  @Deprecated
  public String getName( )
  {
    return m_qName.getLocalPart();
  }

  public Element getElement( )
  {
    return m_element;
  }

  @Override
  public String toString( )
  {
    return m_qName.toString();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof QualifiedElement )
    {
      final QualifiedElement lEl = ((QualifiedElement) obj);
      return m_qName == lEl.m_qName;
    }

    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return m_qName.hashCode();
  }
}
