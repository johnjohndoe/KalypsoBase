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

  // FIXME pos unused?!? please see Todo
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
      return null;
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

    final Number[] ticks = getRenderer().getTicks( this, null );
    if( ticks.length < 2 )
      return 0;
    final int tickdist = ticks[1].intValue() - ticks[0].intValue();
    if( value < ticks[0].intValue() )
      return (value - ticks[0].intValue()) / tickdist;
    if( value > ticks[ticks.length - 1].intValue() )
      return ticks[ticks.length - 1].intValue() + (value - ticks[0].intValue()) / tickdist;

    int minDist = Integer.MAX_VALUE;
    Number returnValue = 0;
    for( int i = 0; i < ticks.length; i++ )
    {
      final int dist = Math.abs( ticks[i].intValue() - value );
      if( dist < minDist )
      {
        minDist = dist;
        returnValue = i;
      }
    }
    return returnValue;

  }
}
