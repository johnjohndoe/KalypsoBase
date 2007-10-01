package org.kalypso.chart.ext.base.data;

import java.util.Comparator;

import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.AbstractDataOperator;
import org.kalypso.chart.framework.model.data.impl.DataRange;

public class NumberDataOperator extends AbstractDataOperator<Number>
{

  public NumberDataOperator( Comparator<Number> comparator )
  {
    super( comparator );
  }

  public DataRange<Number> getContainingInterval( Number numVal, Number numIntervalWidth, Number numFixedPoint )
  {
    double min = numFixedPoint.doubleValue();
    double max = numVal.doubleValue();

    int dirFactor = 1;

    final Double doubleFixedPoint = numFixedPoint.doubleValue();
    final Double doubleVal = numVal.doubleValue();
    final Double doubleIntervalWidth = numIntervalWidth.doubleValue();

    if( doubleFixedPoint.compareTo( doubleVal ) > 0 )
    {
      dirFactor = -1;
    }
    for( double d = doubleFixedPoint; d > doubleVal; d += dirFactor * doubleIntervalWidth )
    {
      min = doubleFixedPoint + d;
    }
    max = min + dirFactor * doubleIntervalWidth;

    DataRange<Number> dataRange = null;
    try
    {
      dataRange = new DataRange<Number>( Math.min( min, max ), Math.max( min, max ) );
    }
    catch( final ZeroSizeDataRangeException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return dataRange;
  }

  public Double logicalToNumerical( Number logVal )
  {
    return logVal.doubleValue();
  }

  public Number numericalToLogical( Number numVal )
  {
    return new Double( numVal.doubleValue() );
  }

  public String logicalToString( Number value )
  {
    return "" + value.toString();
  }

  public Number stringToLogical( String value ) throws MalformedValueException
  {
    try
    {
      final double d = Double.parseDouble( value );
      return d;
    }
    catch( final NumberFormatException e )
    {
      throw new MalformedValueException( e );
    }
  }

  public String getFormatHint( )
  {
    return "[1-9]([0-9])*.([0-9])*[1-9]";

  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataOperator#getDefaultRange()
   */
  public IDataRange<Number> getDefaultRange( )
  {
    try
    {
      return new DataRange<Number>( new Double( 0 ), new Double( 1 ) );
    }
    catch( ZeroSizeDataRangeException e )
    {
      // should never occur (look at constructor values)
    }
    return null;
  }
}
