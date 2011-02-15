package de.openali.odysseus.chart.factory.provider;

import java.net.URL;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

public abstract class AbstractAxisRendererProvider extends AbstractChartComponentProvider implements IAxisRendererProvider
{

  private IStyleSet m_styleSet;

  @Override
  public void init( final IChartModel model, final String id, final IParameterContainer parameters, final URL context, final IStyleSet styleSet )
  {
    super.init( model, id, parameters, context );
    m_styleSet = styleSet;
  }

  protected IStyleSet getStyleSet( )
  {
    return m_styleSet;
  }

// /**
// * * default behaviour: return original xml type; implement, if changes shall be saved
// *
// * @see
// org.kalypso.chart.factory.provider.IAxisRendererProvider#getXMLType(org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer)
// */
// public AxisRendererType getXMLType( IAxisRenderer axisRenderer )
// {
// return m_at;
// }
//
// protected AxisRendererType getAxisRendererType( )
// {
// return m_at;
// }

// /**
// * saves all referenced styles in a map; if the mapper is not found, null is saved
// */
// private void prepareStyles( )
// {
// m_styleMap = StyleFactory.createStyleMap( getAxisRendererType().getStyles(), getContext() );
// }

// /**
// * Helper method to get styles referenced by a role in xml config; if no style is defined for a role, a default style
// * is returned
// */
// @SuppressWarnings("unchecked")
// protected <T extends IStyle> T getStyle( String role, Class<T> clazz )
// {
// IStyle style = m_styleMap.get( role );
// if( style != null && clazz.isAssignableFrom( style.getClass() ) )
// return (T) style;
// else
// {
// Logger.logWarning( Logger.TOPIC_LOG_STYLE, "No style for role '" + role + "'. Needed " + clazz + ". Returning default
// style" );
// return StyleUtils.getDefaultStyle( clazz );
// }
// }
}
