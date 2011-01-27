package de.openali.odysseus.chart.framework;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer;
import de.openali.odysseus.chart.framework.util.resource.ColorRegistry;
import de.openali.odysseus.chart.framework.util.resource.FontRegistry;
import de.openali.odysseus.chart.framework.util.resource.ImageRegistry;
import de.openali.odysseus.chart.framework.util.resource.PatternRegistry;

/**
 * The activator class controls the plug-in life cycle
 */
public class OdysseusChartFrameworkPlugin extends Plugin
{
  private static List<IChartLegendRenderer> CHART_LEGEND_RENDERERS = null;

  public static final String PLUGIN_ID = "de.openali.odysseus.chart.framework";

  private static OdysseusChartFrameworkPlugin PLUGIN;

  private ColorRegistry m_colorRegistry;

  private ImageRegistry m_imageRegistry;

  private PatternRegistry m_patternRegistry;

  private FontRegistry m_fontRegistry;

  /**
   * The constructor
   */
  public OdysseusChartFrameworkPlugin( )
  {
    PLUGIN = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
    m_imageRegistry = new ImageRegistry();
    m_colorRegistry = new ColorRegistry();
    m_patternRegistry = new PatternRegistry();
    m_fontRegistry = new FontRegistry();
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    PLUGIN = null; // NOPMD by alibu on 17.02.08 17:43
    super.stop( context );
    getColorRegistry().dispose();
    getImageRegistry().dispose();
    getPatternRegistry().dispose();
    getFontRegistry().dispose();
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static OdysseusChartFrameworkPlugin getDefault( )
  {
    return PLUGIN;
  }

  public ColorRegistry getColorRegistry( )
  {
    return m_colorRegistry;
  }

  public ImageRegistry getImageRegistry( )
  {
    return m_imageRegistry;
  }

  public PatternRegistry getPatternRegistry( )
  {
    return m_patternRegistry;
  }

  public FontRegistry getFontRegistry( )
  {
    return m_fontRegistry;
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized IChartLegendRenderer[] getRenderers( )
  {
    // fill binding map
    if( CHART_LEGEND_RENDERERS == null )
    {
      CHART_LEGEND_RENDERERS = new ArrayList<IChartLegendRenderer>();

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

  public synchronized IChartLegendRenderer getRenderers( final String identifier )
  {
    final IChartLegendRenderer[] renderers = getRenderers();
    for( final IChartLegendRenderer renderer : renderers )
    {
      if( StringUtils.equals( renderer.getIdentifier(), identifier ) )
        return renderer;
    }

    return null;
  }
}
