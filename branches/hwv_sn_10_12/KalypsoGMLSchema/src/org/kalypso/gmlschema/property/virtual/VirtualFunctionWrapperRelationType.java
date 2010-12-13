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
import org.kalypso.gmlschema.property.relation.IDocumentReference;
import org.kalypso.gmlschema.property.relation.IRelationType;

/**
 * Virtual function wrapping a {@link org.kalypso.gmlschema.property.relation.IRelationType}.
 * 
 * @author Gernot Belger
 */
public class VirtualFunctionWrapperRelationType implements IFunctionPropertyType, IRelationType
{
  private final IRelationType m_realPt;

  private final String m_functionId;

  private final Map<String, String> m_functionProperties;

  public VirtualFunctionWrapperRelationType( final IRelationType realPt, final String functionId, final Map<String, String> functionProperties )
  {
    m_realPt = realPt;
    m_functionId = functionId;
    m_functionProperties = functionProperties;
  }

  @Override
  public String getFunctionId( )
  {
    return m_functionId;
  }

  @Override
  public Map<String, String> getFunctionProperties( )
  {
    return m_functionProperties;
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#getDocumentReferences()
   */
  @Override
  public IDocumentReference[] getDocumentReferences( )
  {
    return m_realPt.getDocumentReferences();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMaxOccurs()
   */
  @Override
  public int getMaxOccurs( )
  {
    return m_realPt.getMaxOccurs();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getMinOccurs()
   */
  @Override
  public int getMinOccurs( )
  {
    return m_realPt.getMinOccurs();
  }

  /**
   * @deprecated
   * @see org.kalypso.gmlschema.property.IPropertyType#getName()
   */
  @Override
  @SuppressWarnings("deprecation")
  @Deprecated
  public String getName( )
  {
    return m_realPt.getName();
  }

  /**
   * @see org.kalypso.gmlschema.xml.IQualifiedElement#getQName()
   */
  @Override
  public QName getQName( )
  {
    return m_realPt.getQName();
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#getTargetFeatureType()
   */
  @Override
  public IFeatureType getTargetFeatureType( )
  {
    return m_realPt.getTargetFeatureType();
  }

  /**
   * @throws GMLSchemaException
   * @see org.kalypso.gmlschema.builder.IInitialize#init(int)
   */
  @Override
  public void init( final int initializeRun ) throws GMLSchemaException
  {
    m_realPt.init( initializeRun );
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isInlineAble()
   */
  @Override
  public boolean isInlineAble( )
  {
    return m_realPt.isInlineAble();
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isLinkAble()
   */
  @Override
  public boolean isLinkAble( )
  {
    return m_realPt.isLinkAble();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isList()
   */
  @Override
  public boolean isList( )
  {
    return m_realPt.isList();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isNillable()
   */
  @Override
  public boolean isNillable( )
  {
    return m_realPt.isNillable();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#isVirtual()
   */
  @Override
  public boolean isVirtual( )
  {
    return m_realPt.isVirtual();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_realPt.toString();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#getAnnotation()
   */
  @Override
  public IAnnotation getAnnotation( )
  {
    return m_realPt.getAnnotation();
  }

  /**
   * @see org.kalypso.gmlschema.property.IPropertyType#cloneForFeatureType(org.kalypso.gmlschema.feature.IFeatureType)
   */
  @Override
  public IPropertyType cloneForFeatureType( final IFeatureType featureType )
  {
    return new VirtualFunctionWrapperRelationType( (IRelationType) m_realPt.cloneForFeatureType( featureType ), m_functionId, m_functionProperties );
  }

}
