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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.services.ods.Activator;
import org.kalypso.services.ods.operation.IODSConstants.ERROR_CODE;
import org.kalypso.swtchart.configuration.ConfigurationLoader;
import org.ksp.chart.viewerconfiguration.AbstractAxisType;
import org.ksp.chart.viewerconfiguration.ChartType;
import org.ksp.chart.viewerconfiguration.ConfigurationType;
import org.ksp.chart.viewerconfiguration.ObjectFactory;

/**
 * @author burtscher
 *
 * IODSOperation to return O&M data as HTML table
 */
public class GetAxesInfo implements IODSOperation
{

  private HttpServletRequest m_request;
  private HttpServletResponse m_response;

  public final static ObjectFactory m_of = new org.ksp.chart.viewerconfiguration.ObjectFactory();
  private static final JAXBContext m_JC = JaxbUtilities.createQuiet( org.ksp.chart.viewerconfiguration.ObjectFactory.class );
  
  public GetAxesInfo()
  {
    
  }
  
  
  /**
   * @see org.kalypso.services.ods.operation.IODSOperation#operate(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void operate(HttpServletRequest request, HttpServletResponse response ) throws IOException
  {
    m_request=request;
    m_response=response;
    
    Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet: GetHTMLTable", null ) );

    String name=m_request.getParameter( "NAME" );
    if (name!=null)
    {
      //    HTMLTabelle erzeugen
      String pathVC=System.getProperty( IODSConstants.ODS_CONFIG_PATH_KEY, IODSConstants.ODS_CONFIG_PATH_DEFAULT)+IODSConstants.ODS_CONFIG_NAME;
      
      ConfigurationLoader cl=null;
        try
        {
          /*
          AxisInfoType ait=m_of.createAxisInfoType();
          */
          ChartType ct = m_of.createChartType();
          cl = new ConfigurationLoader(pathVC);
          ConfigurationType c = cl.getConfiguration();
          List<ChartType> charts = c.getChart();
          for( ChartType chart : charts )
          {
            if (chart.getName().compareTo( name)==0)
            {
             // Chart aic = m_of.createChart();
              //aic.setName( name );
              ct.setName( name );
              
              //ait.getChart().add( aic );
              
              
                List<JAXBElement< ? extends AbstractAxisType>> axes = chart.getAxis();
                for( JAXBElement< ? extends AbstractAxisType> axis : axes )
                {
                  ct.getAxis().add( axis );
                   
//                  ait.getAxis().add( axis );
                }
                
                
            }
          }
          
          
          
          final PrintWriter out=m_response.getWriter();
          m_response.setContentType( "text/xml" );
          
          StringWriter sw=new StringWriter();
        //  JAXBContext jc=JAXBContext.newInstance( "org.ksp.ods.axesinfo" );
        //  Marshaller m= jc.createMarshaller();
          
          JAXBElement<ChartType> chart = m_of.createChart( ct );
          Marshaller m= m_JC.createMarshaller();
          m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
          m.marshal(chart, sw);
          String infoString=sw.toString();
          
          sw.close();
          out.write( infoString );
          out.close();
        }
        catch( JAXBException e )
        {
          e.printStackTrace();
        }

    }
    else
      ODSException.showException( m_request, m_response, ERROR_CODE.NO_NAME_SPECIFIED );
    
  }
  
}
