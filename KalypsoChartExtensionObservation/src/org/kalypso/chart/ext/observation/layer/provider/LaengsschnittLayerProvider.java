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
package org.kalypso.chart.ext.observation.layer.provider;

import java.net.MalformedURLException;
import java.net.URL;

import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.chart.ext.observation.layer.TupleResultLineLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;
import org.ksp.chart.factory.LayerType;

/**
 * @author Gernot Belger
 */
public class LaengsschnittLayerProvider extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayers()
   */
  public IChartLayer getLayer( final URL context ) throws LayerProviderException
  {
    final LayerType lt = getLayerType();
    final IChartModel chartModel = getChartModel();

    IChartLayer icl = null;
    final String configLayerId = lt.getTitle();

    final IParameterContainer pc = getParameterContainer();

    // final ParameterHelper ph = new ParameterHelper();
    // pc.addParameters( m_lt.getParameters(), configLayerId );

    final String href = pc.getParameterValue( "href", null );
    final String xpath = pc.getParameterValue( "gmlxpath", "" );

    try
    {
      if( href == null )
        return null;

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
        Logger.trace( "bad path not set: " + xpath );
        return null;
      }

      final IObservation<TupleResult> obs = ObservationFeatureFactory.toObservation( feature );
      final TupleResult result = obs.getResult();

      final String domainAxisId = lt.getMapper().getDomainAxisRef().getRef();
      final String valueAxisId = lt.getMapper().getTargetAxisRef().getRef();

      final IAxis< ? > domAxis = chartModel.getMapperRegistry().getAxis( domainAxisId );
      final IAxis< ? > valAxis = chartModel.getMapperRegistry().getAxis( valueAxisId );

      final String observationId = getParameterContainer().getParameterValue( "observationId", null );
      final String domainComponentName = getParameterContainer().getParameterValue( "domainComponentId", null );
      final String targetComponentName = getParameterContainer().getParameterValue( "targetComponentId", null );

      TupleResultDomainValueData< ? , ? > data = null;
      if( href != null && observationId != null && domainComponentName != null && targetComponentName != null )
        data = new TupleResultDomainValueData( context, href, observationId, domainComponentName, targetComponentName );

      icl = new TupleResultLineLayer( data, domAxis, valAxis );

      icl.setTitle( lt.getTitle() );
      icl.setVisible( lt.getVisible() );
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

    return icl;
  }
}
