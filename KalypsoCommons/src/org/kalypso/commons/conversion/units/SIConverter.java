package org.kalypso.commons.conversion.units;

/**
 * SIConverter
 * 
 * @author schlienger
 */
public class SIConverter implements IValueConverter
{
  private final double m_factor;

  public SIConverter( final String orgUnit, final String destUnit )
  {
    final double orgf = factor( orgUnit );
    final double destf = factor( destUnit );

    m_factor = orgf / destf;
  }

  /**
   * @see org.kalypso.commons.conversion.units.IValueConverter#convert(double)
   */
  @Override
  public double convert( double value )
  {
    return value * m_factor;
  }

  /**
   * @see org.kalypso.commons.conversion.units.IValueConverter#reverse(double)
   */
  @Override
  public double reverse( double value )
  {
    return value / m_factor;
  }

  /**
   * Returns the factor associated to the given unit
   * <p>
   * Note to developers: complete the list!
   */
  private final static double factor( final String unit )
  {
    // BEWARE: 10^-x does not give the right value
    if( unit.equals( "m" ) ) //$NON-NLS-1$
      return 1;
    if( unit.equals( "dm" ) ) //$NON-NLS-1$
      return 0.1;
    if( unit.equals( "cm" ) ) //$NON-NLS-1$
      return 0.01;
    if( unit.equals( "mm" ) ) //$NON-NLS-1$
      return 0.001;

    if( unit.equals( "m³" ) ) //$NON-NLS-1$
      return 1;

    return 1;
  }
}
