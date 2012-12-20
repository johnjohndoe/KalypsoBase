package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;

/**
 * @author kimwerner
 */
public class ScreenCoordinateAxis extends IntegerAxis
{
  public ScreenCoordinateAxis( final String id, final POSITION pos )
  {
    super( id, pos );
  }


  @Override
  public IDataRange<Double> getNumericRange( )
  {
    // use screenCoordinates as DataRange
    final Integer screenMax = getScreenHeight();
    return new DataRange<>( 0.0, screenMax.doubleValue() );
  }

  @Override
  public boolean isVisible( )
  {
    // always hidden
    return false;
  }

 

  @Override
  public void setNumericRange( final IDataRange range )
  {
    // do nothing, fixed Range from screenCoordinates
  }

 
}
