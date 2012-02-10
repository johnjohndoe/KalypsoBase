package de.openali.odysseus.chart.ext.base.axis;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.BooleanLabelCreator;
import de.openali.odysseus.chart.ext.base.axisrenderer.ExtendedAxisRenderer;
import de.openali.odysseus.chart.ext.base.axisrenderer.ITickCalculator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

public class BooleanAxis extends AbstractAxis
{
  /**
   * TODO: TICK_CALCULATOR is used by BooleanAxisRendererProvider and BooleanAxis. Why?
   */
  public static final ITickCalculator TICK_CALCULATOR = new ITickCalculator()
  {
    @Override
    public Number[] calcTicks( final GC gc, final IAxis ax, final Number minDisplayInterval, final Point tickLabelSize )
    {
      return new Number[] { 0, 1 };
    }
  };

  public BooleanAxis( final String id, final POSITION pos )
  {
    super( id, pos, Number.class, new ExtendedAxisRenderer( id + "_RENDERER", pos, new BooleanLabelCreator(), TICK_CALCULATOR, new AxisRendererConfig() ) );//$NON-NLS-1$ 
  }

  public BooleanAxis( final String id, final POSITION pos, final AxisRendererConfig config )
  {
    super( id, pos, Number.class, new ExtendedAxisRenderer( id + "_RENDERER", pos, new BooleanLabelCreator(), TICK_CALCULATOR, config ) );//$NON-NLS-1$ 
  }

  public BooleanAxis( final String id, final POSITION pos, final Class< ? > clazz )
  {
    super( id, pos, clazz, new ExtendedAxisRenderer( id + "_RENDERER", pos, new BooleanLabelCreator(), TICK_CALCULATOR, new AxisRendererConfig() ) ); //$NON-NLS-1$ 
  }

  public BooleanAxis( final String id, final POSITION pos, final Class< ? > clazz, final IAxisRenderer renderer )
  {
    super( id, pos, clazz, renderer );
  }

  private Number normalizedToNumeric( final double value )
  {
    final IDataRange<Number> dataRange = getNumericRange();

    if( dataRange.getMax() == null || dataRange.getMin() == null )
      return Double.NaN;
    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    final double logical = value * r + dataRange.getMin().doubleValue();

    return logical;
  }

  /**
   * Uses the widgets' complete extension to calculate the screen value in correspondence to a normalized value
   * 
   * @see de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent#normalizedToScreen(double)
   */
  @Override
  public int normalizedToScreen( final double normValue )
  {
    final int range = getScreenHeight();
    return (int) (range * (isInverted() ? 1 - normValue : normValue));
  }

  public double numericToNormalized( final Number value )
  {
    final IDataRange<Number> dataRange = getNumericRange();
    if( dataRange.getMax() == null || dataRange.getMin() == null )
      return Double.NaN;
    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();
    final double norm = (value.doubleValue() - dataRange.getMin().doubleValue()) / r;
    return norm;
  }

  @Override
  public Integer numericToScreen( final Number value )
  {
    return normalizedToScreen( numericToNormalized( value ) );
  }

  /**
   * Uses the widgets' complete extension to allocates the normalized value in correspondence to a screen value
   * 
   * @see de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent#screenToNormalized(int)
   */
  @Override
  public double screenToNormalized( final int screenValue )
  {
    final int range = getScreenHeight();
    if( range == 0 )
      return 0;
    final double normValue = (double) screenValue / range;
    return isInverted() ? 1 - normValue : normValue;
  }

  @Override
  public Number screenToNumeric( final int value )
  {
    return normalizedToNumeric( screenToNormalized( value ) );
  }
}
