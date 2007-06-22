package org.kalypso.chart.ext.observation.layer.provider;

import java.net.URL;
import java.util.Calendar;

import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.chart.ext.observation.layer.TupleResultLineLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;

public class LineLayerProvider extends AbstractLayerProvider
{

  public IChartLayer getLayer( URL context ) throws LayerProviderException
  {

    IChartLayer icl = null;

    String href = getParameterContainer().getParameterValue( "href", null );

    String observationId = getParameterContainer().getParameterValue( "observationId", null );
    String domainComponentName = getParameterContainer().getParameterValue( "domainComponent", null );
    String targetComponentName = getParameterContainer().getParameterValue( "targetComponent", null );

    TupleResultDomainValueData<Calendar, Double> data = null;
    if( href != null && observationId != null && domainComponentName != null && targetComponentName != null )
      data = new TupleResultDomainValueData<Calendar, Double>( context, href, observationId, domainComponentName, targetComponentName );

    String domainAxisId = getLayerType().getMapper().getDomainAxisRef().getRef();
    String targetAxisId = getLayerType().getMapper().getTargetAxisRef().getRef();
    IAxis domainAxis = getChartModel().getMapperRegistry().getAxis( domainAxisId );
    IAxis targetAxis = getChartModel().getMapperRegistry().getAxis( targetAxisId );

    if( data != null )
      icl = new TupleResultLineLayer( data, domainAxis, targetAxis );

    return icl;
  }

}
