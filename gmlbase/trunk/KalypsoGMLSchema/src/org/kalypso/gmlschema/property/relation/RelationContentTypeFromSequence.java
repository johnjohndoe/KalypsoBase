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
package org.kalypso.gmlschema.property.relation;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.AttributeGroupRef;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.ElementWithOccurs;
import org.kalypso.gmlschema.GMLSchema;

/**
 * representation of a feature content definition from a xml schema sequence fragment.
 * 
 * @author doemming
 */
public class RelationContentTypeFromSequence extends RelationContentType
{
  private final List<ElementWithOccurs> m_elements;

  public RelationContentTypeFromSequence( GMLSchema gmlSchema, ComplexType complexType, List<ElementWithOccurs> elements )
  {
    super( gmlSchema, complexType );
    m_elements = elements;
  }

  @Override
  public List<ElementWithOccurs> getSequence( )
  {
    return m_elements;
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.RelationContentType#getAttributeGroups()
   */
  @Override
  public AttributeGroupRef[] getAttributeGroups( )
  {
    return getComplexType().getAttributeGroupArray();
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.RelationContentType#collectReferences()
   */
  @Override
  public String[] collectReferences( )
  {
    final XmlObject[] xmlObjects = getComplexType().selectPath( DOCREF_XPATH );

    final List<String> refs = new ArrayList<String>( xmlObjects.length );
    for( final XmlObject object : xmlObjects )
      refs.add( object.newCursor().getTextValue() );

    return refs.toArray( new String[refs.size()] );
  }
}
