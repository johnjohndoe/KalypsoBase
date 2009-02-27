package org.kalypso.chart.ext.base.axis;

import java.util.Comparator;

import org.kalypso.chart.ext.base.data.NumberDataOperator;
import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DATATYPE;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;

/**
 * 
 * logical and numerical range are identical;
 * 
 * @author burtscher Concrete IAxis implementation - to be used for numeric data
 */
public class NumberAxis extends AbstractAxis<Number>
{
  private final NumberDataOperator m_dataOperator;

  public NumberAxis( final String id, final String label, final PROPERTY prop, final POSITION pos, final DIRECTION dir, final Comparator<Number> comp )
  {
    super( id, label, prop, pos, dir, comp, Number.class );
    m_dataOperator = new NumberDataOperator( comp );
  }

  public double logicalToNormalized( final Number value )
  {
    final IDataRange<Number> dataRange = getLogicalRange();

    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    final double norm = (value.doubleValue() - dataRange.getMin().doubleValue()) / r;

    return norm;
  }

  public Number normalizedToLogical( final double value )
  {
    final IDataRange<Number> dataRange = getLogicalRange();

    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    final double logical = value * r + dataRange.getMin().doubleValue();

    return logical;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#logicalToScreen(T)
   */
  public int logicalToScreen( final Number value )
  {
    if( value == null )
      return 0;
    if( getRegistry() == null )
      return 0;

    final IAxisComponent comp = getRegistry().getComponent( this );
    if( comp == null )
      return 0;

    return comp.normalizedToScreen( logicalToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#screenToLogical(int)
   */
  public Number screenToLogical( final int value )
  {
    if( getRegistry() == null )
      return Double.NaN;

    final IAxisComponent comp = getRegistry().getComponent( this );
    if( comp == null )
      return Double.NaN;

    return normalizedToLogical( comp.screenToNormalized( value ) );
  }

  public int zeroToScreen( )
  {
    return logicalToScreen( 0.0 );
  }

  public IDataOperator<Number> getDataOperator( )
  {
    return m_dataOperator;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#getNumericRange()
   */
  public IDataRange<Number> getNumericRange( )
  {
    return getLogicalRange();
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#numericToScreen(java.lang.Number)
   */
  public int numericToScreen( Number value )
  {
    return logicalToScreen( value );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#screenToNumeric(int)
   */
  public Number screenToNumeric( int value )
  {
    return screenToLogical( value );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#setNumericRange(org.kalypso.chart.framework.model.data.IDataRange)
   */
  public void setNumericRange( IDataRange<Number> range )
  {
    setLogicalRange( range );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IMapper#getDataOperator(java.lang.Object)
   */
  public <T> IDataOperator<T> getDataOperator( Class<T> clazz )
  {
    if( Number.class.isAssignableFrom( clazz ) )
      return (IDataOperator<T>) getDataOperator();
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#getType()
   */
  public DATATYPE getDataType( )
  {
    return DATATYPE.NUMBER;
  }
}
