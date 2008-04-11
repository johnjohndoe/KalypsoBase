package org.kalypso.chart.ext.base.axisrenderer.provider;

import org.kalypso.chart.ext.base.axisrenderer.GenericNumberTickCalculator;
import org.kalypso.chart.ext.base.axisrenderer.ILabelCreator;
import org.kalypso.chart.ext.base.axisrenderer.ITickCalculator;
import org.kalypso.chart.ext.base.axisrenderer.NumberLabelCreator;

public class GenericNumberAxisRendererProvider extends AbstractGenericAxisRendererProvider
{

  /**
   * @see org.kalypso.chart.ext.base.axisrenderer.provider.AbstractGenericAxisRendererProvider#getLabelCreator()
   */
  @Override
  public ILabelCreator getLabelCreator( )
  {
    final String tick_label_formater = getParameterContainer().getParameterValue( "tick_label_formater", "%s" );
    return new NumberLabelCreator( tick_label_formater );
  }

  /**
   * @see org.kalypso.chart.ext.base.axisrenderer.provider.AbstractGenericAxisRendererProvider#getTickCalculator()
   */
  @Override
  public ITickCalculator getTickCalculator( )
  {
    return new GenericNumberTickCalculator();
  }
}
