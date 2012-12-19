package de.openali.odysseus.chart.factory.config;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.openali.odysseus.chart.factory.provider.IAxisProvider;
import de.openali.odysseus.chart.factory.provider.IAxisRendererProvider;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;

/**
 * @author burtscher
 */
public final class ChartExtensionLoader implements IExtensionLoader
{
  /**
   * Map for LayerProviders
   */
  private static Map<String, IConfigurationElement> LP_MAP = null;

  /**
   * Map for AxisProviders
   */
  private static Map<String, IConfigurationElement> AP_MAP = null;

  /**
   * Map for AxisRendererProviders
   */
  private static Map<String, IConfigurationElement> ARP_MAP = null;

  /**
   * Map for MapperProviders
   */
  private static Map<String, IConfigurationElement> MP_MAP = null;

  public static final String PLUGIN_ID = "de.openali.odysseus.chart.factory"; //$NON-NLS-1$

  private static ChartExtensionLoader INSTANCE = null;

  private ChartExtensionLoader( )
  {
  }

  public static synchronized ChartExtensionLoader getInstance( )
  {
    if( INSTANCE == null )
    {
      INSTANCE = new ChartExtensionLoader();
    }

    return INSTANCE;
  }

  public static ILayerProvider createLayerProvider( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> elts = getLayerProviders();

    final IConfigurationElement element = elts.get( id );
    if( element == null )
    {
      return null;
    }

    return (ILayerProvider) element.createExecutableExtension( "class" ); //$NON-NLS-1$
  }

  public static IAxisProvider createAxisProvider( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> elts = getAxisProviders();

    final IConfigurationElement element = elts.get( id );
    if( element == null )
    {
      return null;
    }

    return (IAxisProvider) element.createExecutableExtension( "class" ); //$NON-NLS-1$
  }

//  public static IMapperProvider createMapperProvider( final String id ) throws CoreException
//  {
//    final Map<String, IConfigurationElement> elts = getMapperProviders();
//
//    final IConfigurationElement element = elts.get( id );
//    if( element == null )
//    {
//      return null;
//    }
//
//    return (IMapperProvider) element.createExecutableExtension( "class" ); //$NON-NLS-1$
//  }

  public static IAxisRendererProvider createAxisRendererProvider( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> elts = getAxisRendererProviders();

    final IConfigurationElement element = elts.get( id );
    if( element == null )
    {
      return null;
    }

    return (IAxisRendererProvider) element.createExecutableExtension( "class" ); //$NON-NLS-1$
  }

  private static synchronized Map<String, IConfigurationElement> getLayerProviders( )
  {
    if( LP_MAP != null && LP_MAP.size() > 0 )
    {
      return LP_MAP;
    }

    LP_MAP = new HashMap<>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( PLUGIN_ID + ".LayerProvider" ); //$NON-NLS-1$
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" ); //$NON-NLS-1$
        LP_MAP.put( id, element );
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Added LayerProvider " + id ); //$NON-NLS-1$
      }
    }
    else
    {
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "Error: cant find ExtensionRegistry" ); //$NON-NLS-1$
    }
    return LP_MAP;
  }

  private static synchronized Map<String, IConfigurationElement> getAxisProviders( )
  {
    if( AP_MAP != null && AP_MAP.size() > 0 )
    {
      return AP_MAP;
    }

    AP_MAP = new HashMap<>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( PLUGIN_ID + ".AxisProvider" ); //$NON-NLS-1$
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" ); //$NON-NLS-1$
        AP_MAP.put( id, element );
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Added AxisProvider " + id ); //$NON-NLS-1$
      }
    }
    else
    {
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "Error: cant find ExtensionRegistry" ); //$NON-NLS-1$
    }
    return AP_MAP;
  }

  private static synchronized Map<String, IConfigurationElement> getMapperProviders( )
  {
    if( MP_MAP != null && MP_MAP.size() > 0 )
    {
      return MP_MAP;
    }

    MP_MAP = new HashMap<>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( PLUGIN_ID + ".MapperProvider" ); //$NON-NLS-1$
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" ); //$NON-NLS-1$
        MP_MAP.put( id, element );
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Added MapperProvider " + id ); //$NON-NLS-1$
      }
    }
    else
    {
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "Error: cant find ExtensionRegistry" ); //$NON-NLS-1$
    }
    return MP_MAP;
  }

  private static synchronized Map<String, IConfigurationElement> getAxisRendererProviders( )
  {
    if( ARP_MAP != null && ARP_MAP.size() > 0 )
    {
      return ARP_MAP;
    }

    ARP_MAP = new HashMap<>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( PLUGIN_ID + ".AxisRendererProvider" ); //$NON-NLS-1$
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" ); //$NON-NLS-1$
        ARP_MAP.put( id, element );
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Added AxisRendererProvider " + id ); //$NON-NLS-1$
      }
    }
    else
    {
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "Error: cant find ExtensionRegistry" ); //$NON-NLS-1$
    }
    return ARP_MAP;
  }

  /**
   * @see de.openali.odysseus.chart.factory.config.IExtensionLoader#getExtension(java.lang.Class, java.lang.String)
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T getExtension( final Class<T> extensionClass, final String id )
  {
    try
    {
      if( IAxisProvider.class.isAssignableFrom( extensionClass ) )
      {
        return (T) createAxisProvider( id );
      }
      if( IAxisRendererProvider.class.isAssignableFrom( extensionClass ) )
      {
        return (T) createAxisRendererProvider( id );
      }
      if( ILayerProvider.class.isAssignableFrom( extensionClass ) )
      {
        return (T) createLayerProvider( id );
      }
//      if( IMapperProvider.class.isAssignableFrom( extensionClass ) )
//      {
//        return (T) createMapperProvider( id );
//      }
    }
    catch( final CoreException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Logger.logError( Logger.TOPIC_LOG_GENERAL, "Extensions to type not supported: " + extensionClass ); //$NON-NLS-1$
    // no extendable clas

    return null;
  }

}
