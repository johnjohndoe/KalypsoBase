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
package org.kalypso.gmlschema.builder;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.ElementWithOccurs;
import org.kalypso.gmlschema.basics.QualifiedElement;
import org.kalypso.gmlschema.property.AbstractPropertyTypeFromElement;
import org.kalypso.gmlschema.property.IPropertyContentType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.ITypeHandler;

/**
 * @author doemming
 */
public class ReferencedPropertyType extends AbstractPropertyTypeFromElement implements IValuePropertyType
{
  private final IValuePropertyType m_globalPT;

  public ReferencedPropertyType( final GMLSchema gmlSchema, ElementWithOccurs element, IValuePropertyType pt )
  {
    // minOccurs and maxOccurs are used from local definition (according to XMLSCHEMA-Specs)
    super( gmlSchema, element, pt.getQName() );
    m_globalPT = pt;
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun )
  {
    // nothing to init
  }

  /**
   * @see org.kalypso.gmlschema.basics.QualifiedElement#getElement()
   */
  @Override
  public Element getElement( )
  {
    // witch one to return, please list here the use cases and hints for discussion/implementation
    // 1. provide appinfo -> local element
    // we need a appinfoprovider, that can collect them !

    // allways return the one that known content
    // TODO for other usage implement AppinfoProvider
    return ((QualifiedElement) m_globalPT).getElement();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getPropertyContentType()
   */
  public IPropertyContentType getPropertyContentType( )
  {
    // gloab knows content
    return m_globalPT.getPropertyContentType();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasRestriction()
   */
  public boolean hasRestriction( )
  {
    // global knows content...
    return m_globalPT.hasRestriction();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getRestriction()
   */
  public IRestriction[] getRestriction( )
  {
    // global knows content...
    return m_globalPT.getRestriction();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isFixed()
   */
  public boolean isFixed( )
  {
    // both can be fixed
    return m_globalPT.isFixed() || m_element.isSetFixed();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getFixed()
   */
  public String getFixed( )
  {
    // one can be fixed, or both for the same value
    if( m_element.isSetFixed() )
      return m_element.getFixed();
    else
      return m_globalPT.getFixed();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isNullable()
   */
  public boolean isNullable( )
  {
    // nil-able for references is not allowed
    return m_globalPT.isNullable();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasDefault()
   */
  public boolean hasDefault( )
  {
    // both can have default:
    return m_element.isSetDefault() || m_globalPT.hasDefault();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getDefault()
   */
  public String getDefault( )
  {
    // the more specialized value is more relevant
    if( m_element.isSetDefault() )
      return m_element.getDefault();
    return m_globalPT.getDefault();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isGeometry()
   */
  public boolean isGeometry( )
  {
    // global element knows content...
    return m_globalPT.isGeometry();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueClass()
   */
  public Class getValueClass( )
  {
    // global element knows content...
    return m_globalPT.getValueClass();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getTypeHandler()
   */
  public ITypeHandler getTypeHandler( )
  {
    // global element knows content...
    return m_globalPT.getTypeHandler();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueQName()
   */
  public QName getValueQName( )
  {
    // global element knows content...
    return m_globalPT.getValueQName();
  }
}
