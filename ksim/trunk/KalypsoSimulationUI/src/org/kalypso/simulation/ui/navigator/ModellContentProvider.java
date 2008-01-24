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
package org.kalypso.simulation.ui.navigator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.gmleditor.ui.FeatureAssociationTypeElement;
import org.kalypso.ui.editor.gmleditor.ui.GMLContentProvider;
import org.kalypso.util.pool.IPoolListener;
import org.kalypso.util.pool.IPoolableObjectType;
import org.kalypso.util.pool.PoolableObjectType;
import org.kalypso.util.pool.ResourcePool;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Gernot Belger
 */
public class ModellContentProvider implements ICommonContentProvider, IPoolListener
{
  private final static class KeyWrapper
  {
    private final IPoolableObjectType m_key;

    private final GMLWorkspace m_workspace;

    private final IStatus m_status;

    public KeyWrapper( final IPoolableObjectType key, final GMLWorkspace workspace, final IStatus status )
    {
      m_key = key;
      m_workspace = workspace;
      m_status = status;
    }

    public IPoolableObjectType getKey( )
    {
      return m_key;
    }

    public GMLWorkspace getWorkspace( )
    {
      return m_workspace;
    }

    public IStatus getStatus( )
    {
      return m_status;
    }
  }

  private final Map<IFile, KeyWrapper> m_workspaces = new HashMap<IFile, KeyWrapper>();

  private final Map<GMLWorkspace, GMLContentProvider> m_contentProviderMap = new HashMap<GMLWorkspace, GMLContentProvider>();

  private Viewer m_viewer;

  /**
   * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
   */
  public void init( final ICommonContentExtensionSite aConfig )
  {
    System.out.println( "Initialising ModellContentProvider" );
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren( final Object parentElement )
  {
    if( parentElement instanceof IProject )
    {
      final KeyWrapper wrapperForProject = wrapperForProject( (IProject) parentElement );
      if( wrapperForProject == null )
        return new Object[0];
      else if( wrapperForProject.getWorkspace() == null )
        return new Object[] { wrapperForProject.getStatus() };
    }

    final GMLWorkspace workspace = getWorkspace( parentElement );
    if( workspace == null )
      return new Object[] {};

    final GMLContentProvider cp = getContentProviderForWorkspace( workspace );
    if( cp == null )
      return new Object[] {};

    if( parentElement instanceof IProject )
      return cp.getElements( null );
    else
      return cp.getChildren( parentElement );

  }

  private GMLContentProvider getContentProviderForWorkspace( final GMLWorkspace workspace )
  {
    if( m_contentProviderMap.containsKey( workspace ) )
      return m_contentProviderMap.get( workspace );

    final GMLContentProvider cp = new GMLContentProvider();
    cp.inputChanged( null, null, workspace );

    m_contentProviderMap.put( workspace, cp );

    return cp;
  }

  private GMLWorkspace getWorkspace( final Object parentElement )
  {
    if( parentElement instanceof IProject )
    {
      final KeyWrapper wrapper = wrapperForProject( (IProject) parentElement );
      return wrapper.getWorkspace();
    }
    else if( parentElement instanceof Feature )
    {
      final Feature f = (Feature) parentElement;
      return f.getWorkspace();
    }
    else if( parentElement instanceof FeatureAssociationTypeElement )
    {
      final FeatureAssociationTypeElement fate = (FeatureAssociationTypeElement) parentElement;
      return fate.getParentFeature().getWorkspace();
    }

    return null;
  }

  private KeyWrapper wrapperForProject( final IProject project )
  {
    try
    {
      final IFile file = project.getFile( "modell.gml" );
      final KeyWrapper wrapper = getWorkspaceForFile( file );
      return wrapper;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  private KeyWrapper getWorkspaceForFile( final IFile file ) throws MalformedURLException
  {
    if( m_workspaces.containsKey( file ) )
    {
      final KeyWrapper keyWrapper = m_workspaces.get( file );
      return keyWrapper;
    }

    // Workspace was not loaded yet, so start to load it via the pool

    final URL gmlUrl = ResourceUtilities.createURL( file );

    final ResourcePool pool = KalypsoGisPlugin.getDefault().getPool();
    final PoolableObjectType key = new PoolableObjectType( "gml", gmlUrl.toString(), gmlUrl );
    pool.addPoolListener( this, key );
    final KeyWrapper keyWrapper = new KeyWrapper( key, null, new Status( IStatus.INFO, KalypsoGisPlugin.getId(), "wird geladen..." ) );
    m_workspaces.put( file, keyWrapper );
    return keyWrapper;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent( final Object element )
  {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren( final Object parentElement )
  {
    if( parentElement instanceof IProject )
    {
      final IProject project = (IProject) parentElement;
      final IFile file = project.getFile( "modell.gml" );
      return file.exists();
    }
    else if( parentElement instanceof Feature )
    {
      return getChildren( parentElement ).length > 0;
    }
    else if( parentElement instanceof FeatureAssociationTypeElement )
    {
      return getChildren( parentElement ).length > 0;
    }

    return false;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements( final Object inputElement )
  {
    return new Object[] { "Hallo Gernot" };
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose( )
  {
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    m_viewer = viewer;
  }

  /**
   * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
   */
  public void restoreState( final IMemento aMemento )
  {
  }

  /**
   * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
   */
  public void saveState( final IMemento aMemento )
  {
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#dirtyChanged(org.kalypso.util.pool.IPoolableObjectType, boolean)
   */
  public void dirtyChanged( final IPoolableObjectType key, final boolean isDirty )
  {
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#isDisposed()
   */
  public boolean isDisposed( )
  {
    return false;
  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#objectInvalid(org.kalypso.util.pool.IPoolableObjectType, java.lang.Object)
   */
  public void objectInvalid( IPoolableObjectType key, Object oldValue )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.kalypso.util.pool.IPoolListener#objectLoaded(org.kalypso.util.pool.IPoolableObjectType, java.lang.Object,
   *      org.eclipse.core.runtime.IStatus)
   */
  public void objectLoaded( final IPoolableObjectType key, final Object newValue, final IStatus status )
  {
    for( final Map.Entry<IFile, KeyWrapper> entry : m_workspaces.entrySet() )
    {
      final IFile file = entry.getKey();
      final KeyWrapper wrapper = entry.getValue();
      if( wrapper.getKey().equals( key ) )
      {
        m_workspaces.put( file, new KeyWrapper( key, (GMLWorkspace) newValue, status ) );
        ViewerUtilities.refresh( m_viewer, true );
        return;
      }
    }
  }

}
