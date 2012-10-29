package de.openali.odysseus.service.ods.product;

import java.net.URL;

import org.kalypso.contribs.eclipse.utils.ILocationProvider;
import org.kalypso.hwv.core.util.DebugLocationUtils;

/**
 * @author Holger Albert
 * @author Gernot Belger
 */
public class DebugLocationProvider implements ILocationProvider
{
  @Override
  public URL getLocation( )
  {
    return DebugLocationUtils.getConfigLocation( "OdysseusServiceODSFeature" );
  }
}