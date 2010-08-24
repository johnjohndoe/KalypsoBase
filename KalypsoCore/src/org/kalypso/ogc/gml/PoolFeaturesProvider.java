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
package org.kalypso.ogc.gml;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.event.ModellEvent;

/**
 * @author Gernot Belger
 */
public class PoolFeaturesProvider extends AbstractFeaturesProvider implements ILoadStartable, ISaveableFeaturesProvider
{
  private final IGmlWorkspaceProviderListener m_providerListener = new IGmlWorkspaceProviderListener()
  {
    @Override
    public void workspaceChanged( final CommandableWorkspace oldWorkspace, final CommandableWorkspace newWorkspace )
    {
      handleWorkspaceChanged( newWorkspace );
    }
  };

  private final IFeaturesProviderListener m_featuresProviderListener = new IFeaturesProviderListener()
  {
    @Override
    public void featuresChanged( final IFeaturesProvider source, final ModellEvent modellEvent )
    {
      // We just promote the events of our delegate to our own listeners
      fireFeaturesChanged( modellEvent );
    }
  };

  private final PoolGmlWorkspaceProvider m_workspaceProvider;

  private IFeaturesProvider m_delegate = null;


  public PoolFeaturesProvider( final IPoolableObjectType poolKey, final String featurePath )
  {
    m_workspaceProvider = new PoolGmlWorkspaceProvider( poolKey );

    m_workspaceProvider.addListener( m_providerListener );

    m_delegate = createFeaturesProvider( null, featurePath );
    m_delegate.addFeaturesProviderListener( m_featuresProviderListener );
  }

  @Override
  public void dispose( )
  {
    m_workspaceProvider.removeListener( m_providerListener );
    m_workspaceProvider.dispose();

    m_delegate.dispose();
    m_delegate = null;

    super.dispose();
  }

  @Override
  public void startLoading( )
  {
    m_workspaceProvider.startLoading();
  }

  protected void handleWorkspaceChanged( final CommandableWorkspace newWorkspace )
  {
    final String featurePath = m_delegate.getFeaturePath();

    final IFeaturesProvider newDelegate = createFeaturesProvider( newWorkspace, featurePath );

    setDelegate( newDelegate );
  }

  private static IFeaturesProvider createFeaturesProvider( final CommandableWorkspace newWorkspace, final String featurePath )
  {
    if( newWorkspace == null )
      return new EmptyFeaturesProvider( featurePath );

    return new WorkspaceFeaturesProvider( newWorkspace, featurePath );
  }

  private void setDelegate( final IFeaturesProvider delegate )
  {
    m_delegate.removeFeaturesProviderListener( m_featuresProviderListener );

    m_delegate = delegate;

    m_delegate.addFeaturesProviderListener( m_featuresProviderListener );

    fireFeaturesChanged( null );
  }

  @Override
  public String getFeaturePath( )
  {
    return m_delegate.getFeaturePath();
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeatureType()
   */
  @Override
  public IFeatureType getFeatureType( )
  {
    return m_delegate.getFeatureType();
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeatures()
   */
  @Override
  public FeatureList getFeatureList( )
  {
    return m_delegate.getFeatureList();
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeatures()
   */
  @Override
  public List<Feature> getFeatures( )
  {
    return m_delegate.getFeatures();
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getWorkspace()
   */
  @Override
  public CommandableWorkspace getWorkspace( )
  {
    return m_workspaceProvider.getWorkspace();
  }

  public IStatus getStatus( )
  {
    return m_workspaceProvider.getStatus();
  }

  protected void handleModellChanged( final ModellEvent event )
  {
    fireFeaturesChanged( event );
  }

  public IPoolableObjectType getPoolKey( )
  {
    return m_workspaceProvider.getPoolKey();
  }

  @Override
  public void save( final IProgressMonitor monitor ) throws CoreException
  {
    m_workspaceProvider.save( monitor );
  }
}
