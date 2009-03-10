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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
/**
 * Default implementation of {@link IVirtualFunctionValuePropertyType} bases on the {@link ITypeHandler} associated with
 * the property q-name
 * 
 * @author Congo Patrice
 */
public class VirtualFunctionValuePropertyType implements IFunctionPropertyType, IValuePropertyType
{
  private final IMarshallingTypeHandler m_handler;

  private final IRestriction[] m_restrictions = {};

  private final int m_maxOccurs = 1;

  private final int m_minOccurs = 0;

  private final boolean m_isList = false;

  private final boolean m_isNillable = false;

  private final String m_defaultValue = null;

  private final String m_fixed = null;

  private final boolean m_hasDefault = false;

  private final boolean m_isFixed = false;

  private final boolean m_isNullable = false;

  private final QName m_qName;

  private final String m_functionId;

  private final Map<String, String> m_properties;

  private final IAnnotation m_annotation;

  /**
   * Creates a new {@link VirtualFunctionPropertyType} for the given value handler and the property q-name
   * 
   * @param handler
   *            handler for the property value
   * @param propertyQName
   *            the q-name of the virtual property
   */
  public VirtualFunctionValuePropertyType( final IFeatureType featureType, final IMarshallingTypeHandler handler, final QName propertyQName, final String functionId, final Map<String, String> properties ) throws IllegalArgumentException
  {
    Assert.isNotNull( propertyQName );

    m_qName = propertyQName;
    m_handler = handler;
    m_functionId = functionId;
    m_properties = properties;
    m_annotation = AnnotationUtilities.createAnnotation( propertyQName, featureType, null, null );
  }

  /**
   * @see org.kalypso.gmlschema.xml.IQualifiedElement#getQName()
   */
  public QName getQName( )
  {
    return m_qName;
  }

  /**
   * @see org.kalypso.gmlschema.property.virtual.IVirtualFunctionPropertyType#isGeometry()
   */
  public boolean isGeometry( )
  {
    return m_handler.isGeometry();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMaxOccurs()
   */
  public int getMaxOccurs( )
  {
    return m_maxOccurs;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMinOccurs()
   */
  public int getMinOccurs( )
  {
    return m_minOccurs;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isList()
   */
  public boolean isList( )
  {
    return m_isList;
  }

  /**
   * This IS a virtual property.
   * 
   * @see org.kalypso.gmlschema.property.IPropertyType#isVirtual()
   */
  public boolean isVirtual( )
  {
    return true;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isNillable()
   */
  public boolean isNillable( )
  {
    return m_isNillable;
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( final int initializeRun )
  {

  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getDefault()
   */
  public String getDefault( )
  {
    return m_defaultValue;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getFixed()
   */
  public String getFixed( )
  {
    return m_fixed;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getRestriction()
   */
  public IRestriction[] getRestriction( )
  {
    return m_restrictions;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getTypeHandler()
   */
  public IMarshallingTypeHandler getTypeHandler( )
  {
    return m_handler;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueClass()
   */
  public Class< ? > getValueClass( )
  {
    return m_handler.getValueClass();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueQName()
   */
  public QName getValueQName( )
  {
    return m_handler.getTypeName();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasDefault()
   */
  public boolean hasDefault( )
  {
    return m_hasDefault;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasRestriction()
   */
  public boolean hasRestriction( )
  {
    if( m_restrictions == null )
    {
      return false;
    }
    else
    {
      return m_restrictions.length > 0;
    }
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isFixed()
   */
  public boolean isFixed( )
  {
    return m_isFixed;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isNullable()
   */
  public boolean isNullable( )
  {
    return m_isNullable;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return "" + m_qName;
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  public Object getAdapter( final Class adapter )
  {
    if( !Platform.isRunning() )
      return null;
    final IAdapterManager adapterManager = Platform.getAdapterManager();
    return adapterManager.loadAdapter( this, adapter.getName() );
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getName()
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public String getName( )
  {
    return m_qName.getLocalPart();
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
    return m_properties;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getAnnotation()
   */
  public IAnnotation getAnnotation( )
  {
    return m_annotation;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#cloneForFeatureType(org.kalypso.gmlschema.feature.IFeatureType)
   */
  public IPropertyType cloneForFeatureType( final IFeatureType featureType )
  {
    return new VirtualFunctionValuePropertyType( featureType, m_handler, m_qName, m_functionId, m_properties );
  }
}
