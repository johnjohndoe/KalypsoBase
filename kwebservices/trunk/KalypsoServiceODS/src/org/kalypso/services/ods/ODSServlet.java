/**
 * 
 */
package org.kalypso.services.ods;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.services.ods.operation.GetCapabilites;
import org.kalypso.services.ods.operation.GetChart;
import org.kalypso.services.ods.operation.GetHTMLTable;
import org.kalypso.services.ods.operation.GetLegend;
import org.kalypso.services.ods.operation.IODSOperation;
import org.kalypso.services.ods.operation.ODSException;
import org.kalypso.services.ods.operation.ODSOperationExtensions;
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
   *      
   *      this method has to be synchronized as it uses graphics on an environment without a running display;
   *      therefore, the display has to be created (Display.getDefault()) and disposed synchronously (or using
   *      the display generates an illegal Access Error)
   *      The Display.getCurrent()-method is of no use here.
   *      
   *      @TODO: integrate operations extension
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

    
  /**
   * TODO: check if UI thread execution is really needed - maybe only synchronized doGet method
   * is demanded in order to stop getting swt errors (in second case, its possible to easily integrate new methods
   * by using an extension point
   */
   private void executeOperation(final HttpServletRequest request, final HttpServletResponse response, String odsrequest) throws IOException
   {
     /*  
      * PROBLEM:
      * System-Properties can't be set using the config.ini when running eclipse inside Tomcat;
      * instead of System.getProperty() you have to us FrameworkProperties.getProperty();
      * to make the proxy work, the framework properties have to be written to system properties
      */
      System.setProperty( "proxySet", FrameworkProperties.getProperty( "proxySet", "true" ));
      System.setProperty( "proxyHost", FrameworkProperties.getProperty( "proxyHost", "172.16.0.253" ));
      System.setProperty( "proxyPort", FrameworkProperties.getProperty( "proxyPort", "8080" ));
      
      /**
		For Testing Purposes only - remove if used in different environment
		*/
      System.setProperty( "proxyHost",  "172.16.0.253" );
      System.setProperty( "proxyPort",  "8080" );
      System.setProperty( "proxySet",  "true" );
     
     System.out.println("Operation:  "+odsrequest);
     
     try
      {
       
       //Load Operatione from ExtensionPoint
        IODSOperation op=ODSOperationExtensions.createOperation( odsrequest );
        if (op!=null)
        {
          op.operate( request, response );
        }
        else
        {
          ODSException.showException( request, response, ERROR_CODE.OPERATION_NOT_SUPPORTED );
        }
      }
    catch( CoreException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
   }
   
   
  @Override
  public void destroy()
  {
    DisplayHelper.getInstance().dispose();
  }
}
