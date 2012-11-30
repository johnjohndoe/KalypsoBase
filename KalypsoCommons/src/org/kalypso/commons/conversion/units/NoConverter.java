package org.kalypso.commons.conversion.units;

/**
 * Performs no conversion, return the values 'as is'.
 * 
 * @author schlienger
 */
public final class NoConverter implements IValueConverter
{
  private static NoConverter instance;

  private NoConverter( )
  {
    // no instanciation
  }

  public static NoConverter getInstance( )
  {
    if( instance == null )
      instance = new NoConverter();

    return instance;
  }

  /**
   * @see org.kalypso.commons.conversion.units.IValueConverter#convert(double)
   */
  @Override
  public double convert( final double value )
  {
    return value;
  }

  /**
   * @see org.kalypso.commons.conversion.units.IValueConverter#reverse(double)
   */
  @Override
  public double reverse( final double value )
  {
    return value;
  }
}
