package de.openali.odysseus.chart.factory.provider;

import java.net.URL;
import java.util.Map;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

public abstract class AbstractLayerProvider extends AbstractChartComponentProvider implements ILayerProvider
{
  private Map<String, String> m_mapperMap;

  private String m_domainAxisId;

  private String m_targetAxisId;

  private IStyleSet m_styleSet;

  /**
   * @see de.openali.odysseus.chart.factory.provider.ILayerProvider#init(de.openali.odysseus.chart.framework.model.IChartModel,
   *      java.lang.String, de.openali.odysseus.chart.factory.config.parameters.IParameterContainer, java.net.URL,
   *      java.lang.String, java.lang.String, java.util.Map, de.openali.odysseus.chart.framework.model.style.IStyleSet)
   */
  @Override
  public void init( final IChartModel model, final String id, final IParameterContainer parameters, final URL context, final String domainAxisId, final String targetAxisId, final Map<String, String> mapperMap, final IStyleSet styleSet )
  {
    super.init( model, id, parameters, context );

    m_domainAxisId = domainAxisId;
    m_targetAxisId = targetAxisId;
    m_mapperMap = mapperMap;
    m_styleSet = styleSet;
  }

  protected String getDomainAxisId( )
  {
    return m_domainAxisId;
  }

  protected String getTargetAxisId( )
  {
    return m_targetAxisId;
  }

  protected IStyleSet getStyleSet( )
  {
    return m_styleSet;
  }

  protected Map<String, String> getMapperMap( )
  {
    return m_mapperMap;
  }

}
