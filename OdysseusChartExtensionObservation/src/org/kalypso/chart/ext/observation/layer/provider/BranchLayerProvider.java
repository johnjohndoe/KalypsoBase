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

import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper;

/**
 * @deprecated
 * @author kimwerner FIXME not used
 */

@Deprecated
public class BranchLayerProvider extends AbstractLayerProvider
{
  @Override
  public BranchLayer getLayer( final URL context ) throws ConfigurationException
  {
    final String href = getParameterContainer().getParameterValue( "href", null ); // $NON-NLS-1$

    final String observationId = getParameterContainer().getParameterValue( "observationId", null ); // $NON-NLS-1$
    final String domainComponentName = getParameterContainer().getParameterValue( "domainComponent", null ); // $NON-NLS-1$
    final String targetComponentName = getParameterContainer().getParameterValue( "targetComponent", null ); // $NON-NLS-1$
    final String iconComponentName = getParameterContainer().getParameterValue( "iconComponent", null ); // $NON-NLS-1$

    GMLWorkspace workspace;
    try
    {
      workspace = GmlSerializer.createGMLWorkspace( new URL( getContext(), href ), null );
    }
    catch( final MalformedURLException e )
    {
      throw new ConfigurationException( "invalid URL", e );
    }
    catch( final Exception e )
    {
      throw new ConfigurationException( "an exception occurred", e );
    }
    final Feature feature = workspace.getFeature( observationId );
    if( feature != null )
      Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Found feature: " + feature.getId() );
    final IObservation<TupleResult> observation = ObservationFeatureFactory.toObservation( feature );

    final BranchLayer layer = new BranchLayer( this, observation.getResult(), domainComponentName, targetComponentName, iconComponentName, getStyleSet() );
    final Set<Entry<String, String>> entrySet = getMapperMap().entrySet();
    for( final Entry<String, String> e : entrySet )
      layer.addMapper( e.getKey(), (IRetinalMapper) getModel().getMapperRegistry().getMapper( e.getValue() ) );
    layer.init();
    return layer;

  }

}
