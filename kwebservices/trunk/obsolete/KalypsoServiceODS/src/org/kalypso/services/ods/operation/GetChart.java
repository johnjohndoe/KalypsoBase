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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.kalypso.services.ods.Activator;
import org.kalypso.services.ods.helper.CachedChart;
import org.kalypso.services.ods.helper.ChartCache;
import org.kalypso.services.ods.helper.DisplayHelper;
import org.kalypso.services.ods.helper.ODSUtils;
import org.kalypso.services.ods.helper.IODSConstants.ERROR_CODE;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.ChartImageContainer;
import org.kalypso.swtchart.logging.Logger;

/**
 * @author burtscher
 * IODS operation to display an image containing a chart
 */
public class GetChart implements IODSOperation, Runnable
{


    private HttpServletRequest m_request;
    private HttpServletResponse m_response;


    public GetChart()
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
    String reqWidth=m_request.getParameter( "WIDTH" );
    String reqHeight=m_request.getParameter( "HEIGHT" );
    String reqName=m_request.getParameter( "NAME" );
    if (reqWidth!=null)
      width=Integer.parseInt( reqWidth );
    if (reqHeight!=null)
      height=Integer.parseInt( reqHeight );
    //der Name muss da sein, sonst kann kein Chart ausgew‰hlt werden
    if (reqName!=null)
    {
      Display display=DisplayHelper.getInstance().getDisplay();
      BufferedOutputStream outputStream=null;
      ChartImageContainer cic = null;
      Chart chart=null;

      CachedChart cc=ChartCache.getInstance().getObject( reqName );
      if (cc!=null)
      {
          chart=cc.getChart();
          Logger.trace("GetChart: Using cached chart");
      }
      else
      {
        Logger.trace("GetChart: Creating new chart");
        cc=new CachedChart(reqName);
        ChartCache.getInstance().addObject( reqName, cc );
        chart=cc.getChart();
      }
      ODSUtils.setLayerVisibility( chart, m_request );
      ODSUtils.setAxisValues(  chart, m_request );

      //Gecachetes Chart verwenden
      if (chart!=null )
      {
        cic = new ChartImageContainer(chart, display, width, height);
        Image chartImg=cic.getImage();

        if (chartImg!=null )
        {
          m_response.setContentType( "image/jpeg" );
          ImageLoader il=new ImageLoader();
          ImageData id=chartImg.getImageData();
          il.data=new ImageData[]{id};
          try
          {
            outputStream = new BufferedOutputStream( m_response.getOutputStream() );
            il.save( outputStream, SWT.IMAGE_JPEG );
          }
          catch( IOException e )
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          finally
          {
            if (cic!=null)
              cic.dispose();
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
        {
          ODSException.showException( m_request, m_response, ERROR_CODE.DIAGRAM_NOT_DEFINED );
        }
      }


    }
    else
        ODSException.showException( m_request, m_response, ERROR_CODE.NO_NAME_SPECIFIED );

  }


}
