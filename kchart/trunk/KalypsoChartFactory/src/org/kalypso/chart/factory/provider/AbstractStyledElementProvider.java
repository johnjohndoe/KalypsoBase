package org.kalypso.chart.factory.provider;

import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.util.ChartFactoryUtilities;
import org.ksp.chart.factory.StyleType;

public abstract class AbstractStyledElementProvider implements IStyledElementProvider
{

  private StyleType m_st;

  private IParameterContainer m_pc;

  public void init( StyleType st )
  {
    m_st = st;
    m_pc = ChartFactoryUtilities.createXmlbeansParameterContainer( m_st.getId(), m_st.getProvider() );
  }

  public StyleType getStyleType( )
  {
    return m_st;
  }

  public IParameterContainer getParameterContainer( )
  {
    return m_pc;
  }

}
