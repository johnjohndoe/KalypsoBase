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
package org.kalypso.swtchart.chart.configuration;

import java.awt.Insets;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.KalypsoChartExtensions;
import org.kalypso.swtchart.chart.axis.AxisFactory;
import org.kalypso.swtchart.chart.axis.renderer.DateAxisRenderer;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;
import org.kalypso.swtchart.chart.axis.renderer.NumberAxisRenderer;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.ILayerProvider;
import org.kalypso.swtchart.chart.layer.impl.DoubleDataLayerProvider;
import org.ksp.chart.viewerconfiguration.AbstractAxisType;
import org.ksp.chart.viewerconfiguration.ChartType;
import org.ksp.chart.viewerconfiguration.ConfigurationType;
import org.ksp.chart.viewerconfiguration.LayerProviderType;
import org.ksp.chart.viewerconfiguration.ObjectFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;


/**
 * @author alibu
 *
 */
public class ChartLoader
{
  
  public final static ObjectFactory OF = new ObjectFactory();
  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );
  
  
  private String m_configPath;
  protected ConfigurationType m_config;
  protected final Chart m_chart;

  public ChartLoader(Chart chart, String path)
  {
      m_chart = chart;
      m_configPath=path;
      System.out.println("ChartConfiguration: Constructor");
      loadFile();
  }
  
  public void loadFile()
  {
    System.out.println("loadFile Start");
      try
      {
        Object o = JC.createUnmarshaller().unmarshal( new File(m_configPath) );
        if (o instanceof JAXBElement)
        {
          Object child=((JAXBElement) o).getValue();
          m_config=(ConfigurationType) child;
        }
        else if (o instanceof ConfigurationType)
        {
           m_config=(ConfigurationType) o;
        }
        System.out.println("Configuration geladen");
      }
      catch( JAXBException e )
      {
        e.printStackTrace();
      }
      catch (ClassCastException e)
      {
        e.printStackTrace();
      }
      System.out.println("loadFile End");
  }
  
  /**
   * Erstellt ein Chart aus der Konfiguration  
   * @param chart Das Chart-Objekt, das mit Leben gefüllt wird
   * @param configChartName Der Name der ChartConfiguration, die verwendet werden soll
   */
  public Chart createChart(String configChartName)
  {
    ChartType configChart=null;
    
    //ChartConfig auslesen
    if (m_config!=null)
    {
      List<ChartType> ccharts = m_config.getChart();
      for( ChartType c : ccharts )
      {
          if (c.getName().compareTo(configChartName)==0)
          {
            configChart=c;
            break;
          }
      }
    }
    

    
    
    if (configChart!=null)
    {
        //Achsen hinzufügen
      
        List<JAXBElement< ? extends AbstractAxisType>> jaxbAxes = configChart.getAxis();
        for( JAXBElement< ? extends AbstractAxisType> element : jaxbAxes )
        {
            AbstractAxisType abstractAxis=element.getValue();
            AxisFactory.addConfigAxis(m_chart, abstractAxis);
        }
        
        
        //Layer hinzufügen
        List<LayerProviderType> lps = configChart.getLayerProvider();
        for( LayerProviderType lp : lps )
        {
            String lpname=lp.getName();
            /**
             * Das ist nur zum Testen
             */
            if (
                  lpname.compareTo("org.ksp.observation.layerProvider.Test.Line")==0
                  || lpname.compareTo("org.ksp.observation.layerProvider.Test.Bar")==0  
              )
            {
              DoubleDataLayerProvider ddlp=new DoubleDataLayerProvider();
              ddlp.init(m_chart, lp);
              IChartLayer[] icl=ddlp.getLayers();
              for( IChartLayer layer : icl )
              {
                System.out.println("Adding Layer "+layer.getName());
                m_chart.addLayer(layer);
              }
            }
            else
            {
              try
              {
                final ILayerProvider provider = KalypsoChartExtensions.createProvider( lpname );
                if (provider!=null)
                {
                  provider.init(m_chart, lp);
                  IChartLayer[] icl=provider.getLayers();
                  if (icl!=null)
                  {
                    for( IChartLayer layer : icl )
                    {
                      System.out.println("Adding Layer "+layer.getName());
                      m_chart.addLayer(layer);
                    }
                  }
                }
                else
                {
                  System.out.println("No LayerProvider for "+lpname);
                }
              }
              catch( CoreException e )
              {
                e.printStackTrace();
              }
            }
        }
    }
    return m_chart;
  }


  
}

  