/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
  private IFeatureBindingCollection<IRoughnessClass> m_roughnessClasses = null;

  private IFeatureBindingCollection<IVegetationClass> m_vegetationClasses = null;

  private IFeatureBindingCollection<ICodeClass> m_codeClasses = null;

  public WspmClassification( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public IFeatureBindingCollection<IRoughnessClass> getRoughnessClassCollection( )
  {
    if( m_roughnessClasses == null )
      m_roughnessClasses = new FeatureBindingCollection<IRoughnessClass>( this, IRoughnessClass.class, QN_MEMBER_ROUGHNESS_CLASSES );

    return m_roughnessClasses;
  }

  @Override
  public IRoughnessClass[] getRoughnessClasses( )
  {
    final IFeatureBindingCollection<IRoughnessClass> collection = getRoughnessClassCollection();

    return collection.toArray( new IRoughnessClass[] {} );
  }

  @Override
  public IFeatureBindingCollection<IVegetationClass> getVegetationClassCollection( )
  {
    if( m_vegetationClasses == null )
      m_vegetationClasses = new FeatureBindingCollection<IVegetationClass>( this, IVegetationClass.class, QN_MEMBER_VEGETATION_CLASSES );

    return m_vegetationClasses;
  }

  @Override
  public IVegetationClass[] getVegetationClasses( )
  {
    final IFeatureBindingCollection<IVegetationClass> collection = getVegetationClassCollection();

    return collection.toArray( new IVegetationClass[] {} );
  }

  @Override
  public IRoughnessClass findRoughnessClass( final String name )
  {
    final IRoughnessClass[] roughnesses = getRoughnessClasses();
    for( final IRoughnessClass roughness : roughnesses )
    {
      if( roughness.getName().equals( name ) )
        return roughness;
    }

    return null;
  }

  @Override
  public IVegetationClass findVegetationClass( final String name )
  {
    final IVegetationClass[] vegetations = getVegetationClasses();
    for( final IVegetationClass vegetation : vegetations )
    {
      if( vegetation.getName().equals( name ) )
        return vegetation;
    }

    return null;
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.classifications.IWspmClassification#getCodeClassCollection()
   */
  @Override
  public IFeatureBindingCollection<ICodeClass> getCodeClassCollection( )
  {
    if( m_codeClasses == null )
      m_codeClasses = new FeatureBindingCollection<ICodeClass>( this, ICodeClass.class, QN_MEMBER_CODE_CLASSES );

    return m_codeClasses;
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.classifications.IWspmClassification#getCodeClasses()
   */
  @Override
  public ICodeClass[] getCodeClasses( )
  {
    final IFeatureBindingCollection<ICodeClass> collection = getCodeClassCollection();

    return collection.toArray( new ICodeClass[] {} );
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.classifications.IWspmClassification#findCodeClass(java.lang.String)
   */
  @Override
  public ICodeClass findCodeClass( final String name )
  {
    final ICodeClass[] classes = getCodeClasses();
    for( final ICodeClass clazz : classes )
    {
      if( clazz.getName().equals( name ) )
        return clazz;
    }

    return null;
  }
}
