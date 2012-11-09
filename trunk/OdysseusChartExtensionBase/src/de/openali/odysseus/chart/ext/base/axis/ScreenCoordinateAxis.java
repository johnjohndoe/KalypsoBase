package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.IScreenAxis;

/**
 * DIESE AXIS MACHT SINN UND WIRD IMMER NOCH BENUTZT!
 * 
 * @author kimwerner
 */
public class ScreenCoordinateAxis extends AbstractAxis<Integer> implements IScreenAxis<Integer>
{
  public ScreenCoordinateAxis( final String id, final POSITION pos )
  {
    super( id, pos, null, null );
  }

  @Override
  public Class<Integer> getDataClass( )
  {
    return Integer.class;
  }

  @Override
  public IDataRange<Double> getNumericRange( )
  {
    // FIXME: probably nonsense
    return new DataRange<>( 0.0, 1.0 );
  }

  @Override
  public boolean isVisible( )
  {
    // always hidden
    return false;
  }

  @Override
  public Double logicalToNumeric( final Integer value )
  {
    return value.doubleValue();
  }

  @Override
  public Integer numericToLogical( final Double value )
  {
    return value.intValue();
  }

  @Override
  public int numericToScreen( final Double value )
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
  public Double screenToNumeric( final int value )
  {
    final DIRECTION direction = getDirection();
    switch( direction )
    {
      case POSITIVE:
        return new Integer( value ).doubleValue();

      case NEGATIVE:
        return getScreenHeight() - new Integer( value ).doubleValue();

      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public void setNumericRange( final IDataRange range )
  {
    // do nothing, fixed Range
  }

  @Override
  public Integer xmlStringToLogical( final String value ) throws MalformedValueException
  {
    try
    {
      return Integer.parseInt( value );
    }
    catch( final NumberFormatException e )
    {
      e.printStackTrace();
      throw new MalformedValueException( e );
    }
  }
}
