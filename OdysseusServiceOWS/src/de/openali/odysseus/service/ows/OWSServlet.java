package de.openali.odysseus.service.ows;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.CoreException;

import de.openali.odysseus.service.ows.exception.OWSException;
import de.openali.odysseus.service.ows.extension.IOWSOperation;
import de.openali.odysseus.service.ows.extension.OWSOperationExtensionLoader;
import de.openali.odysseus.service.ows.request.RequestBean;
import de.openali.odysseus.service.ows.request.ResponseBean;

/**
 * @author alibu
 */
@SuppressWarnings("serial")
public class OWSServlet extends HttpServlet// implements Servlet
{
  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    // Operationsparameter f�r Service, Request und Version
    final String opService = request.getParameter( "SERVICE" );
    final String opRequest = request.getParameter( "REQUEST" );
    final String opVersion = request.getParameter( "VERSION" );

    final OutputStream out = response.getOutputStream();

    String locator = request.getRequestURL().toString();

    locator = request.getRequestURL().toString() + "?" + request.getQueryString();

    if( opService == null )
    {
      // Fehler ausgeben: Kein Service spezifiziert
      final String str = new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter 'SERVICE' is mandatory", locator ).toXMLString();
      final OutputStreamWriter osw = new OutputStreamWriter( out );
      osw.write( str );
      osw.close();
      return;
    }
    if( opRequest == null )
    {
      // Fehler ausgeben: Keine Operation spezifiziert
      final String str = new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter 'REQUEST is mandatory", locator ).toXMLString();
      final OutputStreamWriter osw = new OutputStreamWriter( out );
      osw.write( str );
      osw.close();
      return;
    }
    if( opVersion == null )
    {
      // Fehler ausgeben: Keine Version spezifiziert
      final String str = new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter 'VERSION' is mandatory", locator ).toXMLString();
      final OutputStreamWriter osw = new OutputStreamWriter( out );
      osw.write( str );
      osw.close();
      return;
    }
    if( !opVersion.trim().equals( OdysseusServiceOWSPlugin.SERVICE_VERSION ) )
    {
      // Fehler ausgeben: Keine Version spezifiziert
      final String str = new OWSException( OWSException.ExceptionCode.VERSION_NEGOTIOATON_FAILED, "VERSION unknown; only '" + OdysseusServiceOWSPlugin.SERVICE_VERSION + "' is supported.", locator ).toXMLString();
      final OutputStreamWriter osw = new OutputStreamWriter( out );
      osw.write( str );
      osw.close();
      return;
    }

    // Wenn wir hier ankommen, sind alle OWS-Pflicht-Parameter vorhanden
    IOWSOperation owsOperation = null;
    // Service-Klasse laden
    try
    {
      owsOperation = OWSOperationExtensionLoader.createOWSOperation( opRequest );
    }
    catch( final CoreException e1 )
    {
      /*
       * we do not handle the specific exception here, but will instead log the error; the server will display an error
       * as the operation can not be created and therefore is not available
       */
      e1.printStackTrace();
    }

    if( owsOperation != null )
      // Operation ausf�hren
      try
      {
        owsOperation.checkAndExecute( new RequestBean( request ), new ResponseBean( response ), getServletContext() );
      }
      catch( final OWSException e )
      {
        // geworfene Fehlermeldung als XML ausgeben
        final String str = e.toXMLString();
        final OutputStreamWriter osw = new OutputStreamWriter( out );
        osw.write( str );
        osw.close();
      }
    else
    {
      // Operation could not be created -> return error message
      final String str = new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "Operation '" + opRequest + "' not found", locator ).toXMLString();
      final OutputStreamWriter osw = new OutputStreamWriter( out );
      osw.write( str );
      osw.close();
    }
    out.close();

  }

  /**
   * Starting point for HTTP-POST; the method only redirects to doGet, as only KVP parameter encoding is supported
   * 
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    doGet( request, response );
  }
}