package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * logical and numerical range are identical;
 * 
 * @author burtscher Concrete IAxis implementation - to be used for numeric data
 */
public class ScreenCoordinateAxis extends AbstractAxis
{

  /**
   * @see de.openali.odysseus.chart.ext.base.axis.AbstractAxis#setScreenHeight(int)
   */
  @Override
  public void setScreenHeight( final int height )
  {
    // TODO Auto-generated method stub
    super.setScreenHeight( height );
  }

  private final IDataRange<Number> m_numericRange = new DataRange<Number>( null, null );

  public ScreenCoordinateAxis( final String id, final POSITION pos )
  {
    super( id, pos, Integer.class, null );
  }

  public double numericToNormalized( final Number value )
  {
    final IDataRange<Number> dataRange = getNumericRange();
    if( dataRange.getMax() == null || dataRange.getMin() == null )
      return Double.NaN;
    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();
    final double norm = (value.doubleValue() - dataRange.getMin().doubleValue()) / r;
    return norm;
  }

  private Number normalizedToNumeric( final double value )
  {
    final IDataRange<Number> dataRange = getNumericRange();

    if( dataRange.getMax() == null || dataRange.getMin() == null )
      return Double.NaN;
    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    final double logical = value * r + dataRange.getMin().doubleValue();

    return logical;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#getNumericRange()
   */
  @Override
  public IDataRange<Number> getNumericRange( )
  {
    return new DataRange<Number>( 0, 1 );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#numericToScreen(java.lang.Number)
   */
  @Override
  public Integer numericToScreen( final Number value )
  {
    return normalizedToScreen( numericToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#screenToNumeric(int)
   */
  @Override
  public Number screenToNumeric( final int value )
  {
    return normalizedToNumeric( screenToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#setNumericRange(org.kalypso.chart.framework.model.data.IDataRange)
   */
  @Override
  public void setNumericRange( final IDataRange<Number> range )
  {
   //do nothing, fixed Range
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axis.AbstractAxis#setLogicalRange(org.kalypso.chart.framework.model.data.IDataRange)
   */
  @Override
  public void setLogicalRange( final IDataRange<Number> dataRange )
  {
    // Nix machen! Wir wollen auf logical Range verzichten
    assert (false);
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axis.AbstractAxis#getLogicalRange()
   */
  @Override
  public IDataRange<Number> getLogicalRange( )
  {
    assert (false);
    return null;
  }

  /**
   * Uses the widgets' complete extension to calculate the screen value in correspondence to a normalized value
   * 
   * @see de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent#normalizedToScreen(double)
   */
  @Override
  public int normalizedToScreen( final double normValue )
  {
    double myNormValue = normValue;
    final int range = getScreenHeight();
    if( ChartUtilities.isInverseScreenCoords( this ) )
      myNormValue = 1 - myNormValue;
    final int screenValue = (int) (range * myNormValue);
    return screenValue;
  }

  /**
   * Uses the widgets' complete extension to allocates the normalized value in correspondence to a screen value
   * 
   * @see de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent#screenToNormalized(int)
   */
  @Override
  public double screenToNormalized( final int screenValue )
  {
    final int range = getScreenHeight();
    if( range == 0 )
      return 0;
    final double normValue = (double) screenValue / range;
    if( ChartUtilities.isInverseScreenCoords( this ) )
      return 1 - normValue;

    return normValue;
  }
}
