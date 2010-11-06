package org.kalypso.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.module.internal.ModuleComparator;

/**
 * The activator class controls the plug-in life cycle
 */
public class ModuleExtensions extends AbstractUIPlugin
{
  private static final String EXTENSION_NAMESPACE = "org.kalypso.module"; //$NON-NLS-1$

  private static final String EXTENSION_POINT_MODULE = "kalypsoModule"; //$NON-NLS-1$

  private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

  private static List<IKalypsoModule> KALYPSO_MODULES = null;

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public static synchronized IKalypsoModule[] getKalypsoModules( )
  {
    // fill binding map
    if( KALYPSO_MODULES == null )
    {
      KALYPSO_MODULES = new ArrayList<IKalypsoModule>();

      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IExtensionPoint extensionPoint = registry.getExtensionPoint( EXTENSION_NAMESPACE, EXTENSION_POINT_MODULE );
      final IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
      for( final IConfigurationElement element : elements )
      {
        try
        {
          final IKalypsoModule instance = (IKalypsoModule) element.createExecutableExtension( ATTRIBUTE_CLASS );
          KALYPSO_MODULES.add( instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }

      final Comparator<IKalypsoModule> comparator = new ModuleComparator();

      Collections.sort( KALYPSO_MODULES, comparator );
    }

    return KALYPSO_MODULES.toArray( new IKalypsoModule[] {} );
  }

  public static IKalypsoModule getKalypsoModule( final String modulueId )
  {
    final IKalypsoModule[] modules = getKalypsoModules();
    for( final IKalypsoModule module : modules )
    {
      if( modulueId.equals( module.getId() ) )
        return module;
    }

    return null;
  }
}
