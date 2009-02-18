/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.gmlschema.property.relation;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.AbstractPropertyTypeFromElement;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.ElementWithOccurs;
import org.kalypso.gmlschema.xml.QualifiedElement;

/**
 * @author doemming
 */
public class ReferencedRelationType extends AbstractPropertyTypeFromElement implements IRelationType
{
  private IRelationType m_globalRT = null;

  private final ElementReference m_reference;

  public ReferencedRelationType( final GMLSchema gmlSchema, final ElementWithOccurs element, final ElementReference reference )
  {
    // minOccurs and maxOccurs are used from local definition (according to XMLSCHEMA-Specs)
    super( gmlSchema, element, element.getElement().getRef() );

    m_reference = reference;
  }

  private void checkState( )
  {
    if( m_globalRT == null )
      throw new IllegalStateException( "Not yet initialized: " + this );
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#getTargetFeatureTypes(org.kalypso.gmlschema.GMLSchema,
   *      boolean)
   */
  public IFeatureType getTargetFeatureType( )
  {
    checkState();

    // global knows content
    return m_globalRT.getTargetFeatureType();
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isInlineAble()
   */
  public boolean isInlineAble( )
  {
    checkState();

    // global knows content
    return m_globalRT.isInlineAble();
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isLinkAble()
   */
  public boolean isLinkAble( )
  {
    checkState();

    // global knows content
    return m_globalRT.isLinkAble();
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( final int initializeRun ) throws GMLSchemaException
  {
    // find referenced relation type, it should also be build by now
    switch( initializeRun )
    {
      case INITIALIZE_RUN_FIRST:
        m_globalRT = (IRelationType) m_reference.getGMLSchema().getBuildedObjectFor( m_reference.getElement() );
        if( m_globalRT == null )
          throw new GMLSchemaException( "Unable to finde referenced IRelationType for: " + getQName() );

        break;

      default:
        break;
    }
  }

  public Element getReferencingElement( )
  {
    return super.getElement();
  }
  
  /**
   * @see org.kalypso.gmlschema.basics.QualifiedElement#getElement()
   */
  @Override
  public Element getElement( )
  {
//    checkState();
    
    // HACK: while building, this is used by RelationType2ComplexTypeBuilder
    if( m_globalRT == null )
      return m_reference.getElement();

    // allways return the one that known content
    // TODO for other usage implement AppinfoProvider
    return ((QualifiedElement) m_globalRT).getElement();
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#getDocumentReferences()
   */
  public IDocumentReference[] getDocumentReferences( )
  {
    checkState();

    return m_globalRT.getDocumentReferences();
  }
}
