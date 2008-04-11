package de.openali.odysseus.chart.factory.provider;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.openali.odysseus.chart.factory.config.StyleFactory;
import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chartconfig.x010.LayerType;
import de.openali.odysseus.chartconfig.x010.RoleReferencingType;

public abstract class AbstractLayerProvider implements ILayerProvider
{
  private LayerType m_lt;

  private IChartModel m_model;

  private IParameterContainer m_pc;

  private URL m_context;

  private Map<String, IStyle> m_styleMap;

  private HashMap<String, IMapper> m_mapperMap;

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#init(org.kalypso.swtchart.chart.ChartView,
   *      org.ksp.chart.configuration.LayerType)
   */
  public void init( final IChartModel model, final LayerType lt, final URL context )
  {
    m_lt = lt;
    m_model = model;
    m_pc = ChartFactoryUtilities.createXmlbeansParameterContainer( m_lt.getId(), m_lt.getProvider() );
    m_context = context;

    prepareStyles();
    prepareMappers();

  }

  /**
   * saves all referenced Mappers in a map; if the mapper is not found, null is saved
   */
  private void prepareMappers( )
  {
    m_mapperMap = new HashMap<String, IMapper>();
    RoleReferencingType[] mapperRefArray = getLayerType().getMappers().getMapperRefArray();
    for( RoleReferencingType rrt : mapperRefArray )
    {
      String id = rrt.getRef();
      String role = rrt.getRole();
      IMapper< ? , ? > mapper = getChartModel().getMapperRegistry().getMapper( id );
      m_mapperMap.put( role, mapper );
    }
  }

  /**
   * saves all referenced styles in a map; if the mapper is not found, null is saved
   */
  private void prepareStyles( )
  {
    m_styleMap = StyleFactory.createStyleMap( getLayerType().getStyles(), getContext() );
  }

  /**
   * default behaviour: return original xml type; implement, if changes shall be saved
   * 
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getLayerType()
   */
  public LayerType getXMLType( IChartLayer layer )
  {
    LayerType lt = (LayerType) m_lt.copy();
    lt.setVisible( layer.isVisible() );
    return lt;
  }

  public IChartModel getChartModel( )
  {
    return m_model;
  }

  public IParameterContainer getParameterContainer( )
  {
    return m_pc;
  }

  private LayerType getLayerType( )
  {
    return m_lt;
  }

  protected URL getContext( )
  {
    return m_context;
  }

  protected String getId( )
  {
    return m_lt.getId();
  }

  protected String getDomainAxisId( )
  {
    return getLayerType().getMappers().getDomainAxisRef().getRef();
  }

  protected String getTargetAxisId( )
  {
    return getLayerType().getMappers().getTargetAxisRef().getRef();
  }

  /**
   * Helper method to get styles referenced by a role in xml config; if no style is defined for a role, a default style
   * is returned
   * 
   * @param role
   *            role with which the concrete style is identifiable in the xml layer element (e.g. "line")
   * @param clazz
   *            class of style (e.g. ILineStyle.class)
   */
  protected <T extends IStyle> T getStyle( String role, Class<T> clazz )
  {
    IStyle style = m_styleMap.get( role );
    if( style != null && clazz.isAssignableFrom( style.getClass() ) )
      return (T) style;
    else
    {
      Logger.logWarning( Logger.TOPIC_LOG_STYLE, "No style for role '" + role + "'. Needed " + clazz + ". Returning default style" );
      return StyleUtils.getDefaultStyle( clazz );
    }
  }

  /**
   * Helper method to get mappers referenced by a role in xml config; if the mapper can not be found, null is returned
   * 
   * @param role
   *            role with which the concrete mapper is identifiable in the xml layer element (e.g. "line")
   */
  protected IMapper getMapper( String role )
  {
    return m_mapperMap.get( role );
  }

}
