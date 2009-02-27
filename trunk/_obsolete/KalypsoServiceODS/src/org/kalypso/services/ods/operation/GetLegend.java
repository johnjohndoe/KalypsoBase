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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.services.ods.Activator;
import org.kalypso.services.ods.helper.CachedChart;
import org.kalypso.services.ods.helper.CapabilitiesLoader;
import org.kalypso.services.ods.helper.ChartCache;
import org.kalypso.services.ods.helper.DisplayHelper;
import org.kalypso.services.ods.helper.ODSUtils;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.legend.LegendImageContainer;
import org.kalypso.swtchart.logging.Logger;

/**
 * @author burtscher
 *
 * operation creating a legend image from a given name
 */
public class GetLegend implements IODSOperation, Runnable
{

  private RequestBean m_requestBean;
  private ResponseBean m_responseBean;


  public GetLegend()
  {

  }

  /**
   * @see org.kalypso.services.ods.operation.IODSOperation#operate(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
 public void operate( RequestBean requestBean, ResponseBean responseBean )
  {
     m_requestBean=requestBean;
     m_responseBean=responseBean;
     Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet: GetLegend", null ) );
     DisplayHelper.getInstance().getDisplay().syncExec( this );
  }


  /**
   * @see java.lang.Runnable#run()
   */
  public void run( )
  {
    String reqName = m_requestBean.getParameterValue( "NAME" );
    //der Name muss da sein, sonst kann kein Chart ausgew‰hlt werden
    if (reqName!=null)
    {
      Display display=null;
      BufferedOutputStream outputStream=null;
      LegendImageContainer lic=null;
      Chart chart=null;
      try
      {
          CachedChart cc=ChartCache.getInstance().getObject( reqName );
          if (false && cc!=null)
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
          ODSUtils.setLayerVisibility( chart, m_requestBean );
          display=DisplayHelper.getInstance().getDisplay();
          lic = new LegendImageContainer(chart, display);
          Image legendImg=lic.getImage();
          m_responseBean.setContentType( "image/jpeg" );
          ImageLoader il=new ImageLoader();
          ImageData id=legendImg.getImageData();
          il.data=new ImageData[]{id};
          outputStream = new BufferedOutputStream( m_responseBean.getOutputStream() );
          il.save( outputStream, SWT.IMAGE_JPEG );
          outputStream.close();
      }
      catch( Exception e )
      {
        e.printStackTrace();
      }
      finally
      {
        if (lic!=null)
          lic.dispose();
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
    // ODSException.showException( m_request, m_response, ERROR_CODE.NO_NAME_SPECIFIED );
    }
  }
}
