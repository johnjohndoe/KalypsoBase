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
package org.kalypso.layerprovider.observation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kalypso.layerprovider.TupleResultLineChartLayer;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.observation.result.TupleResultUtilities;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.ILayerProvider;
import org.kalypso.swtchart.chart.layer.LayerProviderException;
import org.kalypso.swtchart.configuration.StyleLoader;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;
import org.ksp.chart.layerstyle.StyleType;
import org.ksp.chart.viewerconfiguration.AbstractAxisType;
import org.ksp.chart.viewerconfiguration.AxisMappingType;
import org.ksp.chart.viewerconfiguration.ChartElementType;
import org.ksp.chart.viewerconfiguration.LayerProviderType;
import org.ksp.chart.viewerconfiguration.ParameterType;
import org.ksp.chart.viewerconfiguration.ChartElementType.AxisMapping;
import org.ksp.chart.viewerconfiguration.ChartElementType.StyleLink;

/**
 * @author Gernot Belger
 */
public class WasserstandLayerProvider implements ILayerProvider
{
  private final HashMap<String, String> m_parameters = new HashMap<String, String>();

  private LayerProviderType m_lpt;

  private Chart m_chart;

  public void init( final Chart chart, final LayerProviderType lpt )
  {
    m_chart = chart;
    m_lpt = lpt;

    final List<ParameterType> parameters = m_lpt.getParameter();
    for( final ParameterType parameter : parameters )
      m_parameters.put( parameter.getName(), parameter.getValue() );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayers()
   */
  public IChartLayer[] getLayers( final URL context ) throws LayerProviderException
  {
    final List<IChartLayer> layers = new ArrayList<IChartLayer>();

    final String href = m_parameters.get( "href" );
    final String xpath = m_parameters.get( "gmlxpath" );

    try
    {
      if( href == null )
        return layers.toArray( new IChartLayer[layers.size()] );

      final GMLXPath path = new GMLXPath( xpath );

      final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( new URL( context, href ), null );

      final Object object = GMLXPathUtilities.query( path, workspace );

      final Feature feature;
      if( object == workspace )
        feature = workspace.getRootFeature();
      else if( object instanceof Feature )
        feature = (Feature) object;
      else
      {
        System.out.println( "bad path not set: " + xpath );
        return layers.toArray( new IChartLayer[layers.size()] );
      }

      final IObservation<TupleResult> obs = ObservationFeatureFactory.toObservation( feature );
      final TupleResult result = obs.getResult();

      final List<ChartElementType> ces = m_lpt.getChartElement();
      for( final ChartElementType ce : ces )
      {
        final AxisMapping am = ce.getAxisMapping();

        final AxisMappingType domain = am.getDomain();
        final AxisMappingType value = am.getValue();

        final AbstractAxisType domLink = (AbstractAxisType) domain.getAxisLink();
        final IAxis domAxis = m_chart.getAxisRegistry().getAxis( domLink.getId() );

        final IComponent domainComponent = TupleResultUtilities.findComponentById( result, domain.getEquation() );

        final AbstractAxisType valLink = (AbstractAxisType) value.getAxisLink();
        final IAxis valAxis = m_chart.getAxisRegistry().getAxis( valLink.getId() );

        final IComponent valueComponent = TupleResultUtilities.findComponentById( result, value.getEquation() );

        if( domainComponent != null && valueComponent != null )
        {
          final List<ParameterType> parameter = m_lpt.getParameter();

          final IChartLayer wl = new TupleResultLineChartLayer( result, domainComponent, valueComponent, domAxis, valAxis, parameter );

          final List<StyleLink> styleList = ce.getStyleLink();
          if( styleList.size() > 0 )
            wl.setStyle( StyleLoader.createStyle( (StyleType) styleList.get( 0 ).getRef() ) );
          wl.setName( ce.getName() );
          layers.add( wl );
        }

      }
    }
    catch( final MalformedURLException e )
    {
      throw new LayerProviderException( "URL konnte nicht aufgelöst werden: " + href, e );
    }
    catch( final GMLXPathException e )
    {
      throw new LayerProviderException( "Ungültiger GML-XPATH: " + xpath, e );
    }
    catch( final Exception e )
    {
      throw new LayerProviderException( "GML Workspace konnte nicht geladen werden: " + xpath, e );
    }

    return layers.toArray( new IChartLayer[layers.size()] );
  }
}
