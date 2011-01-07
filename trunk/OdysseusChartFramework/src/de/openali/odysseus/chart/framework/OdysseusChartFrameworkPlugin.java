package de.openali.odysseus.chart.framework;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import de.openali.odysseus.chart.framework.util.resource.ColorRegistry;
import de.openali.odysseus.chart.framework.util.resource.FontRegistry;
import de.openali.odysseus.chart.framework.util.resource.ImageRegistry;
import de.openali.odysseus.chart.framework.util.resource.PatternRegistry;

/**
 * The activator class controls the plug-in life cycle
 */
public class OdysseusChartFrameworkPlugin extends Plugin
{

  // The plug-in ID
  public static final String PLUGIN_ID = "de.openali.odysseus.chart.framework";

  // The shared instance
  private static OdysseusChartFrameworkPlugin plugin;

  private ColorRegistry m_colorRegistry;

  private ImageRegistry m_imageRegistry;

  private PatternRegistry m_patternRegistry;

  private FontRegistry m_fontRegistry;

  /**
   * The constructor
   */
  public OdysseusChartFrameworkPlugin( )
  {
    plugin = this;
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
    plugin = null; // NOPMD by alibu on 17.02.08 17:43
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
    return plugin;
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

}
