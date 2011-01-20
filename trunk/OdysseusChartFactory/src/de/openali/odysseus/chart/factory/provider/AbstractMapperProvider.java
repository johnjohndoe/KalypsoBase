package de.openali.odysseus.chart.factory.provider;

import java.net.URL;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;

public abstract class AbstractMapperProvider extends AbstractChartComponentProvider implements IMapperProvider
{

  @Override
  public void init( final IChartModel model, final String id, final IParameterContainer parameters, final URL context )
  {
    super.init( model, id, parameters, context );
  }

// /**
// * * default behaviour: return original xml type; implement, if changes shall be saved
// *
// * @see
// org.kalypso.chart.factory.provider.IMapperProvider#getXMLType(org.kalypso.chart.framework.model.mapper.IMapper)
// */
// @SuppressWarnings("unchecked")
// public MapperType getXMLType( IMapper mapper )
// {
// return m_mt;
// }

}
