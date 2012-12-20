package de.openali.odysseus.chart.ext.base.deprecated;

import java.net.URL;

import de.openali.odysseus.chart.ext.base.layer.DomainIntervalBarLayer;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;

/**
 * @author alibu
 */
public class CSVBarLayerProvider extends AbstractLayerProvider
{
  private final String ROLE_BAR_STYLE = "bar"; //$NON-NLS-1$

  @Override
  public IChartLayer getLayer( final URL context )
  {
    return new DomainIntervalBarLayer( this, getDataContainer(), getStyleSet().getStyle( ROLE_BAR_STYLE, IAreaStyle.class ) );
  }

  private CSVBarLayerData getDataContainer( )
  {
    final CSVBarLayerData data = new CSVBarLayerData();

    final URL url = ChartFactoryUtilities.createURLQuietly( getContext(), getParameterContainer().getParameterValue( "url", getId() ) ); //$NON-NLS-1$
    data.setInputURL( url );

    return data;
  }
}