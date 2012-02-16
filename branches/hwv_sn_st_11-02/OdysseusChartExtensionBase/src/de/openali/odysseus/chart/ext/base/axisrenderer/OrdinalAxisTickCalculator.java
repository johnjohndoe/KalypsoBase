package de.openali.odysseus.chart.ext.base.axisrenderer;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.ext.base.data.IAxisContentProvider;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

public class OrdinalAxisTickCalculator implements ITickCalculator
{
  private final IAxisContentProvider m_axisContentProvider;

  private int m_fixedMinWidth;

  private int m_fixedMaxWidth;

  public OrdinalAxisTickCalculator( final IAxisContentProvider contentProvider, final int minLabelWidth, final int maxLabelWidth )
  {
    m_axisContentProvider = contentProvider;
    m_fixedMaxWidth = maxLabelWidth;
    m_fixedMinWidth = minLabelWidth;
  }

  /**
   * Calculates the ticks shown for the given Axis *
   * 
   * @param minDisplayInterval
   *          interval division should stop when intervals become smaller than this value
   */
  @Override
  public Number[] calcTicks( final GC gc, final IAxis axis, final Number minDisplayInterval, final Point ticklabelSize )
  {
    if( axis.getNumericRange().getMin() == null || axis.getNumericRange().getMax() == null )
      return new Number[] {};

    final int start = axis.getNumericRange().getMin().intValue();
    final int end = axis.getNumericRange().getMax().intValue();
    final Number[] tickPos = new Number[end - start + 1];
    for( int i = 0; i < tickPos.length; i++ )
    {
      tickPos[i] = start + i;
    }
    return tickPos;


  }

}