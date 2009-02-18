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
package org.kalypso.gmlschema.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.Platform;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;

/**
 * @author doemming
 */
public class CustomFeatureType implements IFeatureType
{
  private final QName m_qName;

  private final HashMap<QName, IPropertyType> m_qNameMap = new HashMap<QName, IPropertyType>();

  private final HashMap<String, IPropertyType> m_locatPartMap = new HashMap<String, IPropertyType>();

  private final HashMap<IPropertyType, Integer> m_positionMap = new HashMap<IPropertyType, Integer>();

  private final IPropertyType[] m_properties;

  private final IValuePropertyType[] m_allGeometryPTS;

  private final int m_defaultGeometryPosition;

  public CustomFeatureType( final QName qName, final IPropertyType[] pts )
  {
    m_qName = qName;
    int geoPos = -1;
    final List<IValuePropertyType> col = new ArrayList<IValuePropertyType>();
    for( int i = 0; i < pts.length; i++ )
    {
      final IPropertyType pt = pts[i];
      m_qNameMap.put( pt.getQName(), pt );
      m_locatPartMap.put( pt.getQName().getLocalPart(), pt );
      m_positionMap.put( pt, new Integer( i ) );
      if( pt instanceof IValuePropertyType )
      {
        final IValuePropertyType vpt = (IValuePropertyType) pt;
        if( vpt.isGeometry() )
        {
          col.add( vpt );
          if( geoPos < 0 )
            geoPos = i;
        }
      }
    }
    m_allGeometryPTS = col.toArray( new IValuePropertyType[col.size()] );
    m_defaultGeometryPosition = geoPos;
    m_properties = pts;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getName()
   */
  public String getName( )
  {
    return m_qName.getLocalPart();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperties()
   */
  public IPropertyType[] getProperties( )
  {
    return m_properties;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperties(int)
   */
  public IPropertyType getProperties( int position )
  {
    return m_properties[position];
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getSizeOfProperties()
   */
  public int getSizeOfProperties( )
  {
    return m_properties.length;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperty(javax.xml.namespace.QName)
   */
  public IPropertyType getProperty( QName qname )
  {
    return m_qNameMap.get( qname );
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getProperty(java.lang.String)
   */
  public IPropertyType getProperty( final String propNameLocalPart )
  {
    return m_locatPartMap.get( propNameLocalPart );
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#isAbstract()
   */
  public boolean isAbstract( )
  {
    return false;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getSubstitutionGroupFT()
   */
  public IFeatureType getSubstitutionGroupFT( )
  {
    return null;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getSubstituts(org.kalypso.gmlschema.GMLSchema, boolean, boolean)
   */
  public IFeatureType[] getSubstituts( GMLSchema contextSchema, boolean includeAbstract, boolean inclusiveThis )
  {
    if( inclusiveThis )
      return new IFeatureType[] { this };
    return new IFeatureType[0];
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getQName()
   */
  public QName getQName( )
  {
    return m_qName;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getNamespace()
   */
  public String getNamespace( )
  {
    return m_qName.getNamespaceURI();
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getDefaultGeometryPropertyPosition()
   */
  public int getDefaultGeometryPropertyPosition( )
  {
    return m_defaultGeometryPosition;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getPropertyPosition(org.kalypso.gmlschema.property.IPropertyType)
   */
  public int getPropertyPosition( final IPropertyType pt )
  {
    return m_positionMap.get( pt );
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getAllGeomteryProperties()
   */
  public IValuePropertyType[] getAllGeomteryProperties( )
  {
    return m_allGeometryPTS;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureType#getDefaultGeometryProperty()
   */
  public IValuePropertyType getDefaultGeometryProperty( )
  {
    return m_allGeometryPTS[0];
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun )
  {
    // TODO nothing to init
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  public Object getAdapter( Class adapter )
  {
    return Platform.getAdapterManager().getAdapter( this, adapter );
  }

}
