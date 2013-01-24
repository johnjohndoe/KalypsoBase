package org.kalypso.service.ogc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.service.ogc.RequestBean.TYPE;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.ogc.interceptor.RequestInterceptor;
import org.kalypso.service.ogc.interceptor.RequestInterceptorExtensions;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("serial")
public class OGCServlet extends HttpServlet implements Servlet
{
  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    executeRequests( TYPE.GET, request, response );
  }

  /**
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    executeRequests( TYPE.POST, request, response );
  }

  private void intercept( final HttpServletRequest request ) throws CoreException
  {
    for( final RequestInterceptor interceptor : RequestInterceptorExtensions.getInterceptors() )
    {
      interceptor.intercept( request );
    }
  }

  /**
   * This function checks the given requests, and if a service could handle it.
   * 
   * @param request
   *          The request.
   * @param response
   *          The response.
   */
  private void executeRequests( final TYPE requestType, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    final ResponseBean responseBean = new ResponseBean( response );
    try
    {
      /* Call all interceptors for request */
      intercept( request );

      /* Get all registered services. */
      Map<String, IOGCService> services = OGCServiceExtensions.createServices();

      /* Ask everyone, if he could handle the request. The first which says yes, is taken. */
      final RequestBean requestBean = new RequestBean( requestType, request );
      String foundKey = null;
      IOGCService foundService = null;
      final Iterator<String> itr = services.keySet().iterator();
      while( itr.hasNext() )
      {
        final String key = itr.next();
        final IOGCService service = services.get( key );

        if( service.responsibleFor( requestBean ) )
        {
          /* Found a service. */
          foundKey = key;
          foundService = service;
          break;
        }
      }

      /* No suitable service found. */
      if( foundService == null || foundKey == null )
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, "No service found, which can handle this request.", "" ); //$NON-NLS-1$ //$NON-NLS-2$

      /* Check, if all required parameters are there. */
      /* But only, if the request was send via the get method. */
      /* Or, if the request was send via the post method without body. */
      if( !requestBean.isPost() || (requestBean.isPost() && (requestBean.getBody() == null || requestBean.getBody().isEmpty())) )
        checkMandadoryParameter( requestBean, foundKey );

      /* Execute the operation. */
      // System.out.println( String.format( "Execute operation of '%s' service...", foundKey ) );
      foundService.executeOperation( requestBean, responseBean );
    }
    catch( final OWSException e )
    {
      sendToClient( responseBean, e );
    }
    catch( final Exception e )
    {
      sendToClient( responseBean, new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getMessage(), "" ) ); //$NON-NLS-1$
    }
  }

  /**
   * This function checks, if the parameter which are mandatory for the given service exists.
   * 
   * @param request
   *          The request.
   * @param id
   *          The id of the service.
   */
  private void checkMandadoryParameter( final RequestBean request, final String id ) throws OWSException
  {
    final List<String> mandadoryParameters = OGCServiceExtensions.getMandadoryParameters( id );
    for( int i = 0; i < mandadoryParameters.size(); i++ )
    {
      final String parameterName = mandadoryParameters.get( i );
      if( request.getParameterValue( parameterName ) == null )
        throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter '" + parameterName + "' is mandatory", parameterName ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  private void sendToClient( final ResponseBean responseBean, final OWSException e ) throws IOException
  {
    final OutputStream os = responseBean.getOutputStream();
    final OutputStreamWriter osw = new OutputStreamWriter( os );
    osw.write( e.toXMLString() );
    osw.close();
    os.close();
  }
}