package org.kalypso.chart.ext.test.layer.provider;

import java.net.URL;

import org.kalypso.chart.ext.test.data.EditableTestDataContainer;
import org.kalypso.chart.ext.test.layer.EditableLineLayer;

import de.openali.odysseus.chart.factory.config.parameters.impl.IntegerParser;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author alibu
 */
public class EditableLineLayerProvider<T_domain, T_target> extends AbstractLayerProvider
{

  private final String ROLE_LINE_STYLE = "line";

  private final String ROLE_POINT_STYLE = "point";

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  @Override
  public IChartLayer getLayer( final URL context )
  {
    return new EditableLineLayer( this, getDataContainer(), getStyleSet().getStyle( ROLE_LINE_STYLE, ILineStyle.class ), getStyleSet().getStyle( ROLE_POINT_STYLE, IPointStyle.class ) );
  }

  /**
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getDataContainer()
   */
  private EditableTestDataContainer getDataContainer( )
  {
    final Integer dataSize = getParameterContainer().getParsedParameterValue( "size", "100", new IntegerParser() );
    final Integer maxVal = getParameterContainer().getParsedParameterValue( "max_val", "1", new IntegerParser() );

    final EditableTestDataContainer data = new EditableTestDataContainer( dataSize, maxVal );
    return data;
  }

}
