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
package org.kalypso.gmlschema.property.value;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.eclipse.core.runtime.Assert;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.AbstractPropertyTypeFromElement;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.xml.Occurs;

/**
 * Represents a geometry property.<br>
 * Support properties hat reference to several geometries (choice).
 *
 * @author Gernot Belger
 */
public class GeometryPropertyType extends AbstractPropertyTypeFromElement implements IValuePropertyType
{
  private static final IRestriction[] EMPTY_RESTRICTION = new IRestriction[0];

  private final QName[] m_geometries;

  private final IMarshallingTypeHandler m_typeHandler;

  private final Class< ? > m_valueClass;

  private final static IMarshallingTypeHandler TH_GMOBJECT = MarshallingTypeRegistrySingleton.getTypeRegistry().getTypeHandlerForTypeName( new QName( NS.GML3, "_Geometry" ) ); //$NON-NLS-1$
  
  public GeometryPropertyType( final IGMLSchema gmlSchema, final Element element, final QName[] geometries, final Occurs occurs, final IFeatureType featureType )
  {
    super( gmlSchema, featureType, element, occurs, null );

    Assert.isNotNull( geometries );
    Assert.isTrue( geometries.length > 0 );

    m_geometries = geometries;

    final IMarshallingTypeHandler[] handlers = findTypeHandler( geometries );
    if( handlers.length == 1 )
      m_typeHandler = handlers[0];
    else
      m_typeHandler = TH_GMOBJECT;

    m_valueClass = m_typeHandler.getValueClass();
  }

  private IMarshallingTypeHandler[] findTypeHandler( final QName[] geometries )
  { 
    final ITypeRegistry<IMarshallingTypeHandler> registry = MarshallingTypeRegistrySingleton.getTypeRegistry();

    final IMarshallingTypeHandler[] handlers = new IMarshallingTypeHandler[geometries.length];
    for( int i = 0; i < geometries.length; i++ )
    {
      final QName geometry = geometries[i];
      handlers[i] = registry.getTypeHandlerForTypeName( geometry );
    }

    return handlers;
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( final int initializeRun )
  {
    // Nothing to do
  }

  public QName getValueQName( )
  {
    return m_typeHandler.getTypeName();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasRestriction()
   */
  public boolean hasRestriction( )
  {
    return false;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getRestriction()
   */
  public IRestriction[] getRestriction( )
  {
    return EMPTY_RESTRICTION;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isFixed()
   */
  public boolean isFixed( )
  {
    return false;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getFixed()
   */
  public String getFixed( )
  {
    return null;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasDefault()
   */
  public boolean hasDefault( )
  {
    return false;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getDefault()
   */
  public String getDefault( )
  {
    return null;
  }

  public boolean isNullable( )
  {
    return false;
  }

  /**
   * @deprecated
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public String getName( )
  {
    return getQName().getLocalPart();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isGeometry()
   */
  public boolean isGeometry( )
  {
    return true;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueClass()
   */
  public Class< ? > getValueClass( )
  {
    return m_valueClass;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getTypeHandler()
   */
  public IMarshallingTypeHandler getTypeHandler( )
  {
    return m_typeHandler;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#cloneForFeatureType(org.kalypso.gmlschema.feature.IFeatureType)
   */
  public IPropertyType cloneForFeatureType( final IFeatureType featureType )
  {
    return new GeometryPropertyType( getGMLSchema(), getElement(), m_geometries, getOccurs(), featureType );
  }
}
