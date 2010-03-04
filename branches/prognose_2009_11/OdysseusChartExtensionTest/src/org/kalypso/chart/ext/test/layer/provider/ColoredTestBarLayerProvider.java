package org.kalypso.chart.ext.test.layer.provider;

import java.net.URL;

import org.kalypso.chart.ext.test.layer.ColoredTestBarLayer;

import de.openali.odysseus.chart.ext.base.layer.provider.CSVBarLayerProvider;
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
  public IChartLayer getLayer(final URL context)
  {
    return new ColoredTestBarLayer( getDataContainer(), getStyleSet().getStyle( "bar", IAreaStyle.class ) );
  }

}
