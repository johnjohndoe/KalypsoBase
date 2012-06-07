package de.renew.workflow.connector.cases;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.eclipse.core.resources.IContainer;
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

  public CaseHandlingSourceProvider( final ActiveWorkContext context )
  {
    m_activeWorkContext = context;
  }

  public void resetCase( )
  {
    fireSourceChanged( 0, getCurrentState() );
  }

  @Override
  public void dispose( )
  {
    m_activeWorkContext = null;
  }

  @Override
  public Map<String, Object> getCurrentState( )
  {
    final Map<String, Object> currentState = new TreeMap<String, Object>();
    currentState.put( ACTIVE_CASE_FOLDER_NAME, getSzenarioFolder() );
    currentState.put( ACTIVE_CASE_DATA_PROVIDER_NAME, m_activeWorkContext.getDataProvider() );
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
    if( currentCase == null )
      return null;

    return currentCase.getFolder();
  }
}