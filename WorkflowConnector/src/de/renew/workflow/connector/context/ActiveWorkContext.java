package de.renew.workflow.connector.context;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import de.renew.workflow.connector.cases.CaseHandlingProjectNature;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;

/**
 * Represents the work context for a user.
 *
 * @author Stefan Kurzbach
 */
public class ActiveWorkContext implements IResourceChangeListener
{
  private CaseHandlingProjectNature m_currentProjectNature;

  private final List<IActiveScenarioChangeListener> m_activeContextChangeListeners = new ArrayList<IActiveScenarioChangeListener>();

  private final String m_natureID;

  /**
   * Creates a new work context and restores the previous state from the given properties
   */
  public ActiveWorkContext( final String natureID )
  {
    m_natureID = natureID;

    // TODO: is this really the right place to do this stuff?
    // Probably better at least inside the afg-ui plug-in
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener( this );
  }

  /**
   * Sets the active case handling project
   */
  private void setCurrentProject( final CaseHandlingProjectNature nature )
  {
    if( m_currentProjectNature == nature )
      return;

    final IProject project = nature == null ? null : nature.getProject();

    // FIXME: set caze of old case manager to null in order to unload data
    if( m_currentProjectNature != null )
    {
      // Deactivate the current case here, if the project changes, as each project has a separate caseManager
      final IProject currentProject = m_currentProjectNature.getProject();
      if( !ObjectUtils.equals( project, currentProject ) )
        m_currentProjectNature.getCaseManager().setCurrentCase( null );
    }

    if( nature == null )
    {
      m_currentProjectNature = null;
      return;
    }

    m_currentProjectNature = nature;
  }

  public CaseHandlingProjectNature getCurrentProject( )
  {
    return m_currentProjectNature;
  }

  /**
   * The same as {@link #getCaseManager()#getCurrentCase()}
   */
  public IScenario getCurrentCase( )
  {
    final IScenarioManager caseManager = getCaseManager();
    if( caseManager == null )
      return null;

    return caseManager.getCurrentCase();
  }

  private IScenarioManager getCaseManager( )
  {
    if( m_currentProjectNature == null )
      return null;

    return m_currentProjectNature.getCaseManager();
  }

  public void addActiveContextChangeListener( final IActiveScenarioChangeListener l )
  {
    m_activeContextChangeListeners.add( l );
  }

  public void removeActiveContextChangeListener( final IActiveScenarioChangeListener l )
  {
    m_activeContextChangeListeners.remove( l );
  }

  protected void fireActiveContextChanged( final CaseHandlingProjectNature newProject, final IScenario caze )
  {
    // Convert to array to avoid concurrent modification exceptions
    final IActiveScenarioChangeListener[] listeners = m_activeContextChangeListeners.toArray( new IActiveScenarioChangeListener[m_activeContextChangeListeners.size()] );
    for( final IActiveScenarioChangeListener l : listeners )
    {
      l.activeScenarioChanged( newProject, caze );
    }
  }

  // TODO: do this in ui thread + monitor
  public synchronized void setCurrentCase( final IScenario caze ) throws CoreException
  {
    final IScenarioManager currentCaseManager = getCaseManager();
    final IScenario currentCase = currentCaseManager == null ? null : currentCaseManager.getCurrentCase();
    if( currentCase == null && caze == null )
      return;

    if( caze != null && currentCase != null && currentCase.getURI().equals( caze.getURI() ) && currentCase.getProject().equals( caze.getProject() ) )
      return;

    // Set current project to the cases project
    if( caze == null )
      setCurrentProject( null );
    else
    {
      final IProject project = caze.getProject();
      if( project.exists() && project.isOpen() )
      {
        final CaseHandlingProjectNature nature = (CaseHandlingProjectNature) project.getNature( m_natureID );
        setCurrentProject( nature );
      }
      else
      {
        throw new CoreException( new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, "Das Projekt " + project.getName() + " für den Case " + caze.getName() + " existiert nicht." ) );
      }
    }

    final IScenarioManager newCaseManager = getCaseManager();
    if( newCaseManager != null )
      newCaseManager.setCurrentCase( caze );

    fireActiveContextChanged( m_currentProjectNature, caze );
  }

  @Override
  public void resourceChanged( final IResourceChangeEvent event )
  {
    // TODO:
    // /* Set base case as current for the newly selected project */
    // final T caseToActivate = m_caseManager == null ? null : m_caseManager.getCases().get( 0 );

    if( event.getType() == IResourceChangeEvent.PRE_DELETE || event.getType() == IResourceChangeEvent.PRE_CLOSE )
    {
      // if the currently active project is deleted or closed
      if( m_currentProjectNature != null && m_currentProjectNature.getProject().equals( event.getResource() ) )
      {
        try
        {
          // project was closed or deleted, deactivate the current case
          setCurrentCase( null );
        }
        catch( final CoreException e )
        {
          final Display display = PlatformUI.getWorkbench().getDisplay();
          final Shell activeShell = display.getActiveShell();
          final IStatus status = e.getStatus();
          ErrorDialog.openError( activeShell, "Problem beim Löschen des Projektes", "Projekt wurde nicht deaktiviert.", status );
          WorkflowConnectorPlugin.getDefault().getLog().log( status );
        }
      }
    }
    else if( event.getType() == IResourceChangeEvent.POST_CHANGE )
    {
      // TODO: does not work for new projects (they are activated anyway!)
      // Symptoms: dialog pops up, but is empty; NPE after that

      // // post change event after some modifications
      // final IResourceDelta delta = event.getDelta();
      // final IResourceDelta[] openedChildren = delta.getAffectedChildren();
      //
      // if( openedChildren.length == 0 )
      // return;
      //
      // final IResourceDelta resourceDelta = openedChildren[0];
      // if( (resourceDelta.getFlags() & (IResourceDelta.OPEN | IResourceDelta.ADDED)) == 0 )
      // return;
      //
      // // if the first affected resource was opened or added, remember this resource
      // final IResource resource = resourceDelta.getResource();
      // if( resource.getType() != IResource.PROJECT )
      // return;
      //
      // // this is an opened or added resource, if it is a project that was opened or added, go on
      // final IProject project = (IProject) resource;
      // if( !project.isOpen() )
      // return;
      //
      // IProjectNature nature = null;
      // try
      // {
      // // get the case handling project nature if available
      // nature = project.getNature( m_natureID );
      // }
      // catch( final CoreException e )
      // {
      // // nature does not exist or such, ignore
      // return;
      // }
      //
      // final CaseHandlingProjectNature caseHandlingNature = (CaseHandlingProjectNature) nature;
      // if( caseHandlingNature == null && m_currentProjectNature != null && project.equals(
      // m_currentProjectNature.getProject() ) )
      // {
      // try
      // {
      // // TODO: when does this ever happen? a new project was opened, and is already active
      //
      // // nature is null, so it must have been removed from this project: close the scenario
      // setCurrentProject( null );
      // }
      // catch( final CoreException e )
      // {
      // final IStatus status = e.getStatus();
      // WorkflowConnectorPlugin.getDefault().getLog().log( status );
      // }
      // return;
      // }
      //
      // final UIJob job = new UIJob( "" )
      // {
      // @Override
      // public IStatus runInUIThread( IProgressMonitor monitor )
      // {
      // final Shell shell = getDisplay().getActiveShell();
      // {
      // try
      // {
      // // A project was opened and has the right nature: ask user if it should be activated as well
      //
      // final WorkbenchContentProvider workbenchContentProvider = new WorkbenchContentProvider();
      // final WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();
      // final ListDialog dialog = new ListDialog( shell );
      // dialog.setTitle( "Szenario-Projekt" );
      // dialog.setMessage( String.format( "Das Projekt \"%s\" enthält Szenarien.\nMöchten Sie ein Szenario
      // aktivieren?", project.getName() ) );
      // dialog.setContentProvider( workbenchContentProvider );
      // dialog.setLabelProvider( workbenchLabelProvider );
      // dialog.setInput( caseHandlingNature );
      // dialog.setBlockOnOpen( true );
      //
      // dialog.open();
      // final Object[] result = dialog.getResult();
      // if( result != null && result.length > 0 )
      // {
      // final T caze = (T) result[0];
      // setCurrentProject( caseHandlingNature );
      // setCurrentCase( caze );
      // }
      // }
      // catch( final CoreException e )
      // {
      // final IStatus status = e.getStatus();
      // ErrorDialog.openError( shell, "Problem beim Öffnen des Projektes", "Projekt wurde nicht aktiviert.", status );
      // WorkflowConnectorPlugin.getDefault().getLog().log( status );
      // }
      // }
      // return null;
      // }
      // };
      // job.schedule();
    }
  }
}
