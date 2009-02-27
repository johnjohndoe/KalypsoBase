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

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.services.ods.Activator;
import org.kalypso.services.ods.helper.CapabilitiesLoader;
import org.kalypso.services.ods.helper.IODSConstants;
import org.kalypso.swtchart.configuration.ConfigLoader;
import org.kalypso.swtchart.configuration.TableLoader;
import org.kalypso.swtchart.exception.ConfigTableNotFoundException;
import org.w3c.dom.Node;

/**
 * @author burtscher
 *
 * IODSOperation to return O&M data as HTML table
 */
public class GetHTMLTable implements IODSOperation
{

  private RequestBean m_requestBean;
  private ResponseBean m_responseBean;

  public GetHTMLTable()
  {

  }


  /**
   * @see org.kalypso.services.ods.operation.IODSOperation#operate(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void operate(RequestBean requestBean, ResponseBean responseBean ) throws OWSException
  {
    Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet: GetHTMLTable", null ) );

    m_requestBean=requestBean;
    m_responseBean=responseBean;

    String name=m_requestBean.getParameterValue( "NAME" );
    if (name!=null)
    {
      
      Node configurationNode=(new CapabilitiesLoader(m_requestBean)).getConfigurationNode();
      ConfigLoader cl=null;
        try
        {
          cl = new ConfigLoader(configurationNode);
          String htmltable = TableLoader.createTable(cl, name, null);
          OutputStreamWriter out=new OutputStreamWriter(m_responseBean.getOutputStream());
          m_responseBean.setContentType( "text/html" );
          if (htmltable!=null)
            out.write( htmltable );
          out.close();
        }
        catch( JAXBException e )
        {
          e.printStackTrace();
        }
        catch( ConfigTableNotFoundException e )
        {
        //  ODSException.showException( m_request, m_response, ERROR_CODE.TABLE_NOT_DEFINED );
        }
        catch( IOException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }
    else
    {
     // ODSException.showException( m_request, m_response, ERROR_CODE.NO_NAME_SPECIFIED );
    }
  }

}
