package com.infomatiq.jsi;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.infomatiq.jsi.rtree.RTree;

public class Activator implements BundleActivator
{
  private static BundleContext context;

  static BundleContext getContext( )
  {
    return context;
  }

  @SuppressWarnings( "unused" )
  @Override
  public void start( final BundleContext bundleContext ) throws Exception
  {
    Activator.context = bundleContext;

    // OPTIMIZATION: setting the log level of the RTree to error is
    // essential, because by default, debug is enabled, which leads to a
    // major performance problem, as string are concatenated during
    // insertion of a new node into the tree.

    /* force static initialization */
    new RTree();

    /* set log level to error */
    final Logger logger = Logger.getLogger( RTree.class.getName() );
    logger.setLevel( Level.ERROR );
  }

  @Override
  public void stop( final BundleContext bundleContext ) throws Exception
  {
    Activator.context = null;
  }
}