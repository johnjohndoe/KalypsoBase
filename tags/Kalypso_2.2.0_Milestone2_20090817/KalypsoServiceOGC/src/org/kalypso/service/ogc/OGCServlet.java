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
    final RequestBean requestBean = new RequestBean( TYPE.GET, request );
    final ResponseBean responseBean = new ResponseBean( response );

    /* Check the request. */
    try
    {
      executeRequests( requestBean, responseBean );
    }
    catch( final OWSException e )
    {
      e.printStackTrace();

      final OutputStream os = responseBean.getOutputStream();
      final OutputStreamWriter osw = new OutputStreamWriter( os );
      osw.write( e.toXMLString() );
      osw.close();
      os.close();
    }
  }

  /**
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    final RequestBean requestBean = new RequestBean( TYPE.POST, request );
    final ResponseBean responseBean = new ResponseBean( response );

    /* Check the request. */
    try
    {
      executeRequests( requestBean, responseBean );
    }
    catch( final OWSException e )
    {
      e.printStackTrace();

      final OutputStream os = responseBean.getOutputStream();
      final OutputStreamWriter osw = new OutputStreamWriter( os );
      osw.write( e.toXMLString() );
      osw.close();
      os.close();
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
  private void executeRequests( final RequestBean request, final ResponseBean response ) throws OWSException
  {
    try
    {
      /* Get all registered services. */
      final Map<String, IOGCService> services = OGCServiceExtensions.createServices();

      /* Ask everyone, if he could handle the request. The first which says yes, is taken. */
      String foundKey = null;
      IOGCService foundService = null;
      final Iterator<String> itr = services.keySet().iterator();
      while( itr.hasNext() )
      {
        final String key = itr.next();
        final IOGCService service = services.get( key );
        System.out.println( "Asking service " + key + " ..." );
        if( service.responsibleFor( request ) )
        {
          /* Found a service. */
          System.out.println( "Service " + key + " answered positivly." );
          foundKey = key;
          foundService = service;
          break;
        }
      }

      /* No suitable service found. */
      if( foundService == null || foundKey == null )
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, "No service found, which can handle this request.", "" );

      /* Check, if all required parameters are there. But only, if the request was send via the get method. */
      if( !request.isPost() )
      {
        System.out.println( "GET detected: Checking if all mandatory parameters are available ..." );
        checkMandadoryParameter( request, foundKey );
      }
      else if( request.isPost() && request.getBody() == null )
      {
        System.out.println( "POST without XML detected: Checking if all mandatory parameters are available ..." );
        checkMandadoryParameter( request, foundKey );
      }
      else
      {
        /* The service itself should check his parameters in the XML. */
        System.out.println( "POST with XML detected: Let the service check if all mandatory parameters are available ..." );
      }

      /* Execute the operation. */
      foundService.executeOperation( request, response );
    }
    catch( final CoreException e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getMessage(), "" );
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
        throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter '" + parameterName + "' is mandatory", parameterName );
    }
  }
}