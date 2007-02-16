package org.kalypso.service.ogc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.service.ogc.exception.OWSException;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("serial")
public class OGCServlet extends HttpServlet implements Servlet
{
  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse) this method has to be synchronized as it uses graphics on an
   *      environment without a running display; therefore, the display has to be created (Display.getDefault()) and
   *      disposed synchronously (or using the display generates an illegal Access Error) The
   *      Display.getCurrent()-method is of no use here.
   * @TODO: integrate operations extension
   */
  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    //Operationsparameter für Service, Request und Version
    String opService = request.getParameter( "SERVICE" );
    String opRequest = request.getParameter( "REQUEST" );
    String opVersion = request.getParameter( "VERSION" );
    
    OutputStream out = response.getOutputStream();

    
    String locator = request.getRequestURL().toString();

    locator=request.getRequestURL().toString()+"?"+request.getQueryString();
    
    if (opService == null)
    {
      //Fehler ausgeben: Kein Service spezifiziert
      String str = (new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter 'SERVICE' is mandatory", locator )).toXMLString();
      OutputStreamWriter osw=new OutputStreamWriter(out);
      osw.write( str );
      osw.close();
      return;
    }
    if (opRequest == null)
    {
      //Fehler ausgeben: Keine Operation spezifiziert
      String str = (new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter 'REQUEST is mandatory", locator )).toXMLString();
      OutputStreamWriter osw=new OutputStreamWriter(out);
      osw.write( str );
      osw.close();
      return;
    }
    if (opVersion == null)
    {
      //Fehler ausgeben: Keine Version spezifiziert
      String str = (new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter 'VERSION' is mandatory", locator )).toXMLString();
      OutputStreamWriter osw=new OutputStreamWriter(out);
      osw.write( str );
      osw.close();
      return;
    }
    
    
      //Wenn wir hier ankommen, sind alle OWS-Pflicht-Parameter vorhanden
      IOGCService ogcService = null;
      // Service-Klasse laden
      try
      {
        ogcService = OGCServiceExtensions.createService( opService );
      }
      catch( CoreException e1 )
      {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

      if( ogcService != null )
      {
        // Operation ausführen
        try
        {
          ogcService.executeOperation( new RequestBean( request ), new ResponseBean(response) );
        }
        catch( OWSException e )
        {
          // geworfene Fehlermeldung als XML ausgeben
          String str= e.toXMLString();
          OutputStreamWriter osw=new OutputStreamWriter(out);
          osw.write( str );
          osw.close();
        }
      }
      else
      {
        //  Fehler ausgeben: Service nicht vorhanden
        String str = (new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "SERVICE '"+opService+"' not found", locator )).toXMLString();
        OutputStreamWriter osw=new OutputStreamWriter(out);
        osw.write( str );
        osw.close();
      }
    out.close();
    
  }
  
  /**
   * doPost gets redirected to doGet - GET and POST are handled the same way
   * 
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException
  {
    doGet(request, response);
  }
}
