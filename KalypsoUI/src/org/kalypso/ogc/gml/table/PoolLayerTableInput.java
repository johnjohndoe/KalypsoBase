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
package org.kalypso.ogc.gml.table;

import java.util.List;

import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.IFeaturesProvider;
import org.kalypso.ogc.gml.IFeaturesProviderListener;
import org.kalypso.ogc.gml.PoolFeaturesProvider;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.event.ModellEvent;

/**
 * @author Gernot Belger
 */
public class PoolLayerTableInput implements ILayerTableInput
{
  private final IFeaturesProviderListener m_featuresProviderListener = new IFeaturesProviderListener()
  {
    @Override
    public void featuresChanged( final IFeaturesProvider source, final ModellEvent modellEvent )
    {
      handleFeaturesChanged( modellEvent );
    }
  };

  private JobExclusiveCommandTarget m_commandTarget;

  private final PoolFeaturesProvider m_featuresProvider;


  public PoolLayerTableInput( final IPoolableObjectType poolKey, final String featurePath )
  {
    m_featuresProvider = new PoolFeaturesProvider( poolKey, featurePath );

    m_featuresProvider.addFeaturesProviderListener( m_featuresProviderListener );

    m_featuresProvider.startLoading();
  }

  protected void handleFeaturesChanged( final ModellEvent modellEvent )
  {
    if( modellEvent == null )
      handleWorkspaceChanged( m_featuresProvider.getWorkspace() );
  }

  public IPoolableObjectType getPoolKey( )
  {
    return m_featuresProvider.getPoolKey();
  }

  protected void handleWorkspaceChanged( final CommandableWorkspace newWorkspace )
  {
    if( m_commandTarget != null )
    {
      m_commandTarget.dispose();
      m_commandTarget = null;
    }

    if( newWorkspace != null )
      m_commandTarget = new JobExclusiveCommandTarget( newWorkspace, null );
  }

  /**
   * @param l
   * @see org.kalypso.ogc.gml.IFeaturesProvider#addFeaturesProviderListener(org.kalypso.ogc.gml.IFeaturesProviderListener)
   */
  @Override
  public void addFeaturesProviderListener( final IFeaturesProviderListener l )
  {
    m_featuresProvider.addFeaturesProviderListener( l );
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    m_featuresProvider.dispose();

    if( m_commandTarget != null )
    {
      m_commandTarget.dispose();
      m_commandTarget = null;
    }
  }

  /**
   * @return
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeatureList()
   */
  @Override
  public FeatureList getFeatureList( )
  {
    return m_featuresProvider.getFeatureList();
  }

  /**
   * @return
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeaturePath()
   */
  @Override
  public String getFeaturePath( )
  {
    return m_featuresProvider.getFeaturePath();
  }

  /**
   * @return
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeatures()
   */
  @Override
  public List<Feature> getFeatures( )
  {
    return m_featuresProvider.getFeatures();
  }

  /**
   * @return
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeatureType()
   */
  @Override
  public IFeatureType getFeatureType( )
  {
    return m_featuresProvider.getFeatureType();
  }

  /**
   * @return
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getWorkspace()
   */
  @Override
  public CommandableWorkspace getWorkspace( )
  {
    return m_featuresProvider.getWorkspace();
  }

  /**
   * @param l
   * @see org.kalypso.ogc.gml.IFeaturesProvider#removeFeaturesProviderListener(org.kalypso.ogc.gml.IFeaturesProviderListener)
   */
  @Override
  public void removeFeaturesProviderListener( final IFeaturesProviderListener l )
  {
    m_featuresProvider.removeFeaturesProviderListener( l );
  }

  /**
   * @see org.kalypso.ogc.gml.table.ILayerTableInput#getCommandTarget()
   */
  @Override
  public ICommandTarget getCommandTarget( )
  {
    return m_commandTarget;
  }

  /**
   * @see org.kalypso.ogc.gml.table.ILayerTableInput#isDirty()
   */
  @Override
  public boolean isDirty( )
  {
    if( m_commandTarget == null )
      return false;

    return m_commandTarget.isDirty();
  }

}
