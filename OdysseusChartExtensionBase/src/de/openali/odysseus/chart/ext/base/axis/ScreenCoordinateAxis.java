package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.IScreenAxis;

/**
 * @author kimwerner
 * 
 * ist eher eine normalisierte Achse: 0 gibt den Startwert in pixeln der Achse, 1 den Endpunkt.
 * @deprecated use {@link AbstractAxis#logicalToScreen(Object)} instead
 */
@Deprecated
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
    return new DataRange<>( 0.0, 1.0 );
  }

  @Override
  public boolean isVisible( )
  {
    // always hidden
    return false;
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
        return new Integer(value).doubleValue();

      case NEGATIVE:
        return getScreenHeight() - new Integer(value).doubleValue();

      default:
        throw new IllegalArgumentException();
    }
  }

  @SuppressWarnings( "rawtypes" )
  @Override
  public void setNumericRange( final IDataRange range )
  {
    // do nothing, fixed Range
  }
}
