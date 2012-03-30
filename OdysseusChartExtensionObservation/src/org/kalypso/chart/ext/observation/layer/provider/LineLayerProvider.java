package org.kalypso.chart.ext.observation.layer.provider;

import java.net.URL;
import java.util.Calendar;

import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.chart.ext.observation.layer.TupleResultLineLayer;

import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;

public class LineLayerProvider extends AbstractLayerProvider
{
  @Override
  public IChartLayer getLayer( final URL context )
  {
    return new TupleResultLineLayer( this, getDataContainer(), getStyleSet());
  }

  protected TupleResultDomainValueData<Calendar, Double> getDataContainer( )
  {
    final String href = getParameterContainer().getParameterValue( "href", null ); //$NON-NLS-1$

    final String observationId = getParameterContainer().getParameterValue( "observationId", null ); //$NON-NLS-1$
    final String domainComponentName = getParameterContainer().getParameterValue( "domainComponent", null ); //$NON-NLS-1$
    final String targetComponentName = getParameterContainer().getParameterValue( "targetComponent", null ); //$NON-NLS-1$

    TupleResultDomainValueData<Calendar, Double> data = null;
    if( href != null && observationId != null && domainComponentName != null && targetComponentName != null )
    {
      data = new TupleResultDomainValueData<Calendar, Double>( getContext(), href, observationId, domainComponentName, targetComponentName );
    }
    return data;
  }

}
