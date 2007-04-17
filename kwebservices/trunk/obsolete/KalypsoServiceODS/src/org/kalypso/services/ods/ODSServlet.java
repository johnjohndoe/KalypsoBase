/**
 * 
 */
package org.kalypso.services.ods;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.service.ogc.IOGCService;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.services.ods.operation.IODSOperation;
import org.kalypso.services.ods.operation.ODSOperationExtensions;

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
   * TODO: check if UI thread execution is really needed - maybe only synchronized doGet method is demanded in order to
   * stop getting swt errors (in second case, its possible to easily integrate new methods by using an extension point
   */

  /**
   * @see org.kalypso.service.ogc.IOGCService#executeOperation(org.kalypso.service.ogc.RequestBean,
   *      javax.servlet.http.HttpServletResponse)
   */
  public synchronized void executeOperation( RequestBean requestBean, ResponseBean responseBean ) throws OWSException
  {
    String operationName = requestBean.getParameterValue( "REQUEST" );

    // Load Operatione from ExtensionPoint
    IODSOperation op = null;
    try
    {
      op = ODSOperationExtensions.createOperation( operationName );
    }
    catch( CoreException e )
    {
      throw new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "Operation " + operationName + " not found", "" );
    }

    if( op != null )
      op.operate( requestBean, responseBean );
    else
      throw new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "Operation " + operationName + " not found", "" );
  }

  /**
   * @see org.kalypso.service.ogc.IOGCService#responsibleFor(org.kalypso.service.ogc.RequestBean)
   */
  public boolean responsibleFor( RequestBean request )
  {
    String parameterValue = request.getParameterValue( "Service" );

    if( parameterValue != null && parameterValue.equals( "ODS" ) )
      return true;

    return false;
  }

  /**
   * @see org.kalypso.service.ogc.IOGCService#destroy()
   */
  public void destroy( )
  {
  }
}