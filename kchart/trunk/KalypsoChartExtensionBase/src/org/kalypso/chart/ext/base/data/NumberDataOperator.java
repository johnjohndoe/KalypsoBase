package org.kalypso.chart.ext.base.data;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Comparator;

import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.AbstractDataOperator;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
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
    for( double d = doubleFixedPoint; d < doubleVal; d += dirFactor * doubleIntervalWidth )
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
    if( logVal == null )
      return null;
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
    if( value == null )
      return null;
    else if( "".equals( value.trim() ) )
      return null;
    else
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
    return new ComparableDataRange<Number>( new Double[] { 0.0, 1.0 } );
  }

  public Format getFormat( IDataRange<Number> range )
  {
    Number min = range.getMin();
    Number max = range.getMax();
    // Differenz bilden und sicherstellen, dass sie positiv ist
    Double diff = Math.abs( max.doubleValue() - min.doubleValue() );

    NumberFormat nf = new DecimalFormat();
    // Anzahl gültiger stellen
    int validDigits = 3;
    // Fraction digits
    int fd = 0;
    // Integer digits
    int id = 1;

    // Minuszeichen einplanen
    if( max.doubleValue() < 0 || min.doubleValue() < 0 )
      id++;

    // Vorkommastellen ausrechnen
    double tmpmax = Math.abs( max.doubleValue() );
    if( tmpmax >= 1 )
    {
      while( tmpmax >= 1 )
      {
        tmpmax /= 10;
        id++;
      }

    }

    // Bereichs-10er-potenz ausmachen
    int pow = 0;
    if( diff >= 1 )
    {
      while( diff >= 1 )
      {
        diff /= 10;
        pow++;
      }
    }
    else if( diff != 0 )
    {
      while( diff <= 0.1 )
      {
        diff *= 10;
        pow--;
      }
    }
    //
    if( pow >= 0 )
    {
      fd = pow > validDigits ? 0 : Math.abs( validDigits - pow );
    }
    else
    {
      fd = Math.abs( pow ) + validDigits;
    }

    nf.setMaximumIntegerDigits( id );
    nf.setMinimumIntegerDigits( 1 );
    nf.setMaximumFractionDigits( fd );
    nf.setMinimumFractionDigits( fd );

    return nf;
  }
}
