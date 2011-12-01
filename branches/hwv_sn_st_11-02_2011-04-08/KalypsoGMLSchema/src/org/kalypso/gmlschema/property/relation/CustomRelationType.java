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
package org.kalypso.gmlschema.property.relation;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.Platform;
import org.kalypso.gmlschema.annotation.DefaultAnnotation;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;

/**
 * TODO: shouldnt we also give the 'inlinable' and 'linkable' properties in the constructor? Maybe this type is not only
 * used in the shapefile-context.
 * 
 * @author doemming
 */
public class CustomRelationType implements IRelationType
{
  private final static IDocumentReference[] DOCUMENT_REFERENCES = new IDocumentReference[] { IDocumentReference.SELF_REFERENCE };

  private final IFeatureType m_targetFT;

  private final int m_minOccurs;

  private final int m_maxOccurs;

  private final QName m_qName;

  private final boolean m_isNillable;

  private final IAnnotation m_annotation;

  public CustomRelationType( final QName qName, final IFeatureType targetFeatureType, final int minOccurs, final int maxOccurs, final boolean isNillable )
  {
    m_qName = qName;
    m_annotation = new DefaultAnnotation( Platform.getNL(), qName.getLocalPart() );
    m_targetFT = targetFeatureType;
    m_minOccurs = minOccurs;
    m_maxOccurs = maxOccurs;
    m_isNillable = isNillable;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isVirtual()
   */
  @Override
  public boolean isVirtual( )
  {
    return false;
  }

  /**
   * @return allways <code>true</code> as in shapefile
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isInlineAble()
   */
  @Override
  public boolean isInlineAble( )
  {
    return true;
  }

  /**
   * @return allways <code>false</code> as in shapefile
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isLinkAble()
   */
  @Override
  public boolean isLinkAble( )
  {
    return false;
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#getTargetFeatureTypes(org.kalypso.gmlschema.GMLSchema,
   *      boolean)
   */
  @Override
  public IFeatureType getTargetFeatureType( )
  {
    return m_targetFT;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMinOccurs()
   */
  @Override
  public int getMinOccurs( )
  {
    return m_minOccurs;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMaxOccurs()
   */
  @Override
  public int getMaxOccurs( )
  {
    return m_maxOccurs;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getQName()
   */
  @Override
  public QName getQName( )
  {
    return m_qName;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getName()
   */
  @Override
  @SuppressWarnings("deprecation")
  @Deprecated
  public String getName( )
  {
    return m_qName.getLocalPart();
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  @Override
  public void init( final int initializeRun )
  {
    // TODO nothing to init
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isList()
   */
  @Override
  public boolean isList( )
  {
    return (m_maxOccurs > 1) || (m_maxOccurs == IPropertyType.UNBOUND_OCCURENCY);
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    return Platform.getAdapterManager().getAdapter( this, adapter );
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isNillable()
   */
  @Override
  public boolean isNillable( )
  {
    return m_isNillable;
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#getDocumentReferences()
   */
  @Override
  public IDocumentReference[] getDocumentReferences( )
  {
    return CustomRelationType.DOCUMENT_REFERENCES;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_qName.toString();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getAnnotation()
   */
  @Override
  public IAnnotation getAnnotation( )
  {
    return m_annotation;
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#cloneForFeatureType(org.kalypso.gmlschema.feature.IFeatureType)
   */
  @Override
  public IPropertyType cloneForFeatureType( final IFeatureType featureType )
  {
    return new CustomRelationType( m_qName, m_targetFT, m_minOccurs, m_maxOccurs, m_isNillable );
  }

}
