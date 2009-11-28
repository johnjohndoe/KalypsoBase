package org.kalypso.chart.ext.test.layer.provider;

import java.net.URL;

import org.kalypso.chart.ext.test.data.GenericAxisTimeSeriesDataContainer;
import org.kalypso.chart.ext.test.layer.GenericAxisLineLayer;

import de.openali.odysseus.chart.factory.config.parameters.impl.IntegerParser;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author alibu
 */
public class GenericAxisLineLayerProvider extends AbstractLayerProvider
{

  private final String ROLE_POINT_STYLE = "point";

  private final String ROLE_LINE_STYLE = "line";

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  public IChartLayer getLayer( final URL context )
  {
    return new GenericAxisLineLayer( getDataContainer(), getStyleSet().getStyle( ROLE_LINE_STYLE, ILineStyle.class ), getStyleSet().getStyle( ROLE_POINT_STYLE, IPointStyle.class ) );
  }

  /**
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getDataContainer()
   */
  protected GenericAxisTimeSeriesDataContainer getDataContainer( )
  {
    final Integer dataSize = getParameterContainer().getParsedParameterValue( "size", "2000", new IntegerParser() );
    final Integer maxVal = getParameterContainer().getParsedParameterValue( "max_val", "1", new IntegerParser() );

    final GenericAxisTimeSeriesDataContainer data = new GenericAxisTimeSeriesDataContainer( dataSize, maxVal );
    return data;
  }

}
