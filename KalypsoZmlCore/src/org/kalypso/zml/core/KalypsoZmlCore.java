package org.kalypso.zml.core;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.rules.IZmlRuleImplementation;
import org.kalypso.zml.core.table.rules.impl.grenzwert.IZmlGrenzwertValue;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class KalypsoZmlCore extends Plugin implements BundleActivator
{
  private static Map<String, IZmlRuleImplementation> ZML_TABLE_RULES = null;

  private static Map<String, IZmlGrenzwertValue> ZML_GRENZWERT_DELEGATES = null;

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
    CONTEXT = bundleContext;
    PLUGIN = this;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( final BundleContext bundleContext ) throws Exception
  {
    CONTEXT = null;
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
  public synchronized Map<String, IZmlRuleImplementation> getRules( )
  {
    // fill binding map
    if( Objects.isNull( ZML_TABLE_RULES ) )
    {
      ZML_TABLE_RULES = new HashMap<String, IZmlRuleImplementation>();

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
          ZML_TABLE_RULES.put( instance.getIdentifier(), instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }
    }

    return ZML_TABLE_RULES;
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized IZmlRuleImplementation findRule( final String identifier )
  {
    final Map<String, IZmlRuleImplementation> rules = getRules();

    return rules.get( identifier );
  }

  public synchronized Map<String, IZmlGrenzwertValue> getGrenzwertDelegates( )
  {
    // fill binding map
    if( Objects.isNull( ZML_GRENZWERT_DELEGATES ) )
    {
      ZML_GRENZWERT_DELEGATES = new HashMap<String, IZmlGrenzwertValue>();

      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IZmlGrenzwertValue.EXTENSION_POINT_ID );

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final String pluginid = element.getContributor().getName();
          final Bundle bundle = Platform.getBundle( pluginid );
          final Class< ? > featureClass = bundle.loadClass( element.getAttribute( "implementation" ) ); //$NON-NLS-1$
          final Constructor< ? > constructor = featureClass.getConstructor();

          final IZmlGrenzwertValue instance = (IZmlGrenzwertValue) constructor.newInstance();
          ZML_GRENZWERT_DELEGATES.put( instance.getIdentifier(), instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }
    }

    return ZML_GRENZWERT_DELEGATES;
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized IZmlGrenzwertValue findGrenzwertDelegate( final String identifier )
  {
    final Map<String, IZmlGrenzwertValue> rules = getGrenzwertDelegates();

    return rules.get( identifier );
  }

}
