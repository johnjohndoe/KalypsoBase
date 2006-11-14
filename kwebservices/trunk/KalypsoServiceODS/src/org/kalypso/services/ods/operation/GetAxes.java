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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.services.ods.Activator;
import org.kalypso.services.ods.DisplayHelper;
import org.kalypso.services.ods.operation.IODSConstants.ERROR_CODE;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.ChartImageContainer;
import org.kalypso.swtchart.chart.Plot;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.component.IAxisComponent;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.configuration.ChartLoader;
import org.kalypso.swtchart.configuration.ConfigurationLoader;
import org.kalypso.swtchart.exception.ConfigChartNotFoundException;
import org.ksp.chart.viewerconfiguration.ConfigurationType;

/**
 * @author burtscher
 * IODS operation to display an image containing a chart 
 */
public class GetAxes implements IODSOperation, Runnable
{

 
    private HttpServletRequest m_request;
    private HttpServletResponse m_response;
    private Image m_img;
    
    
    public GetAxes()
    {
      
    }
    

public void operate( HttpServletRequest request, final HttpServletResponse response )
 {
    m_request=request;
    m_response=response;
    DisplayHelper.getInstance().getDisplay().syncExec( this );
 }  

  /**
   * @see java.lang.Runnable#run()
   */
  public void run( )
  {
    
    Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet: GetChart", null ) );

    int width=500;
    int height=400;
    String reqHeight=m_request.getParameter( "HEIGHT" );
    String reqName=m_request.getParameter( "NAME" );
    if (reqHeight!=null)
      height=Integer.parseInt( reqHeight );
    //der Name muss da sein, sonst kann kein Chart ausgew‰hlt werden
    if (reqName!=null)
    {
      Display display=DisplayHelper.getInstance().getDisplay();
      String pathVC=System.getProperty( IODSConstants.ODS_CONFIG_PATH_KEY, IODSConstants.ODS_CONFIG_PATH_DEFAULT)+IODSConstants.ODS_CONFIG_NAME;
      BufferedOutputStream outputStream=null;

      ConfigurationLoader cl;
      Image img=null;
      
      
      
      
      Display dev=DisplayHelper.getInstance().getDisplay();
      m_img = new Image( dev , width, height );

      GCWrapper gcw = new GCWrapper( new GC( m_img ) );
      Image tmpImg = new Image( dev, width, height );
      GC tmpGc = new GC( tmpImg );
      GCWrapper tmpGcw = new GCWrapper( tmpGc );
      tmpGcw.setBackground( dev.getSystemColor( SWT.COLOR_YELLOW ) );
      tmpGcw.fillRectangle( 0, 0, tmpImg.getBounds().width, tmpImg.getBounds().height );

      // img mit weiﬂem Hintergrund versehen
      gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
      gcw.fillRectangle( 0, 0, width, height );

      //Chart erzeugen
      try
      {
        cl = new ConfigurationLoader(pathVC);
        ConfigurationType config = cl.getConfiguration();
        Shell s=new Shell(DisplayHelper.getInstance().getDisplay());
        Canvas ca=new Canvas(s, SWT.NONE);
        Chart c=new Chart( ca, SWT.NONE );
       
        
        ChartLoader.createChart( c, config, reqName, null);
        
        c.setSize(width, height);
      //  c.layout();
        
        
        
        // Plot zeichnen
        Plot plot = c.getPlot();
        while ( plot.getBounds().height<height)
        {
          c.setSize( c.getSize().x, c.getSize().y+1 );
          System.out.println("Vergrˆssere Hˆhe auf "+plot.getBounds());
        }
        while ( plot.getBounds().width<width)
        {
          c.setSize( c.getSize().x+1, c.getSize().y );
          System.out.println("Vergrˆssere Breite auf "+plot.getBounds());
        }
       // plot.setBounds( 0, 0, width, height );
        ODSUtils.setAxisValues( c, m_request );
        
        tmpImg = plot.paintChart( dev, tmpGcw, m_img.getBounds(), null );
        gcw.drawImage( tmpImg, 0, 0, plot.getBounds().width, plot.getBounds().height,0, 0, plot.getBounds().width, plot.getBounds().height );
        tmpGcw.fillRectangle( 0, 0, tmpImg.getBounds().width, tmpImg.getBounds().height );
        
        
        //Manuelles (kuck nach!) testen, ob die Werte auch nach der Vergrˆﬂerung noch den gew¸nschten Werten entsprechen 
        IAxisRegistry ar=c.getAxisRegistry();
        IAxis[] axes = ar.getAxes();
        for( IAxis axis : axes )
        {
          System.out.println(axis.getIdentifier()+": getFrom:"+axis.getFrom());
          System.out.println(axis.getIdentifier()+": getFrom:"+axis.getTo());
        } 
        
        if (m_img!=null)
        {
          m_response.setContentType( "image/jpeg" );
          ImageLoader il=new ImageLoader();
          ImageData id=m_img.getImageData();
          il.data=new ImageData[]{id};
          outputStream = new BufferedOutputStream( m_response.getOutputStream() );
          il.save( outputStream, SWT.IMAGE_JPEG );
        }
        else
        {
          ODSException.showException( m_request, m_response, ERROR_CODE.DIAGRAM_NOT_DEFINED );
        }
      }
      catch( JAXBException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch( IOException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch( ConfigChartNotFoundException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      finally
      {
        if (m_img!=null)
          m_img.dispose();
        if (outputStream!=null)
        {
          try
          {
            outputStream.close();
          }
          catch( IOException e )
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
    else
        ODSException.showException( m_request, m_response, ERROR_CODE.NO_NAME_SPECIFIED );
  }  
}
