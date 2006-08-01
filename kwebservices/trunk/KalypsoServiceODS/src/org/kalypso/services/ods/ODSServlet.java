/**
 * 
 */
package org.kalypso.services.ods;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
/*
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import org.eclipse.swt.widgets.Display;
 */
import org.kalypso.services.ods.operation.GetCapabilites;
import org.kalypso.services.ods.operation.GetChart;
import org.kalypso.services.ods.operation.GetHTMLTable;
import org.kalypso.services.ods.operation.GetLegend;
import org.kalypso.services.ods.operation.ODSException;
import org.kalypso.services.ods.operation.IODSConstants.ERROR_CODE;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("serial")
public class ODSServlet extends HttpServlet implements Servlet
{
  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected synchronized void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    //Gewünschte Operation ausführen
    String odsrequest = request.getParameter( "REQUEST" );
    //Natürlich nur, wenn als Service auch ODS angegeben ist
    String service = request.getParameter( "SERVICE" );
    if( service != null  )
    {
      if (service.compareTo( "ODS" ) == 0)
      {
        if( odsrequest != null )
            executeOperation(request, response, odsrequest);
        else
          ODSException.showException(request,  response, ERROR_CODE.OPERATION_NOT_SPECIFIED );
      }
      else
        ODSException.showException( request, response, ERROR_CODE.SERVICE_NOT_SUPPORTED );
    }
    else
      ODSException.showException( request, response, ERROR_CODE.SERVICE_NOT_SPECIFIED );
    
  }

    
   private void executeOperation(final HttpServletRequest request, final HttpServletResponse response, String odsrequest) throws IOException
   {
     if( odsrequest.compareTo( "GetCapabilities" ) == 0 )
     {
       GetCapabilites gc = new GetCapabilites();
       gc.operate( request, response );
     }
     else if( odsrequest.compareTo( "GetDiagram" ) == 0 )
     {
       //heisst hier noch GetChart
       GetChart gc = new GetChart();
       gc.init( request, response );
       //die Operation muss im UI-Threat ausgeführt werden, sonst gibts IllegalAccessErrors
       Display.getDefault().syncExec( gc );
     }
     else if( odsrequest.compareTo( "GetHTMLTable" ) == 0 )
     {
       GetHTMLTable ght = new GetHTMLTable();
       ght.operate( request, response );
     }
     else if( odsrequest.compareTo( "GetLegend" ) == 0 )
     {
       GetLegend gl = new GetLegend();
       gl.init( request, response );
       //        die Operation muss im UI-Threat ausgeführt werden, sonst gibts IllegalAccessErrors
       Display.getDefault().syncExec( gl );
     }
     else
       ODSException.showException( request, response, ERROR_CODE.OPERATION_NOT_SUPPORTED );
   }
   
}
