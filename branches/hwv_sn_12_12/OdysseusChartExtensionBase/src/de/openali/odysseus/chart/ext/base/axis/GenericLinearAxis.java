package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.ExtendedAxisRenderer;
import de.openali.odysseus.chart.ext.base.axisrenderer.GenericNumberTickCalculator;
import de.openali.odysseus.chart.ext.base.axisrenderer.NumberLabelCreator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * logical and numerical range are identical;
 * 
 * @author burtscher Concrete IAxis implementation - to be used for numeric data
 */
public class GenericLinearAxis extends AbstractAxis
{

  public GenericLinearAxis( final String id, final POSITION pos )
  {
    super( id, pos, Number.class, new ExtendedAxisRenderer( id + "_RENDERER", pos, new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), new AxisRendererConfig() ) );//$NON-NLS-1$ //$NON-NLS-2$
  }

  public GenericLinearAxis( final String id, final POSITION pos, final AxisRendererConfig config )
  {
    super( id, pos, Number.class, new ExtendedAxisRenderer( id + "_RENDERER", pos, new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), config ) );//$NON-NLS-1$ //$NON-NLS-2$
  }

  public GenericLinearAxis( final String id, final POSITION pos, final Class< ? > clazz )
  {
    super( id, pos, clazz, new ExtendedAxisRenderer( id + "_RENDERER", pos, new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), new AxisRendererConfig() ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public GenericLinearAxis( final String id, final POSITION pos, final Class< ? > clazz, final IAxisRenderer renderer )
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

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#numericToScreen(java.lang.Number)
   */
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

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#screenToNumeric(int)
   */
  @Override
  public Number screenToNumeric( final int value )
  {
    return normalizedToNumeric( screenToNormalized( value ) );
  }
}
