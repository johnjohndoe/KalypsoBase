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

import javax.xml.namespace.QName;

import org.apache.commons.lang.ObjectUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.Occurs;
import org.kalypso.gmlschema.xml.QualifiedElement;

/**
 * Property feature type that is build from a xml-element
 * 
 * @author doemming
 */
public abstract class AbstractPropertyTypeFromElement extends QualifiedElement implements IPropertyType
{
  private final Occurs m_occurs;

  private final IAnnotation m_annotation;

  public AbstractPropertyTypeFromElement( final GMLSchema gmlSchema, final QName qName, final IFeatureType featureType, final Element element, final Occurs occurs, final ElementReference reference )
  {
    super( gmlSchema, element, qName );
    m_occurs = occurs;
    m_annotation = AnnotationUtilities.createAnnotation( qName, featureType, element, reference );
  }

  public AbstractPropertyTypeFromElement( final GMLSchema gmlSchema, final IFeatureType featureType, final Element element, final Occurs occurs, final ElementReference reference )
  {
    super( gmlSchema, element, createQName( gmlSchema, element ) );
    m_occurs = occurs;
    m_annotation = AnnotationUtilities.createAnnotation( getQName(), featureType, element, reference );
  }

  public Occurs getOccurs( )
  {
    return m_occurs;
  }

  public int getMinOccurs( )
  {
    return m_occurs.getMin();
  }

  public int getMaxOccurs( )
  {
    return m_occurs.getMax();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isList()
   */
  public boolean isList( )
  {
    return getMaxOccurs() > 1 || getMaxOccurs() == UNBOUND_OCCURENCY;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isNillable()
   */
  public boolean isNillable( )
  {
    return getElement().getNillable();
  }

  /**
   * <This property is created from a real xml-element and so is not virtual.
   * 
   * @see org.kalypso.gmlschema.property.IPropertyType#isVirtual()
   */
  public boolean isVirtual( )
  {
    return false;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getAnnotation()
   */
  public IAnnotation getAnnotation( )
  {
    return m_annotation;
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof IPropertyType )
      return ObjectUtils.equals( getQName(), ((IPropertyType) obj).getQName() );

    return false;
  }
}
