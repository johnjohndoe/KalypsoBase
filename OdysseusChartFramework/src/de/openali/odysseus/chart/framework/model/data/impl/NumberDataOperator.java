package de.openali.odysseus.chart.framework.model.data.impl;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Comparator;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.IDataRange;

public class NumberDataOperator extends AbstractDataOperator<Number>
{

  public NumberDataOperator( final Comparator<Number> comparator )
  {
    super( comparator );
  }

  public IDataRange<Number> getContainingInterval( final Number numVal, final Number numIntervalWidth, final Number numFixedPoint )
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

    IDataRange<Number> dataRange = new ComparableDataRange<Number>( new Number[] { min, max } );
    return dataRange;
  }

  public Double logicalToNumeric( final Number logVal )
  {
    if( logVal == null )
    {
      return null;
    }
    return logVal.doubleValue();
  }

  public Number numericToLogical( final Number numVal )
  {
    return new Double( numVal.doubleValue() );
  }

  public String logicalToString( final Number value )
  {
    return "" + value.toString();
  }

  public Number stringToLogical( final String value ) throws MalformedValueException
  {
    if( value == null )
    {
      return null;
    }
    else if( "".equals( value.trim() ) )
    {
      return null;
    }
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

  public Format getFormat( final IDataRange<Number> range )
  {
    Number min = range.getMin();
    if( Double.valueOf( min.doubleValue() ).isInfinite() )
    {
      min = 0.0;
    }

    Number max = range.getMax();
    if( Double.valueOf( max.doubleValue() ).isInfinite() )
    {
      max = 0.0;
    }

    final NumberFormat nf = new DecimalFormat();
    // Anzahl gültiger stellen
    final int validDigits = 3;
    // Fraction digits
    int fd = 0;
    // Integer digits
    int id = 1;

    // Minuszeichen einplanen
    if( max.doubleValue() < 0 || min.doubleValue() < 0 )
    {
      id++;
    }

    // Vorkommastellen ausrechnen
    double tmpmax = Math.max( Math.abs( max.doubleValue() ), Math.abs( min.doubleValue() ) );
    if( tmpmax >= 1 && !Double.isInfinite( tmpmax ) )
    {
      while( tmpmax >= 1 )
      {
        tmpmax /= 10;
        id++;
      }
    }

    // Differenz bilden und sicherstellen, dass sie positiv ist
    double diff = Math.abs( max.doubleValue() - min.doubleValue() );
    // Bereichs-10er-potenz ausmachen
    int pow = 0;
    if( diff >= 1 && !Double.isInfinite( diff ) )
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
    nf.setGroupingUsed( false );

    return nf;
  }
}
