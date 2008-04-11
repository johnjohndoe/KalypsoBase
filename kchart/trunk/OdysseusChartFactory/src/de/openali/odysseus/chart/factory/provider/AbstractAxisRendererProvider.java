package de.openali.odysseus.chart.factory.provider;

import java.net.URL;
import java.util.Map;

import de.openali.odysseus.chart.factory.config.StyleFactory;
import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chartconfig.x010.AxisRendererType;

public abstract class AbstractAxisRendererProvider implements IAxisRendererProvider
{

  private AxisRendererType m_at;

  private IParameterContainer m_pc;

  private URL m_context;

  private Map<String, IStyle> m_styleMap;

  public void init( AxisRendererType at, URL context )
  {
    m_at = at;
    m_pc = ChartFactoryUtilities.createXmlbeansParameterContainer( m_at.getId(), m_at.getProvider() );
    m_context = context;
    prepareStyles();
  }

  public IParameterContainer getParameterContainer( )
  {
    return m_pc;
  }

  /**
   * * default behaviour: return original xml type; implement, if changes shall be saved
   * 
   * @see org.kalypso.chart.factory.provider.IAxisRendererProvider#getXMLType(org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer)
   */
  public AxisRendererType getXMLType( IAxisRenderer axisRenderer )
  {
    return m_at;
  }

  protected AxisRendererType getAxisRendererType( )
  {
    return m_at;
  }

  protected String getId( )
  {
    return m_at.getId();
  }

  protected URL getContext( )
  {
    return m_context;
  }

  /**
   * saves all referenced styles in a map; if the mapper is not found, null is saved
   */
  private void prepareStyles( )
  {
    m_styleMap = StyleFactory.createStyleMap( getAxisRendererType().getStyles(), getContext() );
  }

  /**
   * Helper method to get styles referenced by a role in xml config; if no style is defined for a role, a default style
   * is returned
   */
  @SuppressWarnings("unchecked")
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
}
