package de.renew.workflow.connector.cases;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.AbstractSourceProvider;

import de.renew.workflow.connector.context.ActiveWorkContext;
import de.renew.workflow.contexts.ICaseHandlingSourceProvider;

public class CaseHandlingSourceProvider extends AbstractSourceProvider implements ICaseHandlingSourceProvider
{
  private static final Logger LOGGER = Logger.getLogger( CaseHandlingSourceProvider.class.getName() );

  static
  {
    final boolean log = Boolean.parseBoolean( Platform.getDebugOption( "org.kalypso.kalypso1d2d.pjt/debug" ) );
    if( !log )
    {
      LOGGER.setUseParentHandlers( false );
    }
  }

  private static final String[] PROVIDED_SOURCE_NAMES = new String[] { ACTIVE_CASE_FOLDER_NAME, ACTIVE_CASE_DATA_PROVIDER_NAME, ACTIVE_CASE_URI_NAME };

  protected ActiveWorkContext m_activeWorkContext;

  /** data provider for the current szenario */
  private IScenarioDataProvider m_dataProvider;

  public CaseHandlingSourceProvider( final ActiveWorkContext context, final IScenarioDataProvider dataProvider )
  {
    m_activeWorkContext = context;
    m_dataProvider = dataProvider;
  }

  public void resetCase( )
  {
    // FIXME: move into scenario activation code
    m_dataProvider.setCurrent( m_activeWorkContext.getCurrentCase() );

    fireSourceChanged( 0, getCurrentState() );
  }

  @Override
  public void dispose( )
  {
    m_activeWorkContext = null;
    m_dataProvider = null;
  }

  @Override
  public Map<String, Object> getCurrentState( )
  {
    final Map<String, Object> currentState = new TreeMap<String, Object>();
    currentState.put( ACTIVE_CASE_FOLDER_NAME, getSzenarioFolder() );
    currentState.put( ACTIVE_CASE_DATA_PROVIDER_NAME, getDataProvider() );
    currentState.put( ACTIVE_CASE_URI_NAME, getSzenarioUri() );
    return currentState;
  }

  @Override
  public String[] getProvidedSourceNames( )
  {
    return PROVIDED_SOURCE_NAMES;
  }

  private String getSzenarioUri( )
  {
    final IScenario currentCase = m_activeWorkContext.getCurrentCase();
    if( currentCase == null )
      return null;

    return currentCase.getURI();
  }

  private IContainer getSzenarioFolder( )
  {
    final IScenario currentCase = m_activeWorkContext.getCurrentCase();
    final CaseHandlingProjectNature currentProject = m_activeWorkContext.getCurrentProject();
    if( currentProject == null || currentCase == null )
      return null;

    // TODO: is this really up to date? We always assume that the scenarioFolder is a IFolder
    // TODO: comment why we need that
    final IPath projectPath = currentProject.getRelativeProjectPath( currentCase );
    if( projectPath.isEmpty() )
      return currentProject.getProject();

    return currentProject.getProject().getFolder( projectPath );
  }

  private IScenarioDataProvider getDataProvider( )
  {
    return m_dataProvider;
  }
}