package org.kalypso.afgui;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.kalypso.afgui.internal.DefaultTaskActivator;
import org.kalypso.afgui.internal.PerspectiveWatcher;
import org.kalypso.afgui.internal.SzenarioDataProvider;
import org.kalypso.afgui.internal.TaskExecutionAuthority;
import org.kalypso.afgui.internal.TaskExecutor;
import org.kalypso.afgui.internal.workflow.WorkflowView;
import org.kalypso.afgui.perspective.Perspective;
import org.kalypso.afgui.scenarios.ScenarioDataChangeListenerExtension;
import org.kalypso.commons.java.lang.Objects;
import org.osgi.framework.BundleContext;

import de.renew.workflow.connector.cases.IScenarioDataProvider;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.context.ActiveWorkContext;
import de.renew.workflow.connector.context.IActiveScenarioChangeListener;
import de.renew.workflow.connector.worklist.ITaskExecutionAuthority;
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

  private SzenarioDataProvider m_scenarioDataProvider;

  private TaskExecutionAuthority m_taskExecutionAuthority;

  private ITaskExecutor m_taskExecutor;

  private TaskExecutionListener m_taskExecutionListener;

  private final IActiveScenarioChangeListener m_contextChangeListener = new DefaultTaskActivator();

  public KalypsoAFGUIFrameworkPlugin( )
  {
    plugin = this;
  }

  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );

    final WorkflowContextHandlerFactory workflowContextHandlerFactory = new WorkflowContextHandlerFactory();

    if( PlatformUI.isWorkbenchRunning() )
    {
      final IWorkbench workbench = PlatformUI.getWorkbench();
      final IHandlerService handlerService = (IHandlerService)workbench.getService( IHandlerService.class );
      final ICommandService commandService = (ICommandService)workbench.getService( ICommandService.class );

      m_taskExecutionListener = new TaskExecutionListener();
      commandService.addExecutionListener( m_taskExecutionListener );

      m_taskExecutionAuthority = new TaskExecutionAuthority();
      m_taskExecutor = new TaskExecutor( workflowContextHandlerFactory, m_taskExecutionAuthority, commandService, handlerService );

      workbench.addWorkbenchListener( new IWorkbenchListener()
      {
        @Override
        public void postShutdown( final IWorkbench workbench2 )
        {
          handleWorkbenchPostShutdown();
        }

        @Override
        public boolean preShutdown( final IWorkbench workbench2, final boolean forced )
        {
          return handleWorkbenchPreShutdown( forced, workbench2 );
        }
      } );
    }
  }

  boolean handleWorkbenchPreShutdown( final boolean forced, final IWorkbench workbench2 )
  {
    if( forced )
      return false;

    if( !m_taskExecutor.stopActiveTask() )
      return false;

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
    final Collection<String> partsToKeep = new ArrayList<>();
    partsToKeep.add( WorkflowView.ID );
    partsToKeep.add( PerspectiveWatcher.SCENARIO_VIEW_ID );
    PerspectiveWatcher.cleanPerspective( workbench2, partsToKeep );

    return true;
  }

  void handleWorkbenchPostShutdown( )
  {
    stopSzenarioSourceProvider();
  }

  private void startActiveWorkContext( )
  {
    if( m_activeWorkContext == null )
    {
      m_scenarioDataProvider = new SzenarioDataProvider();
      m_activeWorkContext = new ActiveWorkContext( ScenarioHandlingProjectNature.ID, m_scenarioDataProvider );

      m_activeWorkContext.addActiveContextChangeListener( m_contextChangeListener );

      if( PlatformUI.isWorkbenchRunning() )
      {
        // FIXME: must be called in ui thread
        // TODO: why a workspace job at all??
        new WorkspaceJob( "" ) //$NON-NLS-1$
        {
          @Override
          public IStatus runInWorkspace( final IProgressMonitor monitor )
          {
            // register scenario listeners
            ScenarioDataChangeListenerExtension.getInstance();

            return Status.OK_STATUS;
          }
          // TODO: why 5 sec?
        }.schedule( 5000 );
      }
    }
  }

  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    if( PlatformUI.isWorkbenchRunning() )
    {
      final IWorkbench workbench = PlatformUI.getWorkbench();
      if( !workbench.isClosing() )
      {
        final ICommandService commandService = (ICommandService)workbench.getService( ICommandService.class );
        if( commandService != null )
          commandService.removeExecutionListener( m_taskExecutionListener );

        stopSzenarioSourceProvider();
      }
    }

    plugin = null;
    super.stop( context );
  }

  public static ActiveWorkContext getActiveWorkContext( )
  {
    if( plugin == null )
      throw new IllegalStateException();

    plugin.startActiveWorkContext();
    return plugin.m_activeWorkContext;
  }

  public static ITaskExecutor getTaskExecutor( )
  {
    if( plugin == null )
      throw new IllegalStateException();

    return plugin.m_taskExecutor;
  }

  public static ITaskExecutionAuthority getTaskExecutionAuthority( )
  {
    if( plugin == null )
      throw new IllegalStateException();

    return plugin.m_taskExecutionAuthority;
  }

  /**
   * Retrieves the global data provider which gives access to the data of the current scenario.
   */
  public static IScenarioDataProvider getDataProvider( )
  {
    if( plugin == null )
      throw new IllegalStateException();

    plugin.startActiveWorkContext();
    return plugin.m_scenarioDataProvider;
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
        m_activeWorkContext.setCurrentCase( null, new NullProgressMonitor() );
        m_activeWorkContext.removeActiveContextChangeListener( m_contextChangeListener );
      }
      catch( final CoreException e )
      {
        getLog().log( e.getStatus() );
      }
    }

    m_activeWorkContext = null;
  }
}