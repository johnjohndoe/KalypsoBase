package org.kalypso.project.database.client;

import javax.xml.ws.WebServiceException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.project.database.client.core.model.ProjectDatabaseModel;
import org.kalypso.project.database.client.extension.database.IProjectDataBaseClientConstant;
import org.kalypso.project.database.sei.IProjectDatabase;
import org.kalypso.project.database.sei.ProjectDatabaseServiceLocator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class KalypsoProjectDatabaseClient extends AbstractUIPlugin
{
  public static final Color COLOR_WELCOME_PAGE_HEADING = new Color( null, 0x99, 0xB4, 0xCE );

  // FIXME: bad static font/colors... never disposed: we should use an eclipse font in any case...
  static public final Font WELCOME_PAGE_HEADING = new Font( Display.getDefault(), "Tahoma", 28, SWT.BOLD ); //$NON-NLS-1$

  static public final Font WELCOME_PAGE_MODULE = new Font( Display.getDefault(), "Tahoma", 14, SWT.BOLD ); //$NON-NLS-1$

  static public final Font HEADING = new Font( Display.getDefault(), "Tahoma", 8, SWT.BOLD ); //$NON-NLS-1$

  private final ProjectDatabaseModel PROJECT_DATABASE_MODEL = null;

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

  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
    plugin = this;
  }

  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    if( PROJECT_DATABASE_MODEL != null )
      PROJECT_DATABASE_MODEL.stop();
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

}
