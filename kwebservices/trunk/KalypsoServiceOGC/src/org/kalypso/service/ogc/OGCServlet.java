package org.kalypso.service.ogc;

import java.io.IOException;
import java.io.PrintWriter;

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
    String service = request.getParameter( "SERVICE" );

    

    if( service != null )
    {
      IOGCService ogcService = null;
      // Service-Klasse laden
      try
      {
        ogcService = OGCServiceExtensions.createService( service );
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
          ogcService.executeOperation( new RequestBean( request ), response );
        }
        catch( OWSException e )
        {
          // Fehler ausgeben: Operation nicht gefunden

          // Fehler ausgeben: Parameter fehlt

          // Fehler ausgeben: Parameterwert nicht gültig
          PrintWriter writer = response.getWriter();
          writer.write( e.toXMLString() );
          writer.close();
        }
      }
      else
      {
        // Fehler ausgeben: Service nicht gefunden
      }
    }
    else
    {
      // Fehler ausgeben: Kein Service spezifiziert
      PrintWriter writer = response.getWriter();
      writer.write( (new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter 'SERVICE' is mandatory", request.getRequestURL().toString() )).toXMLString() );
      writer.close();
    }
    
  }
}
