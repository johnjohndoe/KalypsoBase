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
package org.kalypso.gmlschema.builder;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.xml.ComplexTypeReference;
import org.kalypso.gmlschema.xml.QualifiedElement;

/**
 * another builder
 *
 * @author doemming
 */
public class RelationType2ComplexTypeBuilder implements IBuilder
{
  @Override
  public Object[] build( final GMLSchema gmlSchema, final Object relationTypeObject ) throws GMLSchemaException
  {
    final QualifiedElement qe = (QualifiedElement) relationTypeObject;
    final Element element = qe.getElement();
    final ComplexTypeReference complexTypeReference = gmlSchema.getComplexTypeReferenceFor( element );
    final ComplexType complexType = complexTypeReference.getComplexType();
    gmlSchema.register( qe, complexType );
    return new Object[] { complexType };
  }

  @Override
  public boolean isBuilderFor( final GMLSchema gmlSchema, final Object object, final String namedPass )
  {
    return object instanceof IRelationType;
  }

  @Override
  public boolean replaces( final IBuilder other )
  {
    return false;
  }
}