package de.openali.odysseus.service.ods;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import de.openali.odysseus.service.ods.util.DisplayHelper;

public class OdysseusServiceODSPlugin extends Plugin
{

  public OdysseusServiceODSPlugin( )
  {
    super();
  }

  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
    DisplayHelper.getInstance().getDisplay();
  }

}
