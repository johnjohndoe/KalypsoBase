package org.kalypso.module.project.local.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.kalypso.module.project.IProjectHandle;
import org.kalypso.module.project.local.AbstractProjectHandleProvider;
import org.kalypso.module.project.local.LocalProjectHandle;

/**
 * @author Dirk Kuch
 */
public class LocalWorkspaceItemProvider extends AbstractProjectHandleProvider
{
  private final IResourceChangeListener m_resourceListener = new IResourceChangeListener()
  {
    @Override
    public void resourceChanged( final IResourceChangeEvent event )
    {
      final IResource eventResource = event.getResource();
      if( eventResource instanceof IProject )
        fireItemsChanged();
      else
      {
        final IResourceDelta delta = event.getDelta();
        final IResource resource = delta.getResource();
        if( resource instanceof IProject || resource instanceof IWorkspaceRoot )
          fireItemsChanged();
      }
    }
  };

  public LocalWorkspaceItemProvider( )
  {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener( m_resourceListener );
  }

  @Override
  public void dispose( )
  {
    super.dispose();

    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.removeResourceChangeListener( m_resourceListener );
  }

  @Override
  public IProjectHandle[] getProjects( )
  {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IWorkspaceRoot root = workspace.getRoot();

    final IProject[] projects = root.getProjects();
    final Collection<IProjectHandle> items = new ArrayList<>( projects.length );
    for( final IProject project : projects )
    {
      if( project.isAccessible() && project.isOpen() )
        items.add( new LocalProjectHandle( project ) );
    }

    return items.toArray( new IProjectHandle[items.size()] );
  }

// /**
// * @see org.kalypso.project.database.client.core.model.interfaces.ILocalWorkspaceModel#getProject(java.lang.String)
// */
// @Override
// public ILocalProject getProject( final String projectReference )
// {
//    if( projectReference == null || "".equals( projectReference.trim() ) ) //$NON-NLS-1$
// return null;
//
// final ILocalProject[] projects = getProjects();
// return resolveProject( projects, projectReference );
// }

// private ILocalProject resolveProject( final ILocalProject[] projects, final String projectReference )
// {
// for( final ILocalProject project : projects )
// {
// if( project.getName().equals( projectReference ) )
// return project;
//
// final String path = project.getProject().getFullPath().toString();
// if( path.equals( projectReference ) )
// return project;
// }
//
// return null;
// }

// /**
// * @see
// org.kalypso.project.database.client.core.model.interfaces.ILocalWorkspaceModel#getProject(org.kalypso.project.database.client.extension.IProjectDatabaseFilter,
// * java.lang.String)
// */
// @Override
// public ILocalProject getProject( final IProjectDatabaseFilter filter, final String projectReference )
// {
// final ILocalProject[] projects = getProjects( filter );
//
// return resolveProject( projects, projectReference );
// }
}
