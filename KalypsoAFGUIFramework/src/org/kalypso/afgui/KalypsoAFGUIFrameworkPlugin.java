package org.kalypso.afgui;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.services.IEvaluationService;
import org.kalypso.afgui.i18n.Messages;
import org.kalypso.afgui.model.IModel;
import org.kalypso.afgui.perspective.Perspective;
import org.kalypso.afgui.scenarios.PerspectiveWatcher;
import org.kalypso.afgui.scenarios.ScenarioDataChangeListenerExtension;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.afgui.scenarios.SzenarioDataProvider;
import org.kalypso.afgui.scenarios.TaskExecutionAuthority;
import org.kalypso.afgui.scenarios.TaskExecutor;
import org.kalypso.afgui.views.WorkflowView;
import org.kalypso.commons.java.lang.Objects;
import org.osgi.framework.BundleContext;

import de.renew.workflow.base.ITask;
import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.connector.cases.CaseHandlingProjectNature;
import de.renew.workflow.connector.cases.CaseHandlingSourceProvider;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.context.ActiveWorkContext;
import de.renew.workflow.connector.context.IActiveScenarioChangeListener;
import de.renew.workflow.connector.worklist.ITaskExecutor;
import de.renew.workflow.connector.worklist.TaskExecutionListener;
import de.renew.workflow.contexts.WorkflowContextHandlerFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class KalypsoAFGUIFrameworkPlugin extends AbstractUIPlugin
{

  // The plug-in ID
  public static final String PLUGIN_ID = "org.kalypso.afgui"; //$NON-NLS-1$

  // The shared instance
  private static KalypsoAFGUIFrameworkPlugin plugin;

  private ActiveWorkContext m_activeWorkContext;

  private CaseHandlingSourceProvider<IModel> m_szenarioSourceProvider;

  private SzenarioDataProvider m_szenarioDataProvider;

  private TaskExecutionAuthority m_taskExecutionAuthority;

  private ITaskExecutor m_taskExecutor;

  private TaskExecutionListener m_taskExecutionListener;

  // Executes the default task as soon as the scenario was activated
  private final IActiveScenarioChangeListener m_activeContextChangeListener = new IActiveScenarioChangeListener()
  {
    @Override
    public void activeScenarioChanged( final CaseHandlingProjectNature newProject, final IScenario caze )
    {
      handleScenarioChanged( newProject, caze );
    }
  };

  public KalypsoAFGUIFrameworkPlugin( )
  {
    plugin = this;
  }

  /**
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );

    final WorkflowContextHandlerFactory workflowContextHandlerFactory = new WorkflowContextHandlerFactory();

    if( PlatformUI.isWorkbenchRunning() )
    {
      final IWorkbench workbench = PlatformUI.getWorkbench();
      final IHandlerService handlerService = (IHandlerService) workbench.getService( IHandlerService.class );
      final ICommandService commandService = (ICommandService) workbench.getService( ICommandService.class );
      m_taskExecutionListener = new TaskExecutionListener();
      commandService.addExecutionListener( m_taskExecutionListener );
      m_taskExecutionAuthority = new TaskExecutionAuthority();
      m_taskExecutor = new TaskExecutor( workflowContextHandlerFactory, m_taskExecutionAuthority, commandService, handlerService );

      workbench.addWorkbenchListener( new IWorkbenchListener()
      {
        /**
         * @see org.eclipse.ui.IWorkbenchListener#postShutdown(org.eclipse.ui.IWorkbench)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void postShutdown( final IWorkbench workbench2 )
        {
          stopSzenarioSourceProvider();
        }

        /**
         * @see org.eclipse.ui.IWorkbenchListener#preShutdown(org.eclipse.ui.IWorkbench, boolean)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public boolean preShutdown( final IWorkbench workbench2, final boolean forced )
        {
          if( !forced && m_taskExecutionAuthority.canStopTask( m_taskExecutor.getActiveTask() ) )
          {
            // IMPORTAN: only close views on workflow perspective
            final IWorkbenchWindow window = workbench2.getActiveWorkbenchWindow();
            if( Objects.isNull( window ) )
              return true;

            final IWorkbenchPage activePage = window.getActivePage();
            if( Objects.isNull( activePage ) )
              return true;

            final IPerspectiveDescriptor perspective = activePage.getPerspective();
            if( !ObjectUtils.equals( perspective.getId(), Perspective.ID ) )
              return true;

            // FIXME: check if this really is still needed. All views of workflow should not load any data upon start
            // of workbench
            // So the views may open, and the default task will close/open the needed views anyways.

            // Close all views previously opened by any task in order to let them save themselves
            final Collection<String> partsToKeep = new ArrayList<String>();
            partsToKeep.add( WorkflowView.ID );
            partsToKeep.add( PerspectiveWatcher.SCENARIO_VIEW_ID );
            PerspectiveWatcher.cleanPerspective( workbench2, partsToKeep );

            m_taskExecutor.stopActiveTask();
            return true;
          }
          else
            return false;
        }
      } );
    }
  }

  private void startActiveWorkContext( )
  {
    if( m_activeWorkContext == null )
    {
      m_activeWorkContext = new ActiveWorkContext( ScenarioHandlingProjectNature.ID );
      m_activeWorkContext.addActiveContextChangeListener( m_activeContextChangeListener );
    }

    if( m_szenarioSourceProvider == null )
    {
      // This can only be called if the platform has already been started
      m_szenarioDataProvider = new SzenarioDataProvider();
      m_szenarioSourceProvider = new CaseHandlingSourceProvider<IModel>( m_activeWorkContext, m_szenarioDataProvider );

      if( PlatformUI.isWorkbenchRunning() )
      {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IEvaluationService evalService = (IEvaluationService) workbench.getService( IEvaluationService.class );
        // FIXME: must be called in ui thread
        evalService.addSourceProvider( m_szenarioSourceProvider );

        new WorkspaceJob( "" ) //$NON-NLS-1$
        {
          @Override
          public IStatus runInWorkspace( final IProgressMonitor monitor )
          {
            // register sceanrio listeners
            ScenarioDataChangeListenerExtension.getInstance();

            return Status.OK_STATUS;
          }
        }.schedule( 5000 );
      }
    }
  }

  /**
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    if( m_activeWorkContext != null )
    {
      m_activeWorkContext.removeActiveContextChangeListener( m_activeContextChangeListener );
    }

    if( PlatformUI.isWorkbenchRunning() )
    {
      final IWorkbench workbench = PlatformUI.getWorkbench();
      if( !workbench.isClosing() )
      {
        final IHandlerService handlerService = (IHandlerService) workbench.getService( IHandlerService.class );
        if( handlerService != null )
        {
          handlerService.removeSourceProvider( m_szenarioSourceProvider );
        }
        final ICommandService commandService = (ICommandService) workbench.getService( ICommandService.class );
        if( commandService != null )
        {
          commandService.removeExecutionListener( m_taskExecutionListener );
        }
        stopSzenarioSourceProvider();
      }
    }

    plugin = null;
    super.stop( context );
  }

  public ActiveWorkContext getActiveWorkContext( )
  {
    startActiveWorkContext();
    return m_activeWorkContext;
  }

  public ITaskExecutor getTaskExecutor( )
  {
    return m_taskExecutor;
  }

  public TaskExecutionAuthority getTaskExecutionAuthority( )
  {
    return m_taskExecutionAuthority;
  }

  public SzenarioDataProvider getDataProvider( )
  {
    startActiveWorkContext();
    return m_szenarioDataProvider;
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static KalypsoAFGUIFrameworkPlugin getDefault( )
  {
    return plugin;
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in relative path
   *
   * @param path
   *          the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor( final String path )
  {
    return imageDescriptorFromPlugin( PLUGIN_ID, path );
  }

  private void stopSzenarioSourceProvider( )
  {
    if( PlatformUI.isWorkbenchRunning() && m_activeWorkContext != null )
    {
      try
      {
        m_activeWorkContext.setCurrentCase( null );
      }
      catch( final CoreException e )
      {
        getLog().log( e.getStatus() );
      }
    }

    m_activeWorkContext = null;
  }

  // FIXME: move this into scenari oactivation code; should not be handled via listeners, probably only works because
  // this listener is always the first one to be executed...
  protected void handleScenarioChanged( final CaseHandlingProjectNature nature, final IScenario caze )
  {
    // First initialize the context (and loading of all the models); else the default task does not work
    // REMARK: normally, this should be done inside the scenario framework (for example at the activeWorkContext)
    // But this is not possible because of the current dependencies between these code parts
    m_szenarioSourceProvider.resetCase();

    // Then execute default task
    final IWorkflow workflow = ScenarioHelper.findWorkflow( caze, nature );
    // lazy check and insurance for backwards compatibility
    ScenarioHelper.ensureBackwardsCompatibility( nature );

    final ITask defaultTask = workflow == null ? null : workflow.getDefaultTask();
    if( defaultTask != null )
    {
      final UIJob job = new UIJob( Messages.getString( "org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin.1" ) + nature.getProject().getName() + " - " + caze.getName() ) //$NON-NLS-1$ //$NON-NLS-2$
      {
        @Override
        public IStatus runInUIThread( final IProgressMonitor monitor )
        {
          return getTaskExecutor().execute( defaultTask );
        }
      };
      job.setUser( true );
      job.schedule();
    }
  }
}
