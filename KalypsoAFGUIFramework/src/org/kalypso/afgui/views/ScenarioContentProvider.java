package org.kalypso.afgui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class ScenarioContentProvider extends WorkbenchContentProvider implements ICaseManagerListener, IActiveScenarioChangeListener
{
  private final IResourceChangeListener m_resourceListener = new IResourceChangeListener()
  {
    @Override
    public void resourceChanged( final IResourceChangeEvent event )
    {
      final IResourceDelta delta = event.getDelta();
      handleResourceChanged( delta );
    }
  };

  private Viewer m_viewer;

  private final boolean m_showResources;


  public ScenarioContentProvider( )
  {
    this( true );
  }

  public ScenarioContentProvider( final boolean showResources )
  {
    m_showResources = showResources;

    final ActiveWorkContext activeWorkContext = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext();
    activeWorkContext.addActiveContextChangeListener( this );

    ResourcesPlugin.getWorkspace().addResourceChangeListener( m_resourceListener, IResourceChangeEvent.POST_CHANGE );
  }

  @Override
  public Object[] getChildren( final Object parentElement )
  {
    final Object[] children = m_showResources ? super.getChildren( parentElement ) : new Object[] {};

    // TODO: does that really belong here??? should'nt it be more correct to add the resource content provider to the
    // scenario view in order to allow the displayment of resources?

    if( parentElement instanceof IProject )
    {
      final IProject project = (IProject) parentElement;
      if( !project.isOpen() )
      {
        // project is closed or does not exist
        return new Object[0];
      }
      else
      {
        try
        {
          final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNature( project );
          if( nature != null )
          {
            // is of correct nature
            final List<Object> resultList = new ArrayList<>( children.length + 3 );
            resultList.addAll( Arrays.asList( children ) );
            final IScenarioManager caseManager = nature.getCaseManager();
            if( caseManager != null )
            {
              caseManager.addCaseManagerListener( this );
              resultList.addAll( caseManager.getCases() );
            }
            return resultList.toArray();
          }
        }
        catch( final CoreException e )
        {
          // cannot happen, all cases checked?
          e.printStackTrace();
        }
      }
    }
    else if( parentElement instanceof IScenario )
    {
      final IScenario scenario = (IScenario) parentElement;
      final IScenarioList iList = scenario.getDerivedScenarios();
      final List<IScenario> scenarios = iList.getScenarios();

      return scenarios.toArray( new IScenario[] {} );
    }

    return children;
  }

  @Override
  public boolean hasChildren( final Object element )
  {
    final boolean hasChildren = super.hasChildren( element );
    if( hasChildren )
    {
      return true;
    }
    if( element != null && element instanceof IProject )
    {
      final IProject project = (IProject) element;
      if( !project.isOpen() )
      {
        // project is closed or does not exist
        return false;
      }
      else
      {
        try
        {
          final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNature( project );
          if( nature != null )
          {
            final IScenarioManager caseManager = nature.getCaseManager();
            if( caseManager != null )
            {
              final List<IScenario> rootScenarios = caseManager.getCases();
              return rootScenarios != null && !rootScenarios.isEmpty();
            }
          }
        }
        catch( final CoreException e )
        {
          // cannot happen, all cases checked?
          e.printStackTrace();
        }
      }
    }
    else if( element instanceof IScenario )
    {
      final IScenario scenario = (IScenario) element;

      return !scenario.getDerivedScenarios().getScenarios().isEmpty();
    }

    return false;
  }

  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    m_viewer = viewer;
    super.inputChanged( viewer, oldInput, newInput );
  }

  @Override
  public void caseAdded( final IScenario caze )
  {
    refreshViewer( caze );
  }

  @Override
  public void caseRemoved( final IScenario caze )
  {
    refreshViewer( caze );
  }

  @Override
  public void activeScenarioChanged( final ScenarioHandlingProjectNature newProject, final IScenario caze )
  {
    refreshViewer( null );
  }

  private void refreshViewer( final IScenario caze )
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
        final StructuredViewer viewer = (StructuredViewer) m_viewer;
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
    activeWorkContext.removeActiveContextChangeListener( this );
    super.dispose();
  }

  @Override
  public Object[] getElements( final Object element )
  {
    if( element instanceof IResource )
    {
      return super.getElements( element );
    }

    if( !(element instanceof IScenario) )
    {
      return new Object[0];
    }

    final IScenario scenario = (IScenario) element;
    final IScenarioList derivedScenarios = scenario.getDerivedScenarios();
    if( derivedScenarios == null )
    {
      return new Object[0];
    }

    final List<IScenario> scenarios = derivedScenarios.getScenarios();
    if( scenarios == null || scenarios.size() == 0 )
    {
      return new Object[0];
    }

    return scenarios.toArray();
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
      return ((IResource) element).getParent();
    }

    if( element instanceof IScenario )
    {
      final IScenario scenario = (IScenario) element;
      final IScenario parentScenario = scenario.getParentScenario();
      if( parentScenario != null )
        return parentScenario;

      return scenario.getProject();
    }

    return super.getParent( element );
  }
}