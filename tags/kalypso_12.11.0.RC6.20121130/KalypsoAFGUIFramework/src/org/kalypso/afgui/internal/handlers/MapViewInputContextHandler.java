/**
 *
 */
package org.kalypso.afgui.internal.handlers;

import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.helper.CatalogStorage;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.widgets.IWidgetManager;
import org.kalypso.ui.views.map.MapView;

import de.renew.workflow.connector.cases.IScenario;

/**
 * Loads a template file in the current map view. Requires that the current context contains the map view. Use a
 * {@link ViewContextHandler} for this purpose.
 *
 * @author Stefan Kurzbach
 */
public class MapViewInputContextHandler extends AbstractHandler
{
  private final String m_url;

  /**
   * Creates a new {@link MapViewInputContextHandler} that loads the given input file
   */
  public MapViewInputContextHandler( final Properties properties )
  {
    m_url = properties.getProperty( KalypsoContextHandlerFactory.PARAM_INPUT );

    Assert.isNotNull( m_url, Messages.getString( "org.kalypso.afgui.handlers.MapViewInputContextHandler.0" ) ); //$NON-NLS-1$
  }

  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    /* project absolute location */
    final IStorageEditorInput input = findInput();

    // find map view
    final IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable( ISources.ACTIVE_WORKBENCH_WINDOW_NAME );
    final IWorkbenchPage activePage = window == null ? null : window.getActivePage();
    final IViewPart view = activePage == null ? null : activePage.findView( MapView.ID );

    if( view == null || !(view instanceof MapView) )
    {
      throw new ExecutionException( Messages.getString( "org.kalypso.afgui.handlers.MapViewInputContextHandler.6" ) ); //$NON-NLS-1$
    }
    else
    {
      // there is a map view and a file
      final MapView mapView = (MapView) view;
      // FIXME: if( mapView.isDirty() )
        mapView.doSave( false, new NullProgressMonitor() );
      mapView.setInput( input );

      final IMapPanel mapPanel = (IMapPanel) mapView.getAdapter( IMapPanel.class );

      // make sure that no theme is active when initializing this context
      final Job unsetActiveThemeJob = new Job( "" ) //$NON-NLS-1$
      {
        @Override
        protected IStatus run( final IProgressMonitor monitor )
        {
          final IMapModell mapModell = mapPanel.getMapModell();
          if( mapModell == null )
          {
            return Status.CANCEL_STATUS;
          }

          mapModell.activateTheme( null );
          return Status.OK_STATUS;
        }
      };
      unsetActiveThemeJob.setRule( mapView.getSchedulingRule().getActivateLayerSchedulingRule() );
      unsetActiveThemeJob.schedule();

      // make sure that no widget is active
      final IWidgetManager widgetManager = mapPanel.getWidgetManager();
      widgetManager.addWidget( null );

      return Status.OK_STATUS;
    }
  }

  private IStorageEditorInput findInput( ) throws ExecutionException
  {
    // TODO: move everything into a helper class, this could be used for all template types.
    if( m_url.startsWith( "project://" ) ) //$NON-NLS-1$
    {
      final String url = m_url.substring( 10 );
      final IProject project = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext().getCurrentCase().getProject();

      final IFile file = project.getFile( url );
      return new FileEditorInput( file );
    }

    /* base scenario relative location */
    if( m_url.startsWith( "base://" ) ) //$NON-NLS-1$
    {
      final String url = m_url.substring( 7 );
      final IScenario caze = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext().getCurrentCase();
      final IScenario root = ScenarioHelper.findRootScenario( caze );

      final IFolder rootFolder = root.getFolder();
      final IFile file = rootFolder.getFile( url );
      return new FileEditorInput( file );
    }

    if( m_url.startsWith( "urn:" ) ) //$NON-NLS-1$
    {
      return createCatalogInput();
    }

    /* current scenario relative location */
    final IContainer folder = ScenarioHelper.getScenarioFolder();
    if( folder == null )
    {
      throw new ExecutionException( Messages.getString( "org.kalypso.afgui.handlers.MapViewInputContextHandler.4" ) ); //$NON-NLS-1$
    }
    else if( m_url == null )
    {
      throw new ExecutionException( Messages.getString( "org.kalypso.afgui.handlers.MapViewInputContextHandler.5" ) ); //$NON-NLS-1$
    }

    // find file in active scenario folder
    final IFile file = folder.getFile( Path.fromPortableString( m_url ) );
    return new FileEditorInput( file );
  }

  private IStorageEditorInput createCatalogInput( ) throws ExecutionException
  {
    try
    {
      final IContainer scenarioFolder = KalypsoAFGUIFrameworkPlugin.getDataProvider().getScenarioFolder();

      return CatalogStorage.createEditorInput( m_url, scenarioFolder );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      throw new ExecutionException( e.getMessage(), e );
    }
  }
}
