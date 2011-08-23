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
package org.kalypso.model.wspm.core.gml.classifications;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * @author Dirk Kuch
 */
public class WspmClassification extends Feature_Impl implements IWspmClassification
{

  private IFeatureBindingCollection<RoughnessClass> m_roughnessClasses = null;

  private IFeatureBindingCollection<VegetationClass> m_vegetationClasses = null;

// @Override
// public IFeatureBindingCollection<WspmWaterBody> getWaterBodies( )
// {
// if( m_waterBodies == null )
// m_waterBodies = new FeatureBindingCollection<WspmWaterBody>( this, WspmWaterBody.class, QN_MEMBER_WATER_BODY );
//
// return m_waterBodies;
// }
//

  public WspmClassification( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.classifications.IWspmClassification#getRoughnessClassCollection()
   */
  @Override
  public IFeatureBindingCollection<RoughnessClass> getRoughnessClassCollection( )
  {
    if( m_roughnessClasses == null )
      m_roughnessClasses = new FeatureBindingCollection<RoughnessClass>( this, RoughnessClass.class, QN_MEMBER_ROUGHNESS_CLASSES );

    return m_roughnessClasses;
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.classifications.IWspmClassification#getRoughnessClasses()
   */
  @Override
  public IRoughnessClass[] getRoughnessClasses( )
  {
    final IFeatureBindingCollection<RoughnessClass> collection = getRoughnessClassCollection();

    return collection.toArray( new RoughnessClass[] {} );

  }

  /**
   * @see org.kalypso.model.wspm.core.gml.classifications.IWspmClassification#getVegetationClassCollection()
   */
  @Override
  public IFeatureBindingCollection<VegetationClass> getVegetationClassCollection( )
  {
    if( m_vegetationClasses == null )
      m_vegetationClasses = new FeatureBindingCollection<VegetationClass>( this, VegetationClass.class, QN_MEMBER_VEGETATION_CLASSES );

    return m_vegetationClasses;
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.classifications.IWspmClassification#getVegetationClasses()
   */
  @Override
  public IVegetationClass[] getVegetationClasses( )
  {
    final IFeatureBindingCollection<VegetationClass> collection = getVegetationClassCollection();

    return collection.toArray( new VegetationClass[] {} );
  }

}
