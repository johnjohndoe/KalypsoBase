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
package org.kalypso.gmlschema;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.Platform;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;

/**
 * @author doemming
 */
public class CustomRelationType implements IRelationType
{

  private final IFeatureType[] m_targetFT;

  private final int m_minOccurs;

  private final int m_maxOccurs;

  private final QName m_qName;

  public CustomRelationType( final QName qName, final IFeatureType[] targetFeatureTypes, final int minOccurs, final int maxOccurs )
  {
    m_qName = qName;
    m_targetFT = targetFeatureTypes;
    m_minOccurs = minOccurs;
    m_maxOccurs = maxOccurs;
  }

  /**
   * @return allways <code>true</code> as in shapefile
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isInlineAble()
   */
  public boolean isInlineAble( )
  {
    return true;
  }

  /**
   * @return allways <code>false</code> as in shapefile
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isLinkAble()
   */
  public boolean isLinkAble( )
  {
    return false;
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#getTargetFeatureTypes(org.kalypso.gmlschema.GMLSchema,
   *      boolean)
   */
  public IFeatureType[] getTargetFeatureTypes( GMLSchema context, boolean includeAbstract )
  {
    return m_targetFT;
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
    // TODO nothing to init
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
  public Object getAdapter( Class adapter )
  {
    return Platform.getAdapterManager().getAdapter( this, adapter );
  }
}
