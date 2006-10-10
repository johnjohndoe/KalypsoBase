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
package org.kalypso.swtchart.chart.layer.impl;

import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.ILayerProvider;
import org.kalypso.swtchart.chart.styles.ILayerStyle;
import org.kalypso.swtchart.configuration.StyleLoader;
import org.ksp.chart.layerstyle.StyleType;
import org.ksp.chart.viewerconfiguration.AbstractAxisType;
import org.ksp.chart.viewerconfiguration.AxisMappingType;
import org.ksp.chart.viewerconfiguration.ChartElementType;
import org.ksp.chart.viewerconfiguration.LayerProviderType;
import org.ksp.chart.viewerconfiguration.ChartElementType.AxisMapping;
import org.ksp.chart.viewerconfiguration.ChartElementType.StyleLink;

/**
 * @author burtscher
 */
public class DoubleDataLayerProvider implements ILayerProvider
{
  private LayerProviderType m_lpt;

  private Chart m_chart;

  /**
   * @see org.kalypso.swtchart.layer.ILayerProvider#getLayers()
   */
  public IChartLayer[] getLayers( final URL context )
  {
    IAxisRegistry ar = m_chart.getAxisRegistry();

    Vector<IChartLayer> layers = new Vector<IChartLayer>();

    String name = m_lpt.getName();

    List<ChartElementType> ces = m_lpt.getChartElement();
    for( ChartElementType ce : ces )
    {
      // Achsen für Layer herausfinden
      AxisMapping am = ce.getAxisMapping();
      // DomainAxis
      AxisMappingType amt_domain = am.getDomain();
      AbstractAxisType domainAxis = (AbstractAxisType) amt_domain.getAxisLink();
      String domainId = domainAxis.getId();
      // ValueAxis
      AxisMappingType amt_value = am.getValue();
      AbstractAxisType valueAxis = (AbstractAxisType) amt_value.getAxisLink();
      String valueId = valueAxis.getId();

      IAxis domain = ar.getAxis( domainId );
      IAxis value = ar.getAxis( valueId );
      IChartLayer cl = null;
      if( name.compareTo( "org.ksp.observation.layerProvider.Test.Line" ) == 0 )
      {
        cl = new TestLayer( domain, value );
      }
      else if( name.compareTo( "org.ksp.observation.layerProvider.Test.Bar" ) == 0 )
      {
        cl = new TestStackLayer( domain, value );
      }

      final List<StyleLink> styleLink = ce.getStyleLink();
      final StyleType st = styleLink.size() > 0 ? (StyleType) (styleLink.get( 0 ).getRef()) : null;

      final ILayerStyle ls = StyleLoader.createStyle( st );

      cl.setStyle( ls );
      cl.setName( ce.getName() );

      layers.add( cl );
    }

    return layers.toArray( new IChartLayer[layers.size()] );
  }

  /**
   * @see org.kalypso.swtchart.layer.ILayerProvider#init(org.kalypso.swtchart.Chart,
   *      org.ksp.chart.viewerconfiguration.LayerProviderType)
   */
  public void init( final Chart chart, final LayerProviderType lpt )
  {
    m_chart = chart;
    m_lpt = lpt;
  }

}
