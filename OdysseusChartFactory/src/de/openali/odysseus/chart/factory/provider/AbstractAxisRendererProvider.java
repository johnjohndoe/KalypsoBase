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
}
