package de.openali.odysseus.chart.factory.provider;

import java.net.URL;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;

public abstract class AbstractAxisProvider extends AbstractChartComponentProvider implements IAxisProvider
{
  private POSITION m_pos;

  private String[] m_valueArray;

  @Override
  public void init( final IChartModel model, final String id, final IParameterContainer parameters, final URL context, final POSITION pos, final String[] valueArray )
  {
    super.init( model, id, parameters, context );

    m_pos = pos;
    m_valueArray = valueArray;
  }

  protected POSITION getPosition( )
  {
    return m_pos;
  }

  protected String[] getValueArray( )
  {
    return m_valueArray;
  }
}