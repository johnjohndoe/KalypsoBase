/**
 * 
 */
package org.kalypso.service.ods;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.service.ods.extension.ODSExtensionLoader;
import org.kalypso.service.ods.util.ODSConfigurationLoader;
import org.kalypso.service.ogc.IOGCService;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;

/**
 * @author Alex Burtscher
 */
@SuppressWarnings("serial")
public class ODSServlet implements IOGCService
{
  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse) this method has to be synchronized as it uses graphics on an
   *      environment without a running display; therefore, the display has to be created (Display.getDefault()) and
   *      disposed synchronously (or using the display generates an illegal Access Error) The
   *      Display.getCurrent()-method is of no use here.
   * @TODO: integrate operations extension
   */

  /**
   * @see org.kalypso.service.ogc.IOGCService#executeOperation(org.kalypso.service.ogc.RequestBean,
   *      javax.servlet.http.HttpServletResponse)
   */
  public synchronized void executeOperation( RequestBean requestBean, ResponseBean responseBean ) throws OWSException
  {
    final String operationName = requestBean.getParameterValue( "REQUEST" );

    // Load Operatione from ExtensionPoint
    IODSOperation op = null;
    try
    {
      op = ODSExtensionLoader.createOWSOperation( operationName );
    }
    catch( final CoreException e )
    {
      throw new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "Operation " + operationName + " not found", "" );
    }

    if( op != null )
    {
      final ODSConfigurationLoader loader = ODSConfigurationLoader.getInstance();
      final String reset = requestBean.getParameterValue( "RESET" );
      if( "TRUE".equals( reset ) )
        loader.reload();

      // Proxy setzen
      final Map<String, String> serviceParams = loader.getServiceParameters();
      String proxyPort = null;
      String proxyHost = null;
      if( serviceParams != null )
      {
        proxyPort = serviceParams.get( "proxyPort" );
        proxyHost = serviceParams.get( "proxyHost" );
      }

      if( proxyPort != null && proxyHost != null )
      {
        System.setProperty( "proxyPort", proxyPort );
        System.setProperty( "proxyHost", proxyHost );
      }

      Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Accessing operation: " + operationName );
      op.operate( requestBean, responseBean );
    }
    else
      throw new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "Operation " + operationName + " not found", "" );
  }

  /**
   * @see org.kalypso.service.ogc.IOGCService#responsibleFor(org.kalypso.service.ogc.RequestBean)
   */
  public boolean responsibleFor( RequestBean request )
  {
    final String parameterValue = request.getParameterValue( "Service" );

    if( parameterValue != null && parameterValue.equals( "ODS" ) )
      return true;

    return false;
  }

  public void destroy( )
  {
    // TODO Auto-generated method stub

  }

}