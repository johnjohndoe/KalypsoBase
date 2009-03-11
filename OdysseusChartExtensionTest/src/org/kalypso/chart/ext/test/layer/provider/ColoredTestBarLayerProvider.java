package org.kalypso.chart.ext.test.layer.provider;

import java.net.URL;

import org.kalypso.chart.ext.test.layer.ColoredTestBarLayer;

import de.openali.odysseus.chart.ext.base.layer.provider.CSVBarLayerProvider;
import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;

/**
 * @author alibu
 */
public class ColoredTestBarLayerProvider extends CSVBarLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayer(java.net.URL)
   */
  @Override
  @SuppressWarnings( { "unused", "unchecked" })
  public IChartLayer getLayer( URL context ) throws ConfigurationException
  {
    return new ColoredTestBarLayer( getDataContainer(), getStyleSet().getStyle( "bar", IAreaStyle.class ) );
  }

}
