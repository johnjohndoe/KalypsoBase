package org.kalypso.project.database.client;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.project.database.client.core.model.ProjectDatabaseModel;
import org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel;
import org.kalypso.project.database.client.extension.IKalypsoModule;
import org.kalypso.project.database.client.extension.database.IProjectDataBaseClientConstant;
import org.kalypso.project.database.client.extension.pages.module.IKalypsoModulePage;
import org.kalypso.project.database.sei.IProjectDatabase;
import org.kalypso.project.database.sei.ProjectDatabaseServiceLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class KalypsoProjectDatabaseClient extends AbstractUIPlugin
{
  private static List<IKalypsoModule> KALYPSO_MODULES = null;

  private ProjectDatabaseModel PROJECT_DATABASE_MODEL = null;

  private FormToolkit m_formToolkit;

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
    if( m_formToolkit != null )
    {
      m_formToolkit.dispose();
      m_formToolkit = null;
    }

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

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized IKalypsoModule[] getKalypsoModules( )
  {
    // fill binding map
    if( KALYPSO_MODULES == null )
    {

      KALYPSO_MODULES = new ArrayList<IKalypsoModule>();
      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IKalypsoModule.EXTENSION_POINT_ID );

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final String pluginid = element.getContributor().getName();
          final Bundle bundle = Platform.getBundle( pluginid );
          final Class< ? > featureClass = bundle.loadClass( element.getAttribute( "module" ) ); //$NON-NLS-1$
          final Constructor< ? > constructor = featureClass.getConstructor();

          final IKalypsoModule instance = (IKalypsoModule) constructor.newInstance();
          KALYPSO_MODULES.add( instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }

      final Comparator<IKalypsoModule> comparator = new Comparator<IKalypsoModule>()
      {
        @Override
        public int compare( final IKalypsoModule o1, final IKalypsoModule o2 )
        {
          final IKalypsoModulePage p1 = o1.getModulePage();
          final IKalypsoModulePage p2 = o2.getModulePage();

          final int compare = p1.getPriority().compareTo( p2.getPriority() );
          if( compare == 0 )
            return p1.getHeader().compareTo( p2.getHeader() );

          return compare;
        }
      };

      Collections.sort( KALYPSO_MODULES, comparator );
    }

    return KALYPSO_MODULES.toArray( new IKalypsoModule[] {} );
  }

  /**
   * This function returns the form toolkit for the Planer-Client.
   * 
   * @return The form toolkit.
   */
  public FormToolkit getToolkit( )
  {
    if( m_formToolkit == null )
    {
      m_formToolkit = new FormToolkit( PlatformUI.getWorkbench().getDisplay() );
    }

    return m_formToolkit;
  }

  public IKalypsoModule getKalypsoModule( final String modulueId )
  {
    final IKalypsoModule[] modules = getKalypsoModules();
    for( final IKalypsoModule module : modules )
    {
      if( modulueId.equals( module.getId() ) )
        return module;

    }

    return null;
  }
}
