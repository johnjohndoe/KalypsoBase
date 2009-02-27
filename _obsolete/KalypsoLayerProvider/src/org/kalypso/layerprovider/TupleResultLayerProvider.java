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
package org.kalypso.layerprovider;

import java.net.URL;

import javax.xml.namespace.QName;

import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.layer.ChartDataProvider;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.ILayerProvider;
import org.kalypso.swtchart.configuration.parameters.impl.ParameterHelper;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.ksp.chart.configuration.AxisType;
import org.ksp.chart.configuration.LayerType;

/**
 * Layer provider which provides a {@link TupleResultLineChartLayer} on a feature beased observation.
 * <p>
 * The following arguments are supported:
 * <ul>
 * <li>showPoints [default=true]: If true, points are drawn even if no point style is given.</li>
 * <li>showlines [default=true]: Same as showPoints for lines.</li>
 * <li>featureKey: String. Key, where to get the feature from the ChartDataProvider.</li>
 * <li>propertyName: QName. If non null, the observation feature is found at that property of the given feature. Else
 * the given feature must be an observation itself.</li>
 * <li>domainComponentId: id of the component to use for the domain</li>
 * <li>valueComponentId: id of the component to use for the range</li>
 * </ul>
 * 
 * @author Gernot Belger
 */
public class TupleResultLayerProvider implements ILayerProvider
{
  private LayerType m_lt;

  private Chart m_chart;

  public void init( final Chart chart, final LayerType lt )
  {
    m_chart = chart;
    m_lt = lt;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayers()
   */
  public IChartLayer getLayer( final URL context )
  {
    final String configLayerId = m_lt.getName();

    final ParameterHelper ph = new ParameterHelper();
    ph.addParameters( m_lt.getParameters(), configLayerId );

    final boolean showPoints = Boolean.valueOf( ph.getParameterValue( "showPoints", "true" ) );
    final Boolean showLines = Boolean.valueOf( ph.getParameterValue( "showLines", "true" ) );

    final String featureKey = ph.getParameterValue( "featureKey", null );
    final String propertyNameStr = ph.getParameterValue( "propertyName", null );
    final QName propertyName = propertyNameStr == null ? null : QName.valueOf( propertyNameStr );

    final Feature baseFeature = ChartDataProvider.FEATURE_MAP.get( featureKey );
    final Feature feature;
    if( propertyName == null )
      feature = baseFeature;
    else
      feature = FeatureHelper.getFeature( baseFeature.getWorkspace(), baseFeature.getProperty( propertyName ) );

    if( feature == null )
      return null;

    final IObservation<TupleResult> obs = ObservationFeatureFactory.toObservation( feature );
    final TupleResult result = obs.getResult();

    final String domainComponentId = ph.getParameterValue( "domainComponentId", "" );
    final String valueComponentId = ph.getParameterValue( "valueComponentId", "" );

    final String domainAxisId = ((AxisType) m_lt.getAxes().getDomainAxisRef().getRef()).getName();
    final String valueAxisId = ((AxisType) m_lt.getAxes().getValueAxisRef().getRef()).getName();

    final IAxis domAxis = m_chart.getAxisRegistry().getAxis( domainAxisId );
    final IAxis valAxis = m_chart.getAxisRegistry().getAxis( valueAxisId );

    final TupleResultLineChartLayer icl = new TupleResultLineChartLayer( result, domainComponentId, valueComponentId, domAxis, valAxis );
    icl.setName( m_lt.getTitle() );
    icl.setVisibility( m_lt.isVisible() );
    icl.setShowPoints( showPoints );
    icl.setShowLines( showLines );

    return icl;
  }
}
