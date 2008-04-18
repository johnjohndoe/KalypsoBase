package de.openali.odysseus.chart.factory.provider;

import java.net.URL;
import java.util.Map;

import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.framework.model.IChartModel;
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
  public void init( IChartModel model, String id, IParameterContainer parameters, URL context, String domainAxisId, String targetAxisId, Map<String, String> mapperMap, IStyleSet styleSet )
  {
    super.init( model, id, parameters, context );
    m_domainAxisId = domainAxisId;
    m_targetAxisId = targetAxisId;
    m_mapperMap = mapperMap;
    m_styleSet = styleSet;
  }

// /**
// * default behaviour: return original xml type; implement, if changes shall be saved
// *
// * @see org.kalypso.chart.factory.provider.ILayerProvider#getLayerType()
// */
// public LayerType getXMLType( IChartLayer layer )
// {
// LayerType lt = (LayerType) m_lt.copy();
// lt.setVisible( layer.isVisible() );
// return lt;
// }

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
