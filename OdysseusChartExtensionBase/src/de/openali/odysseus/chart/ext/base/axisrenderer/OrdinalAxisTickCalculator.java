package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.ext.base.data.IAxisContentProvider;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;

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

    final int intervallCount = end - start + 1;
    final int tickDist = m_fixedMinWidth < 1 ? Math.min( m_fixedMaxWidth, axis.getScreenHeight() / intervallCount - 1/* Pixel */) : m_fixedMaxWidth;

    final Number[] tickPos = new Number[m_axisContentProvider.size()];
    int pos = -tickDist * start;

    for( int i = 0; i < m_axisContentProvider.size(); i++ )
    {
      tickPos[i] = pos;
      pos += tickDist;
    }

    return tickPos;

  }

}