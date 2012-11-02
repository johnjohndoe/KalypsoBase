package de.renew.workflow.connector.context;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioDataProvider;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;
import de.renew.workflow.connector.internal.cases.ScenarioCompatibilityHelper;
import de.renew.workflow.connector.internal.i18n.Messages;

/**
 * Represents the work context for a user.
 *
 * @author Stefan Kurzbach
 */
public class ActiveWorkContext
{
  private final ActiveWorkContextResourceListener m_resourceListener = new ActiveWorkContextResourceListener( this );

  private ScenarioHandlingProjectNature m_currentProjectNature;

  private final List<IActiveScenarioChangeListener> m_activeScenarioChangeListeners = new ArrayList<>();

  private final String m_natureID;

  /** data provider for the current scenario */
  private final IScenarioDataProvider m_dataProvider;

  /**
   * Creates a new work context and restores the previous state from the given properties
   */
  public ActiveWorkContext( final String natureID, final IScenarioDataProvider dataProvider )
  {
    m_natureID = natureID;
    m_dataProvider = dataProvider;

    // TODO: is this really the right place to do this stuff?
    // Probably better at least inside the afg-ui plug-in
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener( m_resourceListener );
  }

  /**
   * Sets the active case handling project
   */
  private void setCurrentProject( final ScenarioHandlingProjectNature nature )
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

    /* ensure backwards compatibility */
    // FIXME: still a bad place, but at least this happens now only once
    // lazy check and insurance for backwards compatibility
    ScenarioCompatibilityHelper.ensureBackwardsCompatibility( m_currentProjectNature );
  }

  public ScenarioHandlingProjectNature getCurrentProject( )
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
    m_activeScenarioChangeListeners.add( l );
  }

  public void removeActiveContextChangeListener( final IActiveScenarioChangeListener l )
  {
    m_activeScenarioChangeListeners.remove( l );
  }

  protected void fireActiveContextChanged( final ScenarioHandlingProjectNature newProject, final IScenario caze )
  {
    // Convert to array to avoid concurrent modification exceptions
    final IActiveScenarioChangeListener[] listeners = m_activeScenarioChangeListeners.toArray( new IActiveScenarioChangeListener[m_activeScenarioChangeListeners.size()] );
    for( final IActiveScenarioChangeListener l : listeners )
    {
      l.activeScenarioChanged( newProject, caze );
    }
  }

  public synchronized void setCurrentCase( final IScenario caze, final IProgressMonitor monitor ) throws CoreException
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
        final ScenarioHandlingProjectNature nature = (ScenarioHandlingProjectNature) project.getNature( m_natureID );
        setCurrentProject( nature );
      }
      else
      {
        final String message = String.format( Messages.getString("ActiveWorkContext_0"), project.getName(), caze.getName() ); //$NON-NLS-1$
        throw new CoreException( new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, message ) );
      }
    }

    final IScenarioManager newCaseManager = getCaseManager();
    if( newCaseManager != null )
      newCaseManager.setCurrentCase( caze );

    /* Load data */
    m_dataProvider.setCurrent( getCurrentCase(), monitor );

    fireActiveContextChanged( m_currentProjectNature, caze );
  }

  public IScenarioDataProvider getDataProvider( )
  {
    return m_dataProvider;
  }
}