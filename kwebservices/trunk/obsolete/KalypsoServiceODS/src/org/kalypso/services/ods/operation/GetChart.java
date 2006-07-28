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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.GregorianCalendar;

import javax.servlet.Servlet;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
/*
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
*/
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.services.ods.Activator;
import org.kalypso.services.ods.tools.HTMLTools;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.Plot;
import org.kalypso.swtchart.configuration.ChartLoader;
import org.kalypso.swtchart.configuration.ConfigurationLoader;
import org.kalypso.ui.KalypsoGisPlugin;
import org.ksp.chart.viewerconfiguration.ConfigurationType;

/**
 * @author alibu
 *
 */
public class GetChart implements IODSOperation
{


  /**
   * @see org.kalypso.services.ods.operation.IODSOperation#operate(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void operate( HttpServletRequest request, HttpServletResponse response )
  {
    Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet: GetChart", null ) );
    System.out.println("---------------GetChart.operate--------------------");
    
    int width=300;
    int height=200;
    String reqWidth=request.getParameter( "WIDTH" );
    String reqHeight=request.getParameter( "HEIGHT" );
    String reqName=request.getParameter( "NAME" );
    if (reqWidth!=null)
      width=Integer.parseInt( reqWidth );
    if (reqHeight!=null)
      height=Integer.parseInt( reqHeight );
  //  Display disp=Display.getDefault();
    Display disp=Display.getDefault();
    Image chartImage=new Image(disp, width, height);
    
    //der Name muss da sein, sonst kann kein Chart ausgew‰hlt werden
    if (reqName!=null)
    {
      createChart(reqName, chartImage);
    
      BufferedOutputStream outputStream;
      try
      {
        response.setContentType( "image/jpeg" );
        ImageLoader il=new ImageLoader();
        ImageData id=chartImage.getImageData();
        il.data=new ImageData[]{id};
        outputStream = new BufferedOutputStream( response.getOutputStream() );
        il.save( outputStream, SWT.IMAGE_JPEG );
        outputStream.close();
      }
      catch( IOException e )
      {
        e.printStackTrace();
      }
     
    }
    
    
    chartImage.dispose();

  }
 
  /*
  * Chart erzeugen
  * @see org.eclipse.ui.IWorkbenchPart#dispose()
  */
 public static void createChart (String chartname, Image img) {
   
   
   //GMLWorkspace initialisieren
   KalypsoGisPlugin.getDefault();
   
   
   String confpath="/home/alibu/dev/workspace32/KalypsoChart/etc/binding/examples/Configuration.xml";
   ConfigurationLoader cl=new ConfigurationLoader(confpath);
   ConfigurationType config = cl.getConfiguration();
   img=ChartLoader.createChartImage( config, chartname, img );
   
   /*
   ImageLoader il=new ImageLoader();
   il.data=new ImageData[]{img.getImageData()};
   long timeInMillis = GregorianCalendar.getInstance().getTimeInMillis();
   il.save("/home/alibu/Desktop/chartImg"+chartname+timeInMillis+".jpg", SWT.IMAGE_JPEG);
   */
   

   
   
 }
 
}
