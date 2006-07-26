/**
 * 
 */
package org.kalypso.services.ods;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
      
      String odsrequest=request.getParameter( "REQUEST" );
      if (odsrequest!=null)
      {
        if (odsrequest.compareTo( "GetCapabilities" )==0)
        {
          GetCapabilites gc=new GetCapabilites();
          gc.operate( request, response );
        }
        else if (odsrequest.compareTo( "GetChart" )==0)
        {
          GetChart gc=new GetChart();
          gc.operate( request, response );
        }
        else if (odsrequest.compareTo( "GetHTMLTable" )==0)
        {
          GetHTMLTable ght=new GetHTMLTable();
          ght.operate( request, response );
        }
        else if (odsrequest.compareTo( "GetLegend" )==0)
        {
          GetLegend gl=new GetLegend();
          gl.operate( request, response );
        }
      }
    
    

  }
  
  
  /**
   * Das ist die alte Testklasse von Gernot; ich lasse sie hier drin, damit ich mich später evtl daran orientieren kann.
   */
  /*
  private void oldDoGet(final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing swt servlet", null ) );

    response.setContentType( "image/jpeg" );

    Display display = null;
    OutputStream outputStream = null;

    try
    {
      display = new Display();
      final Image img = new Image( display, 50, 50 );

      final GC gc = new GC( img );
      gc.setBackground( display.getSystemColor( SWT.COLOR_GREEN ) );
      gc.fillRectangle( img.getBounds() );
      gc.dispose();

      final ImageLoader il = new ImageLoader();
      il.data = new ImageData[] { img.getImageData() };

      outputStream = new BufferedOutputStream( response.getOutputStream() );
      il.save( outputStream, SWT.IMAGE_JPEG );
      outputStream.close();
    }
    finally
    {
      if( outputStream != null )
        outputStream.close();

      if( display != null )
        display.dispose();
    }
  }
  */

}
