package org.kalypso.chart.ext.observation.layer.provider;

import java.net.URL;
import java.util.Calendar;

import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.chart.ext.observation.layer.TupleResultLineLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.model.data.IDataContainer;
import org.kalypso.chart.framework.model.layer.IChartLayer;

//TODO why do we still have several tuple result layer?
//@Alex: please combine them to ONE implementation!
public class LineLayerProvider extends AbstractLayerProvider
{
  public IChartLayer getLayer( final URL context ) throws LayerProviderException
  {
    return new TupleResultLineLayer();
  }

  /**
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getDataContainer()
   */
  public IDataContainer getDataContainer( ) throws LayerProviderException
  {
    final String href = getParameterContainer().getParameterValue( "href", null );

    final String observationId = getParameterContainer().getParameterValue( "observationId", null );
    final String domainComponentName = getParameterContainer().getParameterValue( "domainComponent", null );
    final String targetComponentName = getParameterContainer().getParameterValue( "targetComponent", null );

    TupleResultDomainValueData<Calendar, Double> data = null;
    if( href != null && observationId != null && domainComponentName != null && targetComponentName != null )
      data = new TupleResultDomainValueData<Calendar, Double>( getContext(), href, observationId, domainComponentName, targetComponentName );
    return data;
  }

}
