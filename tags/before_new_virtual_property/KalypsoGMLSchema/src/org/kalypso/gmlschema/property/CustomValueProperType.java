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
package org.kalypso.gmlschema.property;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.ITypeHandler;

/**
 * @author doemming
 */
public class CustomValueProperType implements IValuePropertyType
{
  private final QName m_qName;

  private final IPropertyContentType m_contentType;

  private final int m_minOccurs;

  private final int m_maxOccurs;

  private final boolean m_isNillable;

  public CustomValueProperType( final QName qName, final IPropertyContentType contentType, int minOccurs, int maxOccurs, final boolean isNillable )
  {
    m_qName = qName;
    m_contentType = contentType;
    m_minOccurs = minOccurs;
    m_maxOccurs = maxOccurs;
    m_isNillable = isNillable;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueQName()
   */
  public QName getValueQName( )
  {
    return m_contentType.getValueQName();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getPropertyContentType()
   */
  public IPropertyContentType getPropertyContentType( )
  {
    return m_contentType;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#hasRestriction()
   */
  public boolean hasRestriction( )
  {
    return m_contentType.hasRestriction();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getRestriction()
   */
  public IRestriction[] getRestriction( )
  {
    return m_contentType.getRestriction();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isFixed()
   */
  public boolean isFixed( )
  {
    return false;
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isNullable()
   */
  public boolean isNullable( )
  {
    return true;
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

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#isGeometry()
   */
  public boolean isGeometry( )
  {
    return m_contentType.isGeometry();
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getValueClass()
   */
  public Class getValueClass( )
  {
    return m_contentType.getValueClass();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMinOccurs()
   */
  public int getMinOccurs( )
  {
    return m_minOccurs;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMaxOccurs()
   */
  public int getMaxOccurs( )
  {
    return m_maxOccurs;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getQName()
   */
  public QName getQName( )
  {
    return m_qName;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getName()
   */
  public String getName( )
  {
    return m_qName.getLocalPart();
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun )
  {
    // nothing to init
  }

  /**
   * @see org.kalypso.gmlschema.property.IValuePropertyType#getTypeHandler()
   */
  public ITypeHandler getTypeHandler( )
  {
    return m_contentType.getTypeHandler();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isList()
   */
  public boolean isList( )
  {
    return m_maxOccurs > 1 || m_maxOccurs == UNBOUND_OCCURENCY;
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  public Object getAdapter( Class adapterClass )
  {
    final IAdapterManager adapterManager = Platform.getAdapterManager();
    final Object adapter = adapterManager.loadAdapter( this, adapterClass.getName() );
    return adapter;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isNillable()
   */
  public boolean isNillable( )
  {
    return m_isNillable;
  }

}
