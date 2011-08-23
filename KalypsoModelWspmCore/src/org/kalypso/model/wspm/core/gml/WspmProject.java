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
package org.kalypso.model.wspm.core.gml;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * This is an abstraction layer over an wspmproje gml instance.
 * <p>
 * It has NO own member variables, everything is backed by the given feature instance.
 * </p>
 * 
 * @author Gernot Belger
 */
public abstract class WspmProject extends Feature_Impl implements IWspmProject
{
  private IFeatureBindingCollection<WspmWaterBody> m_waterBodies = null;

  private IFeatureBindingCollection<IWspmClassification> m_classifications = null;

  public WspmProject( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public IFeatureBindingCollection<WspmWaterBody> getWaterBodies( )
  {
    if( m_waterBodies == null )
      m_waterBodies = new FeatureBindingCollection<WspmWaterBody>( this, WspmWaterBody.class, QN_MEMBER_WATER_BODY );

    return m_waterBodies;
  }

  /**
   * @see org.kalypso.model.wspm.core.gml.IWspmProject#getClassificationMember()
   */
  @Override
  public IWspmClassification getClassificationMember( )
  {
    if( Objects.isNull( m_classifications ) )
      m_classifications = new FeatureBindingCollection<IWspmClassification>( this, IWspmClassification.class, QN_CLASSIFICATION_MEMBER );

    if( m_classifications.isEmpty() )
      return m_classifications.get( 0 );

    return null;
  }

  /**
   * Returns the {@link WspmWaterBody} with the given name.
   */
  @Override
  public WspmWaterBody findWater( final String waterName )
  {
    final IFeatureBindingCollection<WspmWaterBody> waters = getWaterBodies();
    for( final WspmWaterBody body : waters )
    {
      if( waterName.equals( body.getName() ) )
        return body;
    }

    return null;
  }

  @Override
  public WspmWaterBody findWaterByRefNr( final String refNr )
  {
    final IFeatureBindingCollection<WspmWaterBody> waters = getWaterBodies();
    for( final WspmWaterBody body : waters )
    {
      if( refNr.equals( body.getRefNr() ) )
        return body;
    }

    return null;
  }

  /**
   * Creates a new water body and adds it to this project.
   * <p>
   * If a waterBody with the same name is already present, this will be retuned instead.
   * </p>
   */
  @Override
  public WspmWaterBody createWaterBody( final String name, final boolean isDirectionUpstreams ) throws GMLSchemaException
  {
    final WspmWaterBody water = findWater( name );
    if( water != null )
      return water;

    final WspmWaterBody wspmWaterBody = (WspmWaterBody) FeatureHelper.addFeature( this, QN_MEMBER_WATER_BODY, null );

    // set default values
    wspmWaterBody.setName( name );
    wspmWaterBody.setDescription( "" ); //$NON-NLS-1$
    wspmWaterBody.setRefNr( "" ); //$NON-NLS-1$
    wspmWaterBody.setDirectionUpstreams( isDirectionUpstreams );

    return wspmWaterBody;
  }
}