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
public class GenericLinearAxis extends AbstractAxis
{

  

  private IDataRange<Number> m_numericRange;

  private int m_height = 1;

  public GenericLinearAxis( final String id, final POSITION pos, Class< ? > clazz )
  {
    super( id, pos, clazz );
    // set initial range, so we can prevent NPEs
    setNumericRange( new DataRange<Number>( 0, 1 ) );
  }

  public double numericToNormalized( final Number value )
  {
    final IDataRange<Number> dataRange = getNumericRange();
    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();
    final double norm = (value.doubleValue() - dataRange.getMin().doubleValue()) / r;
    return norm;
  }

  private Number normalizedToNumeric( double value )
  {
    final IDataRange<Number> dataRange = getNumericRange();

    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    final double logical = value * r + dataRange.getMin().doubleValue();

    return logical;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#getNumericRange()
   */
  public IDataRange<Number> getNumericRange( )
  {
    return m_numericRange;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#numericToScreen(java.lang.Number)
   */
  public Integer numericToScreen( Number value )
  {
    return normalizedToScreen( numericToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#screenToNumeric(int)
   */
  public Number screenToNumeric( int value )
  {
    return normalizedToNumeric( screenToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#setNumericRange(org.kalypso.chart.framework.model.data.IDataRange)
   */
  public void setNumericRange( IDataRange<Number> range )
  {
    m_numericRange = range;
    getEventHandler().fireMapperRangeChanged( this );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axis.AbstractAxis#setLogicalRange(org.kalypso.chart.framework.model.data.IDataRange)
   */
  @Override
  public void setLogicalRange( IDataRange<Number> dataRange )
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
   * Uses the widgets' complete extension to calculate the screen value in correspondance to a normalized value
   * 
   * @see de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent#normalizedToScreen(double)
   */
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
   * Uses the widgets' complete extension to alculates the normalized value in correspondance to a screen value
   * 
   * @see de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent#screenToNormalized(int)
   */
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

  /**
   * TODO: to abstract axis
   * 
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxis#getScreenHeight()
   */
  public int getScreenHeight( )
  {
    return m_height;
  }
  /**
   * @see de.openali.odysseus.chart.ext.base.axis.AbstractAxis#setLabel(java.lang.String)
   */
  @Override
  public void setLabel( String label )
  {
    if( !getLabel().equals( label ) )
    {
      super.setLabel( label );
      getEventHandler().fireMapperRangeChanged( this );
    }
  }
  public void setScreenHeight( int height )
  {
    m_height = height;
  }

}
