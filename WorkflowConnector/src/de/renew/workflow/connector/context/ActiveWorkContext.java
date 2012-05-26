package de.renew.workflow.connector.context;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.renew.workflow.connector.cases.CaseHandlingProjectNature;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;

/**
 * Represents the work context for a user.
 *
 * @author Stefan Kurzbach
 */
public class ActiveWorkContext
{
  private final ActiveWorkContextResourceListener m_resourceListener = new ActiveWorkContextResourceListener( this );

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
    workspace.addResourceChangeListener( m_resourceListener );
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
}