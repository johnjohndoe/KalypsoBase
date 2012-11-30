package de.openali.odysseus.chart.framework;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.commons.java.lang.Objects;
import org.osgi.framework.Bundle;

import de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter;
import de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer;

/**
 * The activator class controls the plug-in life cycle
 */
public final class OdysseusChartExtensions
{
  private static List<IChartLegendRenderer> CHART_LEGEND_RENDERERS = null;

  private static Map<String, IConfigurationElement> FILTERS = null;

  private OdysseusChartExtensions( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public static synchronized IChartLegendRenderer[] getRenderers( )
  {
    // fill binding map
    if( CHART_LEGEND_RENDERERS == null )
    {
      CHART_LEGEND_RENDERERS = new ArrayList<>();

      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IChartLegendRenderer.EXTENSION_POINT_ID );

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final String pluginid = element.getContributor().getName();
          final Bundle bundle = Platform.getBundle( pluginid );
          final Class< ? > featureClass = bundle.loadClass( element.getAttribute( "renderer" ) ); //$NON-NLS-1$
          final Constructor< ? > constructor = featureClass.getConstructor();

          final IChartLegendRenderer instance = (IChartLegendRenderer) constructor.newInstance();
          CHART_LEGEND_RENDERERS.add( instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }
    }

    return CHART_LEGEND_RENDERERS.toArray( new IChartLegendRenderer[] {} );
  }

  public static synchronized IChartLegendRenderer getRenderers( final String identifier )
  {
    final IChartLegendRenderer[] renderers = getRenderers();
    for( final IChartLegendRenderer renderer : renderers )
    {
      if( StringUtils.equals( renderer.getIdentifier(), identifier ) )
        return renderer;
    }

    return null;
  }

  private static synchronized Map<String, IConfigurationElement> getFilters( )
  {
    // fill binding map
    if( Objects.isNull( FILTERS ) )
    {
      FILTERS = new HashMap<>();

      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IChartLayerFilter.EXTENSION_POINT_ID );

      for( final IConfigurationElement element : elements )
      {
        final String id = element.getAttribute( IChartLayerFilter.ATTRIBUTE_ID );
        FILTERS.put( id, element );
      }
    }

    return FILTERS;
  }

  public static synchronized IChartLayerFilter createFilter( final String identifier )
  {
    final Map<String, IConfigurationElement> filters = getFilters();

    final IConfigurationElement element = filters.get( identifier );
    if( element == null )
      return null;

    try
    {
      return (IChartLayerFilter) element.createExecutableExtension( IChartLayerFilter.ATTRIBUTE_FILTER );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return null;
    }
  }
}