package org.kalypso.project.database.client;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.kalypso.project.database.client.core.model.ProjectDatabaseModel;
import org.kalypso.project.database.client.core.model.interfaces.IProjectDatabaseModel;
import org.kalypso.project.database.client.extension.IKalypsoModule;
import org.kalypso.project.database.client.extension.IKalypsoModuleEnteringPageHandler;
import org.kalypso.project.database.sei.IProjectDatabase;
import org.kalypso.project.database.server.ProjectDatabase;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class KalypsoProjectDatabaseClient extends Plugin
{
  private final static String KALYPSO_MODULES_EXTENSION_POINT = "org.kalypso.project.database.client.kalypsoModule"; //$NON-NLS-1$

  private static List<IKalypsoModule> KALYPSO_MODULES = null;
  
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
        final String namespaceURI = "http://server.database.project.kalypso.org/"; //$NON-NLS-1$
        final String serviceImplName = ProjectDatabase.class.getSimpleName();

        final String wsdlLocationProperty = System.getProperty( IProjectDataBaseClientConstant.SERVER_WSDL_LOCATION );
        final URL wsdlLocation = new URL( wsdlLocationProperty );
        final QName serviceName = new QName( namespaceURI, serviceImplName + "Service" ); //$NON-NLS-1$

        final Service service = Service.create( wsdlLocation, serviceName );
        m_service = service.getPort( new QName( namespaceURI, serviceImplName + "Port" ), IProjectDatabase.class ); //$NON-NLS-1$

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

  public IProjectDatabaseModel getProjectDatabaseModel( )
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
  public synchronized static IKalypsoModule[] getKalypsoModules( )
  {
    // fill binding map
    if( KALYPSO_MODULES == null )
    {

      KALYPSO_MODULES = new ArrayList<IKalypsoModule>();
      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( KALYPSO_MODULES_EXTENSION_POINT );

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final String pluginid = element.getContributor().getName();
          final Bundle bundle = Platform.getBundle( pluginid );
          final Class< ? extends IKalypsoModule> featureClass = bundle.loadClass( element.getAttribute( "module" ) ); //$NON-NLS-1$
          final Constructor< ? extends IKalypsoModule> constructor = featureClass.getConstructor();

          final IKalypsoModule instance = constructor.newInstance();
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
          final IKalypsoModuleEnteringPageHandler p1 = o1.getModuleEnteringPage();
          final IKalypsoModuleEnteringPageHandler p2 = o2.getModuleEnteringPage();

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
}
