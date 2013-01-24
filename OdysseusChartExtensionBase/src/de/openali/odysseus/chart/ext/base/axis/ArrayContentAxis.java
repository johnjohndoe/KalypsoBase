package de.openali.odysseus.chart.ext.base.axis;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.OrdinalAxisRenderer;
import de.openali.odysseus.chart.ext.base.data.IAxisContentProvider;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * @author kimwerner
 */
public class ArrayContentAxis extends AbstractAxis
{

  private IAxisContentProvider m_contentProvider = null;

  private int m_fixedWidth = -1;

  public ArrayContentAxis( final String id, final IAxisContentProvider contentProvider, final int fixedWidth )
  {
    this( id, POSITION.BOTTOM, contentProvider, fixedWidth );

  }

  public ArrayContentAxis( final String id, POSITION position, final IAxisRenderer axisRenderer, final IAxisContentProvider contentProvider, final int fixedWidth )
  {
    super( id, position, Integer.class, axisRenderer );
    m_contentProvider = contentProvider;
    m_fixedWidth = fixedWidth;
  }

  public ArrayContentAxis( final String id, POSITION position, final IAxisContentProvider contentProvider, final int fixedWidth )
  {
    this( id, position, new OrdinalAxisRenderer( id + "_axisRenderer", new AxisRendererConfig(), contentProvider ), contentProvider, fixedWidth );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxis#normalizedToScreen(double)
   */
  @Override
  public int normalizedToScreen( final double d )
  {
    throw new NotImplementedException( "use numericToScreen instead" );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#numericToScreen(java.lang.Number)
   */
  @Override
  public Integer numericToScreen( final Number value )
  {
    final int start = getNumericRange().getMin().intValue();
    return (value.intValue() - start) * m_fixedWidth;
  }

  public Object numericToContent( final int index )
  {
    if( m_contentProvider == null )
      return null;
    return m_contentProvider.getContent( index );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxis#screenToNormalized(int)
   */
  @Override
  public double screenToNormalized( final int value )
  {
    throw new NotImplementedException( "use screenToNumeric instead" );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#screenToNumeric(int)
   */
  @Override
  public Number screenToNumeric( final int value )
  {
    // Todo: zurzeit nur intervallRendered mit festem Intervall, und 1. Tick bei screen =0
    return Math.round( value / m_fixedWidth ) + getNumericRange().getMin().intValue();
  }
}
