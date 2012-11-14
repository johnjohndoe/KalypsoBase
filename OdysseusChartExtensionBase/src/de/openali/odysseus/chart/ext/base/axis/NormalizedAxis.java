package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;

/**
 * @author kimwerner
 */
public class NormalizedAxis extends IntegerAxis
{
  public NormalizedAxis( final String id, final POSITION pos )
  {
    super( id, pos );
  }


  @Override
  public IDataRange<Double> getNumericRange( )
  {
    // use fixed normalized values
    // see IAxisConstants ALIGNMENT
    // 0.0 => alignment Top, 1.0=>alignment Bottom, 0,5 => Center
    return new DataRange<>( 0.0, 1.0);
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
    // do nothing, fixed Range <0,1>
  }

 
}
