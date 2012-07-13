package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.IScreenAxis;

/**
 * @author kimwerner
 */
public class ScreenCoordinateAxis extends AbstractAxis implements IScreenAxis
{
  public ScreenCoordinateAxis( final String id, final POSITION pos )
  {
    super( id, pos, Double.class, null );
  }

  @Override
  public boolean isVisible( )
  {
    // always hidden
    return false;
  }

  @Override
  public IDataRange<Number> getNumericRange( )
  {
    return new DataRange<Number>( 0, 1 );
  }

  @Override
  public int numericToScreen( final Number value )
  {
    return normalizedToScreen( value.doubleValue() );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#screenToNumeric(int)
   */
  @Override
  public Number screenToNumeric( final int value )
  {
    return screenToNormalized( value );
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void setNumericRange( final IDataRange range )
  {
    // do nothing, fixed Range
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
    return (int) (range * normValue);
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
// final double normValue = (double) screenValue / range;
// if( ChartUtilities.isInverseScreenCoords( this ) )
// return 1 - normValue;
//
// return normValue;
    return (double) screenValue / range;
  }
}
