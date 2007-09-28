package org.kalypso.chart.factory.provider;

import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.util.ChartFactoryUtilities;
import org.ksp.chart.factory.MapperType;

public abstract class AbstractMapperProvider implements IMapperProvider
{

  private MapperType m_mt;

  private IParameterContainer m_pc;

  public void init( MapperType mt )
  {
    m_mt = mt;
    m_pc = ChartFactoryUtilities.createXmlbeansParameterContainer( m_mt.getId(), m_mt.getProvider() );
  }

  public MapperType getMapperType( )
  {
    return m_mt;
  }

  public IParameterContainer getParameterContainer( )
  {
    return m_pc;
  }

}
