package org.kalypso.zml.core;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.kalypso.zml.core.table.rules.IZmlRuleImplementation;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class KalypsoZmlCore extends Plugin implements BundleActivator
{
  private static List<IZmlRuleImplementation> ZML_TABLE_RULES = null;

  private static BundleContext CONTEXT;

  static BundleContext getContext( )
  {
    return CONTEXT;
  }

  private static KalypsoZmlCore PLUGIN;

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( final BundleContext bundleContext ) throws Exception
  {
    KalypsoZmlCore.CONTEXT = bundleContext;
    PLUGIN = this;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( final BundleContext bundleContext ) throws Exception
  {
    KalypsoZmlCore.CONTEXT = null;
    PLUGIN = null;
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static KalypsoZmlCore getDefault( )
  {
    return PLUGIN;
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized IZmlRuleImplementation[] getRules( )
  {
    // fill binding map
    if( ZML_TABLE_RULES == null )
    {
      ZML_TABLE_RULES = new ArrayList<IZmlRuleImplementation>();

      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IZmlRuleImplementation.EXTENSION_POINT_ID );

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final String pluginid = element.getContributor().getName();
          final Bundle bundle = Platform.getBundle( pluginid );
          final Class< ? > featureClass = bundle.loadClass( element.getAttribute( "rule" ) ); //$NON-NLS-1$
          final Constructor< ? > constructor = featureClass.getConstructor();

          final IZmlRuleImplementation instance = (IZmlRuleImplementation) constructor.newInstance();
          ZML_TABLE_RULES.add( instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }
    }

    return ZML_TABLE_RULES.toArray( new IZmlRuleImplementation[] {} );
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized IZmlRuleImplementation findRule( final String identifier )
  {
    final IZmlRuleImplementation[] rules = getRules();
    for( final IZmlRuleImplementation rule : rules )
    {
      if( rule.getIdentifier().equals( identifier ) )
        return rule;
    }

    return null;
  }
}
