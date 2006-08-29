/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.services.ods.operation;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.kalypso.services.ods.Activator;
import org.kalypso.services.ods.operation.IODSConstants.ERROR_CODE;
import org.kalypso.services.ods.preferences.IODSPreferences;
import org.kalypso.services.ods.preferences.ODSPreferences;
import org.kalypso.swtchart.chart.ChartImageContainer;
import org.kalypso.swtchart.chart.legend.LegendImageContainer;
import org.kalypso.swtchart.configuration.ChartLoader;
import org.kalypso.swtchart.configuration.ConfigurationLoader;
import org.kalypso.ui.KalypsoGisPlugin;
import org.ksp.chart.viewerconfiguration.ConfigurationType;

/**
 * @author alibu
 *
 */
public class GetChart implements IODSOperation, Runnable
{

 
 private HttpServletRequest m_request;
private HttpServletResponse m_response;

public void operate( HttpServletRequest request, HttpServletResponse response ) throws IOException
 {
   Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet: GetChart", null ) );

   int width=500;
   int height=400;
   String reqWidth=request.getParameter( "WIDTH" );
   String reqHeight=request.getParameter( "HEIGHT" );
   String reqName=request.getParameter( "NAME" );
   if (reqWidth!=null)
     width=Integer.parseInt( reqWidth );
   if (reqHeight!=null)
     height=Integer.parseInt( reqHeight );
   //der Name muss da sein, sonst kann kein Chart ausgew‰hlt werden
   if (reqName!=null)
   {
     Display display=null;
     BufferedOutputStream outputStream=null;
     ChartImageContainer cic = null;
     try
     {
        //GMLWorkspace initialisieren
         KalypsoGisPlugin.getDefault();
         IEclipsePreferences prefs = new ODSPreferences().getPreferences();
         String pathVC=prefs.get( IODSPreferences.CONFIG_PATH, "" )+prefs.get( IODSPreferences.CONFIG_NAME, "" );

         ConfigurationLoader cl=new ConfigurationLoader(pathVC);
         ConfigurationType config = cl.getConfiguration();
         display=Display.getDefault();
         cic = new ChartImageContainer(config, reqName, display, width, height);
         Image chartImg=cic.getImage();
         
         if (chartImg!=null)
         {
           response.setContentType( "image/jpeg" );
           ImageLoader il=new ImageLoader();
           ImageData id=chartImg.getImageData();
           il.data=new ImageData[]{id};
           outputStream = new BufferedOutputStream( response.getOutputStream() );
           il.save( outputStream, SWT.IMAGE_JPEG );
         }
         else
         {
           ODSException.showException( request, response, ERROR_CODE.DIAGRAM_NOT_DEFINED );
         }
     }
     catch( Exception e )
     {
       e.printStackTrace();
     }
     finally
     {
       if (display!=null && !display.isDisposed())
       { 
         try{
           display.dispose();
         }
         catch (SWTError e)
         {
           System.out.println("Display wurde bereits disposed");
         }
       }
       if (cic!=null)
         cic.dispose();
       if (outputStream!=null)
         outputStream.close();
     }
   }
   else
       ODSException.showException( request, response, ERROR_CODE.NO_NAME_SPECIFIED );
 }  
 public void init(HttpServletRequest request, HttpServletResponse response)
 {
   m_request=request;
   m_response=response;
 }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run( )
  {
    try
    {
      operate( m_request, m_response );
    }
    catch( IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }  

 
}
