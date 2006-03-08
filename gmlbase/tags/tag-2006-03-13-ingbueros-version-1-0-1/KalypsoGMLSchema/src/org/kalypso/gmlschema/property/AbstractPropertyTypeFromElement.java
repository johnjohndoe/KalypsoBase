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
package org.kalypso.gmlschema.property;

import java.io.StringWriter;
import java.math.BigInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.basics.QualifiedElement;

/**
 * Property feature type that is build from a xml-element
 * 
 * @author doemming
 */
public abstract class AbstractPropertyTypeFromElement extends QualifiedElement implements IPropertyType
{

  public AbstractPropertyTypeFromElement( GMLSchema gmlSchema, Element element )
  {
    super( gmlSchema, element );
  }

  public final int getMinOccurs( )
  {
    if( !m_element.isSetMinOccurs() )
      return 1;
    final BigInteger minOccurs = m_element.getMinOccurs();
    return minOccurs.intValue();
  }

  public final int getMaxOccurs( )
  {
    if( !m_element.isSetMaxOccurs() )
      return 1;
    final Object maxOccurs = m_element.getMaxOccurs();
    if( maxOccurs instanceof Number )
      return ((Number) maxOccurs).intValue();
    if( "unbounded".equals( maxOccurs ) )
      return IPropertyType.UNBOUND_OCCURENCY;
    throw new UnsupportedOperationException( "unknown occurency in schema: " + maxOccurs.toString() );
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isList()
   */
  public boolean isList( )
  {
    return getMaxOccurs() > 1 || getMaxOccurs() == UNBOUND_OCCURENCY;
  }

  public void toStringTree( StringWriter writer, int indent )
  {
    final int maxOccurs = getMaxOccurs();
    final int minOccurs = getMinOccurs();
    writer.write( "\n" + StringUtils.repeat( " ", indent ) );
    writer.write( " [" + minOccurs + "-" );
    if( maxOccurs == IPropertyType.UNBOUND_OCCURENCY )
      writer.write( "unbounded" );
    else
      writer.write( Integer.toString( maxOccurs ) );
    writer.write( "]" );
  }
}
