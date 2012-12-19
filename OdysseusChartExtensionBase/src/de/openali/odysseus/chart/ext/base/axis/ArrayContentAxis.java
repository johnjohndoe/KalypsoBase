package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.OrdinalAxisRenderer;
import de.openali.odysseus.chart.ext.base.data.IAxisContentProvider;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * @author kimwerner
 */

public class ArrayContentAxis extends AbstractAxis<Integer>
{
  @Override
  public Class<Integer> getDataClass( )
  {
    return Integer.class;
  }

  private IAxisContentProvider m_contentProvider = null;

  private int m_fixedWidth = -1;

  public ArrayContentAxis( final String id, final IAxisContentProvider contentProvider, final int fixedWidth )
  {
    this( id, POSITION.BOTTOM, contentProvider, fixedWidth );
  }

  public ArrayContentAxis( final String id, final POSITION position, final IAxisRenderer axisRenderer, final IAxisContentProvider contentProvider, final int fixedWidth )
  {
    super( id, position, axisRenderer, null );

    m_contentProvider = contentProvider;
    m_fixedWidth = fixedWidth;
  }

  public ArrayContentAxis( final String id, final POSITION position, final IAxisContentProvider contentProvider, final int fixedWidth )
  {
    this( id, position, new OrdinalAxisRenderer( id + "_axisRenderer", new AxisRendererConfig(), contentProvider ), contentProvider, fixedWidth ); //$NON-NLS-1$
  }

  @Override
  public int numericToScreen( final Double value )
  {
    final int start = getNumericRange().getMin().intValue();
    return getScreenOffset() + (value.intValue() - start) * m_fixedWidth;
  }
//FIXME: use API logicalToNumeric and refactor axis<INTEGER> to axis<Content>
  public Object numericToContent( final int index )
  {
    if( m_contentProvider == null )
      return null;
    return m_contentProvider.getContent( index );
  }

  @Override
  public Double screenToNumeric( final int value )
  {
    // Todo: zurzeit nur intervallRendered mit festem Intervall, und 1. Tick bei screen =0
    return Math.round( (value - getScreenOffset()) / m_fixedWidth ) + getNumericRange().getMin();
  }

  @Override
  public Double logicalToNumeric( Integer value )
  {
    return value.doubleValue();
  }

  @Override
  public Integer numericToLogical( Double value )
  {
    return value.intValue();
  }
}
