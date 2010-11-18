package org.kalypso.repository;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.repository.conf.RepositoryFactoryConfig;
import org.kalypso.repository.factory.IRepositoryFactory;

/**
 * Helper class that delves into the extensions of the extension point org.kalypso.repositories.
 * 
 * @author schlienger
 */
public final class RepositoriesExtensions
{
  public static final String FACTORY_EXTENSION_POINT = "org.kalypso.repository.factories"; //$NON-NLS-1$

  public static final String BROWSER_EXTENSION_POINT = "org.kalypso.repository.repositoryBrowser"; //$NON-NLS-1$

  public static final String FILTER_EXTENSION_POINT = "org.kalypso.repository.repositoryFilter"; //$NON-NLS-1$

  public static final String ATT_NAME = "name"; //$NON-NLS-1$

  public static final String ATT_FACTORY = "factory"; //$NON-NLS-1$

  public static final String ATT_CONF = "conf"; //$NON-NLS-1$

  public static final String ATT_RO = "readOnly"; //$NON-NLS-1$

  public static final String ATT_CACHED = "cached"; //$NON-NLS-1$

  public static final String ATT_METADATA_SERVICE_PRIORITY = "metadata_service_priority"; //$NON-NLS-1$

  private RepositoriesExtensions( )
  {

  }

  /**
   * Uses the platform extension registry to retrieve all extensions for the repositories extension point.
   * <p>
   * For each extension, a RepositoryFactoryConfig is created which can be used in your application.
   * 
   * @return array of config items
   * @throws CoreException
   */
  public static RepositoryFactoryConfig[] retrieveFactories( ) throws CoreException
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint extensionPoint = registry.getExtensionPoint( FACTORY_EXTENSION_POINT );

    if( extensionPoint == null )
      return new RepositoryFactoryConfig[0];

    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();

    final Vector<RepositoryFactoryConfig> items = new Vector<RepositoryFactoryConfig>();

    for( final IConfigurationElement element : configurationElements )
    {
      final String name = element.getAttribute( ATT_NAME );
      final String conf = element.getAttribute( ATT_CONF );
      final boolean ro = Boolean.valueOf( element.getAttribute( ATT_RO ) ).booleanValue();
      final boolean cached = Boolean.valueOf( element.getAttribute( ATT_CACHED ) ).booleanValue();

      final IRepositoryFactory factory = (IRepositoryFactory) element.createExecutableExtension( ATT_FACTORY );

      items.add( new RepositoryFactoryConfig( factory, name, conf, ro, cached ) );
    }

    return items.toArray( new RepositoryFactoryConfig[items.size()] );
  }

  /**
   * Returns the corresponding factory config for the given repository factory classname
   * 
   * @param factoryClassName
   * @param repositoryName
   * @param conf
   * @param readOnly
   * @throws CoreException
   */
  public static IRepositoryFactory retrieveFactoryFor( final String factoryClassName ) throws CoreException
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint extensionPoint = registry.getExtensionPoint( FACTORY_EXTENSION_POINT );

    if( extensionPoint == null )
      return null;

    final IConfigurationElement[] elements = extensionPoint.getConfigurationElements();

    for( final IConfigurationElement element : elements )
    {
      final String factoryClass = element.getAttribute( ATT_FACTORY );
      if( factoryClassName.equals( factoryClass ) )
        return (IRepositoryFactory) element.createExecutableExtension( ATT_FACTORY );
    }

    return null;
  }

  public static IRepositoryResolver[] retrieveTimeSeriesBrowserRepositories( ) throws CoreException
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( BROWSER_EXTENSION_POINT );
    if( extensionPoint == null )
      return new IRepositoryResolver[] {};

    final Set<IRepositoryResolver> resolvers = new HashSet<IRepositoryResolver>();

    final IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement element : elements )
    {
      final IRepositoryResolver reolver = (IRepositoryResolver) element.createExecutableExtension( "repository" );//$NON-NLS-1$
      resolvers.add( reolver );
    }

    return resolvers.toArray( new IRepositoryResolver[] {} );
  }

  public static IRepositoryFilter[] retrieveRepositoryFilters( ) throws CoreException
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( FILTER_EXTENSION_POINT );
    if( extensionPoint == null )
      return new IRepositoryFilter[] {};

    final Set<IRepositoryFilter> filters = new HashSet<IRepositoryFilter>();

    final IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement element : elements )
    {
      final IRepositoryFilter filter = (IRepositoryFilter) element.createExecutableExtension( "filter" );//$NON-NLS-1$
      filters.add( filter );
    }

    return filters.toArray( new IRepositoryFilter[] {} );
  }
}