package org.kalypso.chart.ext.test.layer.provider;

import java.net.URL;

import org.kalypso.chart.ext.test.data.EditableTestDataContainer;
import org.kalypso.chart.ext.test.layer.EditableLineLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.IntegerParser;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;

/**
 * @author alibu
 */
public class EditableLineLayerProvider<T_domain, T_target> extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  @SuppressWarnings( { "unused", "unchecked" })
  public IChartLayer< ? , ? > getLayer( URL context ) throws LayerProviderException
  {
    IChartLayer< ? , ? > icl = null;

    final String domainAxisId = getLayerType().getMapper().getDomainAxisRef().getRef();
    final String targetAxisId = getLayerType().getMapper().getTargetAxisRef().getRef();

    final IAxis<Number> domAxis = (IAxis<Number>) getChartModel().getMapperRegistry().getAxis( domainAxisId );
    final IAxis<Number> valAxis = (IAxis<Number>) getChartModel().getMapperRegistry().getAxis( targetAxisId );

    final Integer dataSize = getParameterContainer().getParsedParameterValue( "size", "2000", new IntegerParser() );
    final Integer maxVal = getParameterContainer().getParsedParameterValue( "max_val", "1", new IntegerParser() );

    EditableTestDataContainer data = new EditableTestDataContainer( dataSize, maxVal );
    icl = new EditableLineLayer( data, domAxis, valAxis );
    icl.setTitle( getLayerType().getTitle() );

    return icl;
  }

}
