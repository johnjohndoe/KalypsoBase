package de.openali.odysseus.chart.ext.base.axisrenderer.provider;

import de.openali.odysseus.chart.ext.base.axisrenderer.GenericNumberTickCalculator;
import de.openali.odysseus.chart.ext.base.axisrenderer.ILabelCreator;
import de.openali.odysseus.chart.ext.base.axisrenderer.ITickCalculator;
import de.openali.odysseus.chart.ext.base.axisrenderer.NumberLabelCreator;

public class GenericNumberAxisRendererProvider extends AbstractGenericAxisRendererProvider
{

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.provider.AbstractGenericAxisRendererProvider#getLabelCreator()
   */
  @Override
  public ILabelCreator getLabelCreator( )
  {
    final String tickLabelFormater = getParameterContainer().getParameterValue( "tick_label_formater", "%s" );

    return new NumberLabelCreator( tickLabelFormater );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.provider.AbstractGenericAxisRendererProvider#getTickCalculator()
   */
  @Override
  public ITickCalculator getTickCalculator( )
  {
    return new GenericNumberTickCalculator();
  }
}
