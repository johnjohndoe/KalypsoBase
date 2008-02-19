package org.kalypso.chart.factory;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.chart.factory.provider.IAxisProvider;
import org.kalypso.chart.factory.provider.IAxisRendererProvider;
import org.kalypso.chart.factory.provider.ILayerProvider;
import org.kalypso.chart.factory.provider.IMapperProvider;
import org.kalypso.chart.factory.provider.IStyledElementProvider;
import org.kalypso.chart.framework.impl.logging.Logger;

/**
 * @author burtscher
 */
public class ChartExtensionLoader
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
   * Map for StyledElementProviders
   */
  private static Map<String, IConfigurationElement> SP_MAP = null;

  /**
   * Map for AxisRendererProviders
   */
  private static Map<String, IConfigurationElement> ARP_MAP = null;

  /**
   * Map for MapperProviders
   */
  private static Map<String, IConfigurationElement> MP_MAP = null;

  private ChartExtensionLoader( )
  {
    // will not be instantiated
  }

  public static ILayerProvider createLayerProvider( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> elts = getLayerProviders();

    final IConfigurationElement element = elts.get( id );
    if( element == null )
      return null;

    return (ILayerProvider) element.createExecutableExtension( "class" );
  }

  public static IAxisProvider createAxisProvider( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> elts = getAxisProviders();

    final IConfigurationElement element = elts.get( id );
    if( element == null )
      return null;

    return (IAxisProvider) element.createExecutableExtension( "class" );
  }

  public static IMapperProvider createMapperProvider( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> elts = getMapperProviders();

    final IConfigurationElement element = elts.get( id );
    if( element == null )
      return null;

    return (IMapperProvider) element.createExecutableExtension( "class" );
  }

  public static IAxisRendererProvider createAxisRendererProvider( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> elts = getAxisRendererProviders();

    final IConfigurationElement element = elts.get( id );
    if( element == null )
      return null;

    return (IAxisRendererProvider) element.createExecutableExtension( "class" );
  }

  public static IStyledElementProvider createStyledElementProvider( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> elts = getStyledElementProviders();

    final IConfigurationElement element = elts.get( id );
    if( element == null )
      return null;

    return (IStyledElementProvider) element.createExecutableExtension( "class" );
  }

  private synchronized static Map<String, IConfigurationElement> getLayerProviders( )
  {
    if( LP_MAP != null && LP_MAP.size() > 0 )
      return LP_MAP;

    LP_MAP = new HashMap<String, IConfigurationElement>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.chart.factory.LayerProvider" );
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" );
        LP_MAP.put( id, element );
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Added LayerProvider " + id );
      }
    }
    else
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "Error: cant find ExtensionRegistry" );
    return LP_MAP;
  }

  private synchronized static Map<String, IConfigurationElement> getAxisProviders( )
  {
    if( AP_MAP != null && AP_MAP.size() > 0 )
      return AP_MAP;

    AP_MAP = new HashMap<String, IConfigurationElement>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.chart.factory.AxisProvider" );
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" );
        AP_MAP.put( id, element );
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Added AxisProvider " + id );
      }
    }
    else
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "Error: cant find ExtensionRegistry" );
    return AP_MAP;
  }

  private synchronized static Map<String, IConfigurationElement> getMapperProviders( )
  {
    if( MP_MAP != null && MP_MAP.size() > 0 )
      return MP_MAP;

    MP_MAP = new HashMap<String, IConfigurationElement>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.chart.factory.MapperProvider" );
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" );
        MP_MAP.put( id, element );
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Added MapperProvider " + id );
      }
    }
    else
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "Error: cant find ExtensionRegistry" );
    return MP_MAP;
  }

  private synchronized static Map<String, IConfigurationElement> getAxisRendererProviders( )
  {
    if( ARP_MAP != null && ARP_MAP.size() > 0 )
      return ARP_MAP;

    ARP_MAP = new HashMap<String, IConfigurationElement>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.chart.factory.AxisRendererProvider" );
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" );
        ARP_MAP.put( id, element );
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Added AxisRendererProvider " + id );
      }
    }
    else
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "Error: cant find ExtensionRegistry" );
    return ARP_MAP;
  }

  private synchronized static Map<String, IConfigurationElement> getStyledElementProviders( )
  {
    if( SP_MAP != null && SP_MAP.size() > 0 )
      return SP_MAP;

    SP_MAP = new HashMap<String, IConfigurationElement>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.chart.factory.StyledElementProvider" );
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" );
        SP_MAP.put( id, element );
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Added StyledElementProvider " + id );
      }
    }
    else
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "Error: cant find ExtensionRegistry" );
    return SP_MAP;
  }

}
