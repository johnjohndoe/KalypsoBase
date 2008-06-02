package org.kalypso.chart.factory.provider;

import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.util.ChartFactoryUtilities;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;
import org.ksp.chart.factory.AxisRendererType;

public abstract class AbstractAxisRendererProvider implements IAxisRendererProvider
{

  private AxisRendererType m_at;

  private IParameterContainer m_pc;

  public void init( AxisRendererType at )
  {
    m_at = at;
    m_pc = ChartFactoryUtilities.createXmlbeansParameterContainer( m_at.getId(), m_at.getProvider() );
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

}
