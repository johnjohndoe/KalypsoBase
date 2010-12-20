package org.kalypso.chart.ext.observation.layer.provider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Set;

import org.kalypso.chart.ext.observation.layer.BranchLayer;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

public class BranchLayerProvider extends AbstractLayerProvider
{
  @Override
  public BranchLayer getLayer( final URL context ) throws ConfigurationException
  {
    final String href = getParameterContainer().getParameterValue( "href", null );

    final String observationId = getParameterContainer().getParameterValue( "observationId", null );
    final String domainComponentName = getParameterContainer().getParameterValue( "domainComponent", null );
    final String targetComponentName = getParameterContainer().getParameterValue( "targetComponent", null );
    final String iconComponentName = getParameterContainer().getParameterValue( "iconComponent", null );

    GMLWorkspace workspace;
    try
    {
      workspace = GmlSerializer.createGMLWorkspace( new URL( getContext(), href ), null );
    }
    catch( MalformedURLException e )
    {
      throw new ConfigurationException( "invalid URL", e );
    }
    catch( Exception e )
    {
      throw new ConfigurationException( "an exception occurred", e );
    }
    final Feature feature = workspace.getFeature( observationId );
    if( feature != null )
      Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Found feature: " + feature.getId() );
    IObservation<TupleResult> observation = ObservationFeatureFactory.toObservation( feature );

    BranchLayer layer = new BranchLayer( observation.getResult(), domainComponentName, targetComponentName, iconComponentName, getStyleSet().getStyle( "line", ILineStyle.class ), getStyleSet().getStyle( "point", IPointStyle.class ) );
    Set<Entry<String, String>> entrySet = getMapperMap().entrySet();
    for( Entry<String, String> e : entrySet )
      layer.addMapper( e.getKey(), (IRetinalMapper) getChartModel().getMapperRegistry().getMapper( e.getValue() ) );
    layer.init();
    return layer;

  }

}
