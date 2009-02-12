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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.services.ods.Activator;
import org.kalypso.services.ods.helper.CachedChart;
import org.kalypso.services.ods.helper.CapabilitiesLoader;
import org.kalypso.services.ods.helper.ChartCache;
import org.kalypso.services.ods.helper.DisplayHelper;
import org.kalypso.services.ods.helper.ODSUtils;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.logging.Logger;

/**
 * @author burtscher
 * IODS operation to display an image containing a chart
 */
public class GetPlot implements IODSOperation, Runnable
{
    private RequestBean m_requestBean;
    private ResponseBean m_responseBean;


    public GetPlot()
    {

    }


public void operate( RequestBean requestBean, ResponseBean responseBean )
 {
    m_requestBean=requestBean;
    m_responseBean=responseBean;
    DisplayHelper.getInstance().getDisplay().syncExec( this );
 }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run( )
  {
    Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet: GetPlot", null ) );
    int width=500;
    int height=400;
    String reqWidth=m_requestBean.getParameterValue( "WIDTH" );
    String reqHeight=m_requestBean.getParameterValue( "HEIGHT" );
    String reqName=m_requestBean.getParameterValue( "NAME" );
    if (reqWidth!=null)
      width=Integer.parseInt( reqWidth );
    if (reqHeight!=null)
      height=Integer.parseInt( reqHeight );
    //der Name muss da sein, sonst kann kein Chart ausgew‰hlt werden
    if (reqName!=null)
    {
      Display display=DisplayHelper.getInstance().getDisplay();
      BufferedOutputStream outputStream=null;
      Image img=null;
      Shell shell=null;
      Chart chart=null;
      GCWrapper gcw=null;
      GC gc=null;
      try
      {


          img=new Image(display, width, height);

          CachedChart cc=ChartCache.getInstance().getObject( reqName );
          if (cc!=null)
          {
              chart=cc.getChart();
              Logger.trace("GetLegend: Using cached chart");
          }
          else
          {
            Logger.trace("GetLegend: Creating new chart");
            cc=new CachedChart((new CapabilitiesLoader(m_requestBean)).getConfigurationNode(), reqName);
            ChartCache.getInstance().addObject( reqName, cc );
            chart=cc.getChart();
          }


          chart.setPlotSize( width, height );
          ODSUtils.setAxisValues( chart, m_requestBean );
          ODSUtils.setLayerVisibility( chart, m_requestBean );

          gc=new GC(img);
          gcw=new GCWrapper(gc);
          img=chart.getPlot().paintChart( display, gcw, new Rectangle(0,0,width, height), null );

          if (img!=null)
          {
            m_responseBean.setContentType( "image/png" );
            ImageLoader il=new ImageLoader();
            ImageData id=img.getImageData();
            il.data=new ImageData[]{id};
            outputStream = new BufferedOutputStream( m_responseBean.getOutputStream() );
            il.save( outputStream, SWT.IMAGE_PNG );
          }
          else
          {
        //    ODSException.showException( m_request, m_response, ERROR_CODE.DIAGRAM_NOT_DEFINED );
          }
      }
      catch( Exception e )
      {
        e.printStackTrace();
      }
      finally
      {
        if (img!=null && !img.isDisposed())
          img.dispose();
        if(shell!=null && !shell.isDisposed())
          shell.dispose();
        if (chart!=null && !chart.isDisposed())
          chart.dispose();
        if (gcw!=null && !gcw.isDisposed())
          gcw.dispose();
        if (gc!=null && !gc.isDisposed())
          gc.dispose();

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
       //ODSException.showException( m_request, m_response, ERROR_CODE.NO_NAME_SPECIFIED );
    }
  }


}
