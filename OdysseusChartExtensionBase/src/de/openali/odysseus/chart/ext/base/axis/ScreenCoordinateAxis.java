package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
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
    final int screenHeight = getScreenHeight();

    return new DataRange<Number>( 0, screenHeight );
  }

  @Override
  public int numericToScreen( final Number value )
  {
    final DIRECTION direction = getDirection();
    switch( direction )
    {
      case POSITIVE:
        return value.intValue();

      case NEGATIVE:
        return getScreenHeight() - value.intValue();

      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public Number screenToNumeric( final int value )
  {
    final DIRECTION direction = getDirection();
    switch( direction )
    {
      case POSITIVE:
        return value;

      case NEGATIVE:
        return getScreenHeight() - value;

      default:
        throw new IllegalArgumentException();
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void setNumericRange( final IDataRange range )
  {
    // do nothing, fixed Range
  }
}
