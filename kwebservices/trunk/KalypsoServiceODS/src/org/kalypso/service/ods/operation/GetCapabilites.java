package org.kalypso.service.ods.operation;

import org.kalypso.service.ods.IODSOperation;
import org.kalypso.service.ods.util.CapabilitiesLoader;
import org.kalypso.service.ods.util.XMLOutput;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;

/**
 * @author burtscher IODSOperation to display the services capabilities
 */
public class GetCapabilites implements IODSOperation
{

  public GetCapabilites( )
  {
  }

  /**
   * @see de.openali.ows.service.IOWSOperation#operate(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  public void operate( RequestBean requestBean, final ResponseBean responseBean ) throws OWSException
  {
    final CapabilitiesLoader cl = new CapabilitiesLoader( requestBean.getUrl(), requestBean.getParameterValue( "SCENE" ) );
    XMLOutput.xmlResponse( responseBean, cl.getCapabilitiesDocument() );
  }

}
