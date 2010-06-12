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

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.adv.ADVUtilities;
import org.kalypso.gmlschema.property.relation.AdvRelationType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.relation.ReferencedRelationType;
import org.kalypso.gmlschema.property.relation.RelationType;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.ElementWithOccurs;
import org.kalypso.gmlschema.xml.Occurs;

/**
 * a builder for references based on restrictions of FeaturePropertyType and ADV rules
 * 
 * @author doemming
 */
public class FeaturePropertyType2RelationTypeBuilder extends AbstractBuilder
{
  private final String m_gmlVersion;

  public FeaturePropertyType2RelationTypeBuilder( final String gmlVersion )
  {
    m_gmlVersion = gmlVersion;
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#build(org.kalypso.gmlschema.GMLSchema, java.lang.Object)
   */
  @Override
  public Object[] build( final GMLSchema gmlSchema, final Object elementObject ) throws GMLSchemaException
  {
    final ElementWithOccurs elementWithOccurs = (ElementWithOccurs) elementObject;
    final Element element = elementWithOccurs.getElement();
    final Occurs occurs = elementWithOccurs.getOccurs();
    final QName ref = element.getRef();

    if( ref != null )
    {
      final ElementReference reference = gmlSchema.resolveElementReference( ref );
      final ReferencedRelationType result = new ReferencedRelationType( gmlSchema, element, occurs, reference, null );
      gmlSchema.register( element, result );
      return new Object[] { result };
    }

    final QName advReferenziertesElement = ADVUtilities.getReferenziertesElement( element );
    if( advReferenziertesElement != null )
    {
      final AdvRelationType advRelation = new AdvRelationType( gmlSchema, element, occurs, advReferenziertesElement, null );
      gmlSchema.register( element, advRelation );
      return new Object[0]; // why not return it?
    }

    final IRelationType result = new RelationType( gmlSchema, element, occurs, null );
    gmlSchema.register( element, result );
    return new Object[] { result };
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#isBuilderFor(org.kalypso.gmlschema.GMLSchema, java.lang.Object,
   *      java.lang.String)
   */
  @Override
  public boolean isBuilderFor( final GMLSchema gmlSchema, final Object object, final String namedPass ) throws GMLSchemaException
  {
    if( !(object instanceof ElementWithOccurs) )
      return false;
    final Element element = ((ElementWithOccurs) object).getElement();
    final QName baseType = GMLSchemaUtilities.findBaseType( gmlSchema, element, m_gmlVersion );
    if( baseType == null )
      return false;

    final boolean result = GMLSchemaUtilities.isRelationType( m_gmlVersion, baseType );
    if( result == true )
    {
      // Special case adv annotation stuff: should be handled separately
      // Only build it, if we really have a typed reference; else we will later
      // have not feature type for this element
      if( GMLSchemaUtilities.GML3_ReferenceType.equals( baseType ) )
        return ADVUtilities.getReferenziertesElement( element ) != null;

      return true;
    }

    return result;
  }

  /**
   * @see org.kalypso.gmlschema.builder.IBuilder#replaces(org.kalypso.gmlschema.builder.IBuilder)
   */
  @Override
  public boolean replaces( final IBuilder other )
  {
    return false;
  }

}
