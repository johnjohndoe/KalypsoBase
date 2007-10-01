package org.kalypso.chart.factory.provider;

import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.util.ChartFactoryUtilities;
import org.kalypso.chart.framework.model.mapper.IMapper;
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

  public IParameterContainer getParameterContainer( )
  {
    return m_pc;
  }

  public MapperType getMapperType( )
  {
    return m_mt;
  }

  /**
   * * default behaviour: return original xml type; implement, if changes shall be saved
   * 
   * @see org.kalypso.chart.factory.provider.IMapperProvider#getXMLType(org.kalypso.chart.framework.model.mapper.IMapper)
   */
  public MapperType getXMLType( IMapper mapper )
  {
    return m_mt;
  }

}
