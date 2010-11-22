package org.kalypso.zml.ui;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.zml.ui.table.rules.IZmlTableRule;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class KalypsoZmlUI extends AbstractUIPlugin
{

  private static List<IZmlTableRule> ZML_TABLE_RULES = null;

  // The plug-in ID
  public static final String PLUGIN_ID = "org.kalypso.zml.ui"; //$NON-NLS-1$

  // The shared instance
  private static KalypsoZmlUI PLUGIN;

  /**
   * The constructor
   */
  public KalypsoZmlUI( )
  {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
    PLUGIN = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    PLUGIN = null;
    super.stop( context );
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static KalypsoZmlUI getDefault( )
  {
    return PLUGIN;
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized IZmlTableRule[] getMetadataBoundaries( )
  {
    // fill binding map
    if( ZML_TABLE_RULES == null )
    {
      ZML_TABLE_RULES = new ArrayList<IZmlTableRule>();

      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IZmlTableRule.EXTENSION_POINT_ID );

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final String pluginid = element.getContributor().getName();
          final Bundle bundle = Platform.getBundle( pluginid );
          final Class< ? > featureClass = bundle.loadClass( element.getAttribute( "rule" ) ); //$NON-NLS-1$
          final Constructor< ? > constructor = featureClass.getConstructor();

          final IZmlTableRule instance = (IZmlTableRule) constructor.newInstance();
          ZML_TABLE_RULES.add( instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }
    }

    return ZML_TABLE_RULES.toArray( new IZmlTableRule[] {} );
  }

}
