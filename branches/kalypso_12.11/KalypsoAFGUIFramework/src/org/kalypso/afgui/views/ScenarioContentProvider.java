package org.kalypso.afgui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;

import de.renew.workflow.connector.cases.ICaseManagerListener;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioList;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.context.ActiveWorkContext;
import de.renew.workflow.connector.context.IActiveScenarioChangeListener;

/**
 * @author Stefan Kurzbach
 */
public class ScenarioContentProvider extends WorkbenchContentProvider
{
  private final IActiveScenarioChangeListener m_activeScenarioListener = new IActiveScenarioChangeListener()
  {
    @Override
    public void activeScenarioChanged( final ScenarioHandlingProjectNature newProject, final IScenario caze )
    {
      refreshViewer( null );
    }
  };

  private final ICaseManagerListener m_caseManagerListener = new ICaseManagerListener()
  {
    @Override
    public void caseRemoved( final IScenario scenario )
    {
      refreshViewer( scenario );
    }

    @Override
    public void caseAdded( final IScenario scenario )
    {
      refreshViewer( scenario );
    }
  };

  private final IResourceChangeListener m_resourceListener = new IResourceChangeListener()
  {
    @Override
    public void resourceChanged( final IResourceChangeEvent event )
    {
      final IResourceDelta delta = event.getDelta();
      handleResourceChanged( delta );
    }
  };

  private final Map<IProject, IScenarioManager> m_cachedManagers = new HashMap<>();

  private final boolean m_showResources;

  private Viewer m_viewer;

  public ScenarioContentProvider( )
  {
    this( true );
  }

  public ScenarioContentProvider( final boolean showResources )
  {
    m_showResources = showResources;

    final ActiveWorkContext activeWorkContext = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext();
    activeWorkContext.addActiveContextChangeListener( m_activeScenarioListener );

    ResourcesPlugin.getWorkspace().addResourceChangeListener( m_resourceListener, IResourceChangeEvent.POST_CHANGE );
  }

  @Override
  public Object[] getChildren( final Object parentElement )
  {
    final Object[] children = m_showResources ? super.getChildren( parentElement ) : new Object[] {};

    if( parentElement instanceof IProject )
    {
      final IProject project = (IProject)parentElement;
      if( !project.isOpen() )
      {
        // project is closed or does not exist
        return new Object[0];
      }

      final IScenarioManager caseManager = getCaseManager( project );
      if( caseManager == null )
        return ArrayUtils.EMPTY_OBJECT_ARRAY;

      // is of correct nature
      final List<Object> resultList = new ArrayList<>( children.length + 3 );
      resultList.addAll( Arrays.asList( children ) );

      resultList.addAll( caseManager.getCases() );

      return resultList.toArray();
    }

    if( parentElement instanceof IScenario )
    {
      final IScenario scenario = (IScenario)parentElement;
      return getSortedChildScenarios( scenario );
    }

    return children;
  }

  private synchronized IScenarioManager getCaseManager( final IProject project )
  {
    if( m_cachedManagers.containsKey( project ) )
      return m_cachedManagers.get( project );

    try
    {
      final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNature( project );
      if( nature == null )
        return null;

      // FIXME: never removed, resource leak!
      final IScenarioManager manager = nature.getCaseManager();
      m_cachedManagers.put( project, manager );

      manager.addCaseManagerListener( m_caseManagerListener );

      return manager;
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  private Object[] getSortedChildScenarios( final IScenario scenario )
  {
    final IScenarioList derivedScenarios = scenario.getDerivedScenarios();
    if( derivedScenarios == null )
      return new Object[0];

    final List<IScenario> scenarios = derivedScenarios.getScenarios();
    if( scenarios == null || scenarios.size() == 0 )
      return new Object[0];

    final IScenario[] children = scenarios.toArray( new IScenario[scenarios.size()] );
    // REMARK: sort by name here, as we do not always have access to the viewer in order so set a comparator there.
    Arrays.sort( children, new ScenarioNameComparator() );

    return children;
  }

  @Override
  public boolean hasChildren( final Object element )
  {
    final boolean hasChildren = super.hasChildren( element );

    if( hasChildren )
      return true;

    final Object[] children = getChildren( element );
    return children.length > 0;
  }

  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    m_viewer = viewer;

    super.inputChanged( viewer, oldInput, newInput );
  }

  void refreshViewer( final IScenario caze )
  {
    if( m_viewer instanceof StructuredViewer )
    {
      if( caze == null )
      {
        ViewerUtilities.refresh( m_viewer, true );
      }
      else
      {
        final IProject project = caze.getProject();
        final StructuredViewer viewer = (StructuredViewer)m_viewer;
        final IScenario parentScenario = caze.getParentScenario();
        if( parentScenario != null )
        {
          ViewerUtilities.refresh( viewer, parentScenario, true );
        }
        else
        {
          if( project != null )
          {
            ViewerUtilities.refresh( viewer, project, true );
          }
        }

        final IFolder folder = caze.getFolder();
        ViewerUtilities.refresh( viewer, folder.getParent(), true );
      }
    }
  }

  @Override
  public void dispose( )
  {
    final ActiveWorkContext activeWorkContext = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext();
    activeWorkContext.removeActiveContextChangeListener( m_activeScenarioListener );

    for( final IScenarioManager manager : m_cachedManagers.values() )
      manager.removeCaseManagerListener( m_caseManagerListener );

    super.dispose();
  }

  @Override
  public Object[] getElements( final Object element )
  {
    if( element instanceof IResource )
      return super.getElements( element );

    if( !(element instanceof IScenario) )
      return new Object[0];

    return getSortedChildScenarios( (IScenario)element );
  }

  protected void handleResourceChanged( final IResourceDelta delta )
  {
    final boolean shouldRefresh = checkResourceDelta( delta );
    if( shouldRefresh )
      refreshViewer( null );
  }

  private boolean checkResourceDelta( final IResourceDelta delta )
  {
    // REMARK: for the moment, we refresh on any resource change of a project
    // We might refresh on any change that changes the sceanrio stuff as well (change of nature, etc.)

    final IResource resource = delta.getResource();
    if( resource instanceof IWorkspaceRoot )
      return true;

    return false;
  }

  @Override
  public Object getParent( final Object element )
  {
    if( element instanceof IResource )
    {
      return ((IResource)element).getParent();
    }

    if( element instanceof IScenario )
    {
      final IScenario scenario = (IScenario)element;
      final IScenario parentScenario = scenario.getParentScenario();
      if( parentScenario != null )
        return parentScenario;

      return scenario.getProject();
    }

    return super.getParent( element );
  }
}