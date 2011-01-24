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

  public ArrayContentAxis( final String id, final POSITION pos, final AxisRendererConfig config )
  {
    this( id, pos, config, null );
  }

  public ArrayContentAxis( final String id, final POSITION pos, final AxisRendererConfig config, final IAxisContentProvider contentProvider )
  {// TODO more positions
    super( id, POSITION.BOTTOM, Integer.class, new OrdinalAxisRenderer( id, config, null, contentProvider ) );
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
    final Number[] ticks = getRenderer().getTicks( this, null );
    if( ticks.length <= value.intValue() || ticks.length < 1 )
      return Integer.MIN_VALUE;
    return ticks[value.intValue()].intValue();
  }

  public Object numericToContent( final int index )
  {
    final IAxisRenderer renderer = getRenderer();
    if( renderer instanceof OrdinalAxisRenderer )
    {
      return ((OrdinalAxisRenderer) renderer).getContent( index );
    }
    return null;
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
    int minDist = Integer.MAX_VALUE;
    Number returnValueMin = 0;
    final Number[] ticks = getRenderer().getTicks( this, null );
    // wenn die screenCoordinaten ausserhalb der Ticks sind, ist der index immer 0 oder length-1, egal wie weit die
    // screenCoordinate vom Tick weg ist.
    // um pannen zu können braucht man aber eine distanz
    if( value < ticks[0].intValue() )
      return -screenToNumeric( ticks[0].intValue() + ticks[0].intValue() - value ).intValue();
    if( value > ticks[ticks.length - 1].intValue() )
    {
      return ticks.length - 1 + ticks.length - 1 - (screenToNumeric( ticks[ticks.length - 1].intValue() - (value - ticks[ticks.length - 1].intValue()) ).intValue());
    }
    for( int i = 0; i < ticks.length; i++ )
    {
      final int dist = Math.abs( ticks[i].intValue() - value );
      if( dist < minDist )
      {
        minDist = dist;
        returnValueMin = i;
      }
    }
    return returnValueMin;

  }
}
