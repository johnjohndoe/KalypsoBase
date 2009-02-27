/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.services.ods.Activator;
import org.kalypso.services.ods.helper.CapabilitiesLoader;
import org.kalypso.services.ods.helper.IODSConstants;
import org.kalypso.swtchart.configuration.ConfigLoader;
import org.ksp.chart.configuration.AxisType;
import org.ksp.chart.configuration.ChartType;
import org.ksp.chart.configuration.ConfigurationType;
import org.ksp.chart.configuration.LayerType;
import org.ksp.chart.configuration.ObjectFactory;
import org.ksp.chart.configuration.ChartType.Layers.LayerRef;
import org.w3c.dom.Node;

/**
 * @author burtscher IODSOperation to return O&M data as HTML table
 */
public class GetAxesInfo implements IODSOperation
{

  private RequestBean m_requestBean;

  private ResponseBean m_responseBean;

  public final static ObjectFactory m_of = new org.ksp.chart.configuration.ObjectFactory();

  private static final JAXBContext m_JC = JaxbUtilities.createQuiet( org.ksp.chart.configuration.ObjectFactory.class );

  public GetAxesInfo( )
  {

  }

  /**
   * @see org.kalypso.services.ods.operation.IODSOperation#operate(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  public void operate( RequestBean requestBean, ResponseBean responseBean ) throws OWSException
  {
    m_responseBean = responseBean;
    m_requestBean = requestBean;

    Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet: GetHTMLTable", null ) );

    String name = m_requestBean.getParameterValue( "NAME" );
    if( name != null )
    {
      // HTMLTabelle erzeugen
      String pathVC = System.getProperty( IODSConstants.ODS_CONFIG_PATH_KEY, IODSConstants.ODS_CONFIG_PATH_DEFAULT ) + IODSConstants.ODS_CONFIG_NAME;

      ConfigLoader cl = null;
      try
      {
        
        
        ConfigurationType ct = m_of.createConfigurationType();
        Node configNode=(new CapabilitiesLoader(m_requestBean)).getConfigurationNode();
        cl = new ConfigLoader( configNode );
        ConfigurationType c = cl.getConfiguration();
        ChartType configChart = cl.getCharts().get( name );
        HashMap<String, AxisType> addedAxes = new HashMap<String, AxisType>();

        List<LayerRef> layerRefs = configChart.getLayers().getLayerRef();
        for( LayerRef layerRef : layerRefs )
        {
          LayerType layerType = (LayerType) layerRef.getRef();
          AxisType domainAxis = (AxisType) layerType.getAxes().getDomainAxisRef().getRef();
          AxisType valueAxis = (AxisType) layerType.getAxes().getValueAxisRef().getRef();

          // Die Achsen werden einfach in die Map geschrieben-
          // falls eine schon drin ist, wird sie halt überschrieben
          addedAxes.put( domainAxis.getName(), domainAxis );
          addedAxes.put( valueAxis.getName(), valueAxis );

        }

        Set<String> axisNames = addedAxes.keySet();
        for( String axisName : axisNames )
        {
          ct.getChartOrLayerOrAxis().add( addedAxes.get( axisName ) );
        }

        OutputStreamWriter out = null;
        out = new OutputStreamWriter( m_responseBean.getOutputStream() );

        m_responseBean.setContentType( "text/xml" );

        StringWriter sw = new StringWriter();

        JAXBElement<ConfigurationType> outputConfigType = m_of.createConfiguration( ct );
        Marshaller m = m_JC.createMarshaller();
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.marshal( outputConfigType, sw );
        String infoString = sw.toString();

        try
        {
          sw.close();
        }
        catch( IOException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        try
        {
          out.write( infoString );
          out.close();
        }
        catch( IOException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
      catch( JAXBException e )
      {
        e.printStackTrace();
      }

    }
    else
    {
      throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "", "" );
    }

  }

}
