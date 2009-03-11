package de.openali.odysseus.service.ods;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import de.openali.odysseus.service.ods.util.DisplayHelper;

public class OdysseusServiceODSPlugin extends Plugin
{

  public OdysseusServiceODSPlugin( )
  {
  }

  @Override
  public void start( BundleContext context ) throws Exception
  {
    super.start( context );
    DisplayHelper.getInstance().getDisplay();
  }

}
