package net.sourceforge.nattable.rcp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class NatTableActivator implements BundleActivator
{
  private static BundleContext CONTEXT;

  static BundleContext getContext( )
  {
    return CONTEXT;
  }

  @Override
  public void start( final BundleContext bundleContext ) throws Exception
  {
    NatTableActivator.CONTEXT = bundleContext;
  }

  @Override
  public void stop( final BundleContext bundleContext ) throws Exception
  {
    NatTableActivator.CONTEXT = null;
  }

}
