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
      final Map<String, IOGCService> services;
      try
      {
        /* Call all interceptors for request */
        intercept( request );
        /* Get all registered services. */
        services = OGCServiceExtensions.createServices();
      }
      catch( final CoreException e )
      {
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getMessage(), "" ); //$NON-NLS-1$
      }

      /* Ask everyone, if he could handle the request. The first which says yes, is taken. */
      final RequestBean requestBean = new RequestBean( requestType, request );
      String foundKey = null;
      IOGCService foundService = null;
      final Iterator<String> itr = services.keySet().iterator();
      while( itr.hasNext() )
      {
        final String key = itr.next();
        final IOGCService service = services.get( key );
        System.out.println( "Asking service " + key + " ..." ); //$NON-NLS-1$ //$NON-NLS-2$
        if( service.responsibleFor( requestBean ) )
        {
          /* Found a service. */
          System.out.println( "Service " + key + " answered positivly." ); //$NON-NLS-1$ //$NON-NLS-2$
          foundKey = key;
          foundService = service;
          break;
        }
      }

      /* No suitable service found. */
      if( foundService == null || foundKey == null )
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, "No service found, which can handle this request.", "" ); //$NON-NLS-1$ //$NON-NLS-2$

      /* Check, if all required parameters are there. But only, if the request was send via the get method. */
      if( !requestBean.isPost() )
      {
        System.out.println( "GET detected: Checking if all mandatory parameters are available ..." ); //$NON-NLS-1$
        checkMandadoryParameter( requestBean, foundKey );
      }
      else if( requestBean.isPost() && requestBean.getBody() == null || requestBean.getBody().isEmpty() )
      {
        System.out.println( "POST without body detected: Checking if all mandatory parameters are available ..." ); //$NON-NLS-1$
        checkMandadoryParameter( requestBean, foundKey );
      }
      else
      {
        /* The service itself should check his parameters in the XML. */
        System.out.println( "POST detected: Let the service check if all mandatory parameters are available ..." ); //$NON-NLS-1$
      }

      /* Execute the operation. */
      foundService.executeOperation( requestBean, responseBean );
    }
    catch( final OWSException e )
    {
      final OutputStream os = responseBean.getOutputStream();
      final OutputStreamWriter osw = new OutputStreamWriter( os );
      osw.write( e.toXMLString() );
      osw.close();
      os.close();
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
}