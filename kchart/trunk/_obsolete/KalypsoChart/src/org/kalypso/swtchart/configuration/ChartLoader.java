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
package org.kalypso.swtchart.configuration;

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.KalypsoChartExtensions;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisProvider;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.ILayerProvider;
import org.kalypso.swtchart.configuration.parameters.impl.ParameterHelper;
import org.kalypso.swtchart.exception.AxisProviderException;
import org.kalypso.swtchart.exception.ConfigChartNotFoundException;
import org.kalypso.swtchart.exception.LayerProviderException;
import org.kalypso.swtchart.logging.Logger;
import org.ksp.chart.configuration.AxisType;
import org.ksp.chart.configuration.ChartType;
import org.ksp.chart.configuration.ConfigurationType;
import org.ksp.chart.configuration.LayerType;
import org.ksp.chart.configuration.ChartType.Layers.LayerRef;
import org.ksp.chart.configuration.LayerType.Axes;

/**
 * @author burtscher Creates a chart object from a configration
 */
public class ChartLoader
{
  /**
   * @param chart
   *          the (empty) chart widget which is to be filled
   * @param configChartName
   *          name of ChartConfiguration that is to be used
   */
  public static Chart createChart( Chart chart, ConfigLoader cl, String configChartName, final URL context ) throws ConfigChartNotFoundException
  {
    ChartType configChart = null;

    // ChartConfig auslesen
    if( cl != null )
    {
      configChart = cl.getCharts().get( configChartName );
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
    final List<LayerRef> layerRefs = configChart.getLayers().getLayerRef();
    for( final LayerRef layerRef : layerRefs )
    {
      final LayerType layerType = (LayerType) layerRef.getRef();

      final String providerId = layerType.getProvider().getId();

      // Achsen hinzuf¸gen
      Axes axes = layerType.getAxes();
      AxisType domainAxisType = (AxisType) axes.getDomainAxisRef().getRef();
      addConfigAxis( chart, domainAxisType );
      AxisType valueAxisType = (AxisType) axes.getValueAxisRef().getRef();
      addConfigAxis( chart, valueAxisType );

      try
      {
        final ILayerProvider provider = KalypsoChartExtensions.createLayerProvider( providerId );
        if( provider != null )
        {
          Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "LayerProvider loaded:" + provider.getClass().toString() );
          provider.init( chart, layerType );
          IChartLayer icl = provider.getLayer( context );
          if( icl != null )
          {
            Logger.trace( "Adding Layer " + icl.getName() );
            icl.setStyle( StyleLoader.createStyle( layerType ) );
            icl.setName( layerType.getName() );
            String title=layerType.getTitle();
            if (title == null | title.trim().equals( "" ))
              icl.setDescription( layerType.getName() );
            else
              icl.setDescription( title );
     //       icl.setDescription( layerType.getDescription() );
            icl.setVisibility( layerType.isVisible() );
            chart.addLayer( icl );
          }
        }
        else
        {
          Logger.logError( Logger.TOPIC_LOG_GENERAL, "No LayerProvider for " + providerId );
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

  /**
   * creates a concrete IAxis-Implementation from an AbstractAxisType derived from a ChartConfiguration, sets the
   * corresponding renderer and adds both to a given Chart
   */
  public static void addConfigAxis( final Chart chart, final AxisType axisType )
  {
    final IAxisRegistry ar = chart.getAxisRegistry();

    if( axisType != null )
    {
      final ParameterHelper ph = new ParameterHelper();
      ph.addParameters( axisType.getParameters(), axisType.getName() );

      final String provider = axisType.getProvider().getId();
      if( provider != null && provider.length() > 0 )
      {
        try
        {
          final IAxisProvider ap = KalypsoChartExtensions.createAxisProvider( provider );
          ap.init( axisType );
          final IAxis axis = ap.getAxis() ;
          
          // TODO: only add renderer if there is at least one layer associated with it
          // update, if layers where added/remove/visibility changed
          if( ar.getRenderer( axis ) == null )
          {
            IAxisRenderer axisRenderer = ap.getRenderer();
            Class< ? > dataClass = ap.getDataClass();
            ar.setRenderer( dataClass, axisRenderer );
          }
          ar.addAxis( axis );
        }
        catch( CoreException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( AxisProviderException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      else
      {
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "AxisProvider " + provider + " not known" );
      }
    }
    else
    {
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "AxisFactory: given axis is NULL." );
    }
  }

  /**
   * returns the first chartType of a configurationType
   */
  static public ChartType findFirstChart( final ConfigurationType config )
  {
    if( config == null )
      return null;

    ChartType ct = null;

    final List<Object> chartOrLayerOrAxis = config.getChartOrLayerOrAxis();
    for( final Object o : chartOrLayerOrAxis )
    {
      if( o instanceof ChartType )
      {
        ct = (ChartType) o;
        break;
      }
    }
    return ct;
  }

}