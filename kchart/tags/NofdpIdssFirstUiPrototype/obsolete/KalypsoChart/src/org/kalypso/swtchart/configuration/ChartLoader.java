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
package org.kalypso.swtchart.configuration;

import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.KalypsoChartExtensions;
import org.kalypso.swtchart.chart.axis.AxisFactory;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.ILayerProvider;
import org.kalypso.swtchart.chart.layer.LayerProviderException;
import org.kalypso.swtchart.chart.layer.impl.DoubleDataLayerProvider;
import org.kalypso.swtchart.exception.ConfigChartNotFoundException;
import org.ksp.chart.viewerconfiguration.AbstractAxisType;
import org.ksp.chart.viewerconfiguration.ChartType;
import org.ksp.chart.viewerconfiguration.ConfigurationType;
import org.ksp.chart.viewerconfiguration.LayerProviderType;

/**
 * @author burtscher
 * 
 * Creates a chart object from a configration
 */
public class ChartLoader
{
  /**
   * @param chart
   *          the (empty) chart widget which is to be filled 
   * @param configChartName
   *          name of ChartConfiguration that is to be used 
   */
  public static Chart createChart( Chart chart, ConfigurationType config, String configChartName, final URL context ) throws ConfigChartNotFoundException
  {
    ChartType configChart = null;

    // ChartConfig auslesen
    if( config != null )
    {
      final List<ChartType> ccharts = config.getChart();
      for( final ChartType c : ccharts )
      {
        if( c.getName().compareTo( configChartName ) == 0 )
        {
          configChart = c;
          break;
        }
      }
    }

    if( configChart == null )
    {
      throw new ConfigChartNotFoundException( configChartName );
    }
    else
    {
      configureChart( chart, configChart, context );
    }
    return chart;
  }

  
  public static void configureChart( final Chart chart, final ChartType configChart, final URL context )
  {
    // Achsen hinzufügen

    List<JAXBElement< ? extends AbstractAxisType>> jaxbAxes = configChart.getAxis();
    for( JAXBElement< ? extends AbstractAxisType> element : jaxbAxes )
    {
      AbstractAxisType abstractAxis = element.getValue();
      AxisFactory.addConfigAxis( chart, abstractAxis );
    }

    // Layer hinzufügen
    final List<LayerProviderType> lps = configChart.getLayerProvider();
    for( LayerProviderType lp : lps )
    {
      String lpname = lp.getName();
      /**
       * Das ist nur zum Testen
       */
      if( lpname.compareTo( "org.ksp.observation.layerProvider.Test.Line" ) == 0 || lpname.compareTo( "org.ksp.observation.layerProvider.Test.Bar" ) == 0 )
      {
        final DoubleDataLayerProvider ddlp = new DoubleDataLayerProvider();
        ddlp.init( chart, lp );
        final IChartLayer[] icl = ddlp.getLayers( context );
        for( IChartLayer layer : icl )
        {
          System.out.println( "Adding Layer " + layer.getName() );
          chart.addLayer( layer );
        }
      }
      else
      {
        try
        {
          final ILayerProvider provider = KalypsoChartExtensions.createProvider( lpname );
          if( provider != null )
          {
            provider.init( chart, lp );
            IChartLayer[] icl = provider.getLayers( context );
            if( icl != null )
            {
              for( final IChartLayer layer : icl )
              {
                System.out.println( "Adding Layer " + layer.getName() );
                chart.addLayer( layer );
              }
            }
          }
          else
          {
            System.out.println( "No LayerProvider for " + lpname );
          }
        }
        catch( final CoreException e )
        {
          e.printStackTrace();
        }
        catch( final LayerProviderException e )
        {
          e.printStackTrace();
        }
      }
    }
  }

}
