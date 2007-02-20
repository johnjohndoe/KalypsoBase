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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.TreeMap;

import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.ILayerProvider;
import org.kalypso.swtchart.configuration.parameters.impl.ParameterHelper;
import org.kalypso.swtchart.logging.Logger;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.ksp.chart.configuration.AxisType;
import org.ksp.chart.configuration.LayerType;

/**
 * @author burtscher Layer Provider for precipitation data from O&M data; it's looking for a feature named "wq_table1",
 *         then tries to transform it into an IObservation, creates a DefaultTupleResultLayer and uses the result
 *         components which go by the name "Wasserstand" (domain data) and "Abfluss" (value data) as data input for the
 *         layer; the DefaultTupleResultLayer draws its data as line chart
 * @TODO: change named feature to "wq_table" or - even better - move feature name to parameter section The following
 *        configuration parameters are needed for this LayerProvider: dataSource: URL or relative path leading to
 *        observation data
 * @TODO: dataSource is an independent configuration tag right now - it should be moved into the parameter section as
 *        not every layer provider needs an url
 */
public class WQTabelleLayerProvider implements ILayerProvider
{

  private Chart m_chart = null;

  private LayerType m_lt = null;

  public WQTabelleLayerProvider( )
  {

  }

  public void init( Chart chart, LayerType lt )
  {
    m_lt = lt;
    m_chart = chart;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayers()
   */
  public IChartLayer getLayer( URL context )
  {
    IChartLayer icl = null;
    String configLayerId = m_lt.getName();

    ParameterHelper ph = new ParameterHelper();

    ph.addParameters( m_lt.getParameters(), configLayerId );
    String url = ph.getParameterValue( "url", configLayerId, "" );
    String observationId = ph.getParameterValue( "observationId", configLayerId, "" );
    String domainAxisId = ((AxisType) m_lt.getAxes().getDomainAxisRef().getRef()).getName();
    String valueAxisId = ((AxisType) m_lt.getAxes().getValueAxisRef().getRef()).getName();

    try
    {
      GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( new URL( url ), null );
      Feature feature = workspace.getFeature( observationId );
      if( feature != null )
      {
        Logger.trace( "Found feature: " + feature.getId() );
      }
      IObservation<TupleResult> obs = ObservationFeatureFactory.toObservation( feature );

      IAxis domAxis = m_chart.getAxisRegistry().getAxis( domainAxisId );

      IAxis valAxis = m_chart.getAxisRegistry().getAxis( valueAxisId );

      TupleResult result = obs.getResult();

      // ResultComponent rausfinden
      final IComponent[] comps = result.getComponents();
      final TreeMap<String, IComponent> map = new TreeMap<String, IComponent>();
      for( int i = 0; i < comps.length; i++ )
      {
        // final QName qn = comps[i].getValueTypeName();
        IComponent c = comps[i];
        if( c.getName().compareTo( "Wasserstand" ) == 0 )
          map.put( "domain", c );
        else if( c.getName().compareTo( "Abfluss" ) == 0 )
          map.put( "value", c );
        if( map.size() == 2 )
          break;
      }

      TupleResultLineChartLayer trcl = new TupleResultLineChartLayer( result, map.get( "domain" ).getId(), map.get( "value" ).getId(), domAxis, valAxis );

      trcl.setName( m_lt.getTitle() );

    }
    catch( MalformedURLException e )
    {
      e.printStackTrace();
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
    return icl;
  }

}
