package de.openali.odysseus.service.ods.operation;

import de.openali.odysseus.service.ods.util.CapabilitiesLoader;
import de.openali.odysseus.service.ods.util.XMLOutput;

public class GetCapabilities extends AbstractODSOperation
{

  @Override
  public void execute( )
  {
    final CapabilitiesLoader cl = new CapabilitiesLoader( getEnv() );
    String scene = getRequest().getParameterValue("SCENE");
    if (scene == null || "".equals(scene.trim()))
    {
    	scene = getEnv().getConfigLoader().getDefaultSceneId();
    }
    XMLOutput.xmlResponse( getResponse(), cl.getCapabilitiesDocument( scene ) );
  }

}
