package org.kalypso.project.database.client;

import javax.xml.ws.WebServiceException;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.project.database.client.core.model.ProjectDatabaseModel;
import org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel;
import org.kalypso.project.database.client.extension.database.IProjectDataBaseClientConstant;
import org.kalypso.project.database.sei.IProjectDatabase;
import org.kalypso.project.database.sei.ProjectDatabaseServiceLocator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class KalypsoProjectDatabaseClient extends AbstractUIPlugin
{
  private ProjectDatabaseModel PROJECT_DATABASE_MODEL = null;

  private static IProjectDatabase m_service = null;

  /**
   * Returns the database-service.<br>
   * The first call to this method blocks until the service instance has been created. Can take some time when the
   * remote connection is not available.<br>
   * Use {@link #getServiceUnblocking()}, if this is not acceptable.
   * 
   * @see #getServiceUnblocking()
   */
  public static synchronized IProjectDatabase getService( ) throws WebServiceException
  {
    if( m_service == null )
    {
      try
      {
        final String wsdlLocationProperty = System.getProperty( IProjectDataBaseClientConstant.SERVER_WSDL_LOCATION );
        m_service = ProjectDatabaseServiceLocator.locate( wsdlLocationProperty );
      }
      catch( final Throwable e )
      {
        // TODO trace option
      }
    }

    return m_service;
  }

  public static IProjectDatabaseModel getModel( )
  {
    return getDefault().getProjectDatabaseModel();
  }

  /**
   * Returns the database-service.<br>
   * Does not block but returns the current available service. If the service is not yet available (see
   * {@link #getService()}, <code>null</code> is returned.<br>
   * 
   * @see #getService()
   */
  public static IProjectDatabase getServiceUnblocking( )
  {
    return m_service;
  }

  // The plug-in ID
  public static final String PLUGIN_ID = "org.kalypso.project.database.client"; //$NON-NLS-1$

  // The shared instance
  private static KalypsoProjectDatabaseClient plugin;

  /**
   * The constructor
   */
  public KalypsoProjectDatabaseClient( )
  {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    plugin = null;
    super.stop( context );
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static KalypsoProjectDatabaseClient getDefault( )
  {
    return plugin;
  }

  private IProjectDatabaseModel getProjectDatabaseModel( )
  {
    /* don't implement ProjectdatabaseModel() as Singleton, perhaps we have to flexibilise the model in future */
    if( PROJECT_DATABASE_MODEL == null )
    {
      PROJECT_DATABASE_MODEL = new ProjectDatabaseModel();
    }

    return PROJECT_DATABASE_MODEL;
  }
}
