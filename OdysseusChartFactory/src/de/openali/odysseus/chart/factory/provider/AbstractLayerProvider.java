package de.openali.odysseus.chart.factory.provider;

import java.util.Map;

import de.openali.odysseus.chart.framework.model.impl.settings.CHART_DATA_LOADER_STRATEGY;
import de.openali.odysseus.chart.framework.model.impl.settings.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILayerProviderSource;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

public abstract class AbstractLayerProvider extends AbstractChartComponentProvider implements ILayerProvider
{

  private ILayerProviderSource m_source;

  /**
   * @see de.openali.odysseus.chart.factory.provider.ILayerProvider#init(de.openali.odysseus.chart.framework.model.IChartModel,
   *      java.lang.String, de.openali.odysseus.chart.factory.config.parameters.IParameterContainer, java.net.URL,
   *      java.lang.String, java.lang.String, java.util.Map, de.openali.odysseus.chart.framework.model.style.IStyleSet)
   */
  @Override
  public void init( final ILayerProviderSource source )
  {
    m_source = source;

    super.init( source.getModel(), source.getIdentifier(), source.getContainer(), source.getContext() );
  }

  @Override
  public String getDomainAxisId( )
  {
    return m_source.getDomainAxis();
  }

  @Override
  public String getTargetAxisId( )
  {
    return m_source.getTargetAxis();
  }

  protected IStyleSet getStyleSet( )
  {
    return m_source.getStyleSet();
  }

  protected Map<String, String> getMapperMap( )
  {
    return m_source.getMapperMap();
  }

  public CHART_DATA_LOADER_STRATEGY getDataLoaderStrategy( )
  {
    final IBasicChartSettings settings = getModel().getSettings();

    return settings.getDataLoaderStrategy();
  }
}
