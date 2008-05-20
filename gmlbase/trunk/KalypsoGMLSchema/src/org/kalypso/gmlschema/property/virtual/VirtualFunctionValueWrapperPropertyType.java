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
package org.kalypso.gmlschema.property.virtual;

import java.util.Map;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;

/**
 * Implementation of {@link IVirtualFunctionValuePropertyType} based on a real property type.<br>
 * This implementation simply adds the functionId and functionProperties needed to implement the functionnal behaviour
 * of this property. The rest is delegated to the original property type.
 * 
 * @author Gernot Belger
 */
public class VirtualFunctionValueWrapperPropertyType implements IFunctionPropertyType, IValuePropertyType
{
  private final IValuePropertyType m_realPropertyType;

  private final String m_functionId;

  private final Map<String, String> m_functionProperties;

  public VirtualFunctionValueWrapperPropertyType( final IValuePropertyType realPropertyType, final String functionId, final Map<String, String> functionProperties )
  {
    m_realPropertyType = realPropertyType;
    m_functionId = functionId;
    m_functionProperties = functionProperties;
  }

  /**
   * @see org.kalypso.gmlschema.property.virtual.IVirtualFunctionValuePropertyType#getFunctionId()
   */
  public String getFunctionId( )
  {
    return m_functionId;
  }

  /**
   * @see org.kalypso.gmlschema.property.virtual.IVirtualFunctionValuePropertyType#getFunctionProperties()
   */
  public Map<String, String> getFunctionProperties( )
  {
    return m_functionProperties;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getDefault()
   */
  public String getDefault( )
  {
    return m_realPropertyType.getDefault();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getFixed()
   */
  public String getFixed( )
  {
    return m_realPropertyType.getFixed();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMaxOccurs()
   */
  public int getMaxOccurs( )
  {
    return m_realPropertyType.getMaxOccurs();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMinOccurs()
   */
  public int getMinOccurs( )
  {
    return m_realPropertyType.getMinOccurs();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getName()
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public String getName( )
  {
    return m_realPropertyType.getName();
  }

  /**
   * @see org.kalypso.gmlschema.xml.IQualifiedElement#getQName()
   */
  public QName getQName( )
  {
    return m_realPropertyType.getQName();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getRestriction()
   */
  public IRestriction[] getRestriction( )
  {
    return m_realPropertyType.getRestriction();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getTypeHandler()
   */
  public IMarshallingTypeHandler getTypeHandler( )
  {
    return m_realPropertyType.getTypeHandler();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueClass()
   */
  public Class< ? > getValueClass( )
  {
    return m_realPropertyType.getValueClass();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueQName()
   */
  public QName getValueQName( )
  {
    return m_realPropertyType.getValueQName();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasDefault()
   */
  public boolean hasDefault( )
  {
    return m_realPropertyType.hasDefault();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasRestriction()
   */
  public boolean hasRestriction( )
  {
    return m_realPropertyType.hasRestriction();
  }

  /**
   * @see org.kalypso.gmlschema.builder.IInitialize#init(int)
   */
  public void init( final int initializeRun ) throws GMLSchemaException
  {
    m_realPropertyType.init( initializeRun );
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isFixed()
   */
  public boolean isFixed( )
  {
    return m_realPropertyType.isFixed();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isGeometry()
   */
  public boolean isGeometry( )
  {
    return m_realPropertyType.isGeometry();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isList()
   */
  public boolean isList( )
  {
    return m_realPropertyType.isList();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isNillable()
   */
  public boolean isNillable( )
  {
    return m_realPropertyType.isNillable();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isNullable()
   */
  public boolean isNullable( )
  {
    return m_realPropertyType.isNullable();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isVirtual()
   */
  public boolean isVirtual( )
  {
    return m_realPropertyType.isVirtual();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_realPropertyType.toString();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getAnnotation()
   */
  public IAnnotation getAnnotation( )
  {
    return m_realPropertyType.getAnnotation();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#cloneForFeatureType(org.kalypso.gmlschema.feature.IFeatureType)
   */
  public IPropertyType cloneForFeatureType( final IFeatureType featureType )
  {
    return new VirtualFunctionValueWrapperPropertyType( (IValuePropertyType) m_realPropertyType.cloneForFeatureType( featureType ), m_functionId, m_functionProperties );
  }

}
