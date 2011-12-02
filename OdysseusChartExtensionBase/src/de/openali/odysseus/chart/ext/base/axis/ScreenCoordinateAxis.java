package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.IScreenAxis;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author kimwerner
 */
public class ScreenCoordinateAxis extends AbstractAxis implements IScreenAxis
{
  public ScreenCoordinateAxis( final String id, final POSITION pos )
  {
    super( id, pos, Double.class, null );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axis.AbstractAxis#isVisible()
   */
  @Override
  public boolean isVisible( )
  {
    // always hidden
    return false;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#getNumericRange()
   */
  @Override
  public IDataRange<Number> getNumericRange( )
  {
    return new DataRange<Number>( 0, 1 );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#numericToScreen(java.lang.Number)
   */
  @Override
  public Integer numericToScreen( final Number value )
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

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#setNumericRange(org.kalypso.chart.framework.model.data.IDataRange)
   */
  @Override
  public void setNumericRange( final IDataRange<Number> range )
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
    double myNormValue = normValue;
    final int range = getScreenHeight();
    if( ChartUtilities.isInverseScreenCoords( this ) )
      myNormValue = 1 - myNormValue;
    final int screenValue = (int) (range * myNormValue);
    return screenValue;
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
    if( ChartUtilities.isInverseScreenCoords( this ) )
      return 1 - normValue;

    return normValue;
  }
}
