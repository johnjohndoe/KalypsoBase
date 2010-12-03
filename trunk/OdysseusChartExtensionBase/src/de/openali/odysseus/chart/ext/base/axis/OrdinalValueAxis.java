package de.openali.odysseus.chart.ext.base.axis;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.OrdinalAxisRenderer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;

/**
 * @author kimwerner
 */
public class OrdinalValueAxis extends AbstractAxis
{

  /**
   * @see de.openali.odysseus.chart.ext.base.axis.AbstractAxis#getNumericRange()
   */
  @Override
  public IDataRange<Number> getNumericRange( )
  {

    final Object labels = getRenderer().getData( OrdinalAxisRenderer.STRING_ARRAY );
    if( labels == null )
      return null;
    final int len = ((String[]) labels).length;
    return new DataRange<Number>( 0, len == 0 ? 0 : len - 1 );

  }

  public OrdinalValueAxis( final String id, final POSITION pos, final AxisRendererConfig config )
  {
    super( id, pos, Integer.class, new OrdinalAxisRenderer( id, config ) );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#numericToScreen(java.lang.Number)
   */
  @Override
  public Integer numericToScreen( final Number value )
  {
    final Number[] ticks = getRenderer().getTicks( this, null );
    if( ticks.length < value.intValue() || ticks.length < 1 )
      return 0;
    return ticks[value.intValue()].intValue();
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#screenToNumeric(int)
   */
  @Override
  public Number screenToNumeric( final int value )
  {
    int minDist = Integer.MAX_VALUE;
    Number returnValue = 0;
    for( final Number tick : getRenderer().getTicks( this, null ) )
    {
      final int dist = Math.abs( tick.intValue() - value );
      if( dist < minDist )
      {
        minDist = dist;
        returnValue = tick;
      }
    }
    return returnValue;
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
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxis#screenToNormalized(int)
   */
  @Override
  public double screenToNormalized( final int value )
  {
    throw new NotImplementedException( "use screenToNumeric instead" );
  }

}
