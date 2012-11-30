package de.openali.odysseus.chart.ext.base.axisrenderer.provider;

import de.openali.odysseus.chart.ext.base.axis.BooleanAxis;
import de.openali.odysseus.chart.ext.base.axisrenderer.BooleanLabelCreator;
import de.openali.odysseus.chart.ext.base.axisrenderer.ILabelCreator;
import de.openali.odysseus.chart.ext.base.axisrenderer.ITickCalculator;

public class BooleanAxisRendererProvider extends AbstractGenericAxisRendererProvider
{

  @Override
  public ILabelCreator getLabelCreator( )
  {
    return new BooleanLabelCreator();
  }

  @Override
  public ITickCalculator getTickCalculator( )
  {
    return BooleanAxis.TICK_CALCULATOR;
  }

}
