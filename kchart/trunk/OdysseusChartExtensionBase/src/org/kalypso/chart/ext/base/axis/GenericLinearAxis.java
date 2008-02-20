package org.kalypso.chart.ext.base.axis;

import java.util.Calendar;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.kalypso.chart.ext.base.axis.AbstractAxis;
import org.kalypso.chart.ext.base.data.CalendarDataOperator;
import org.kalypso.chart.ext.base.data.NumberDataOperator;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DATATYPE;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent;

/**
 * 
 * logical and numerical range are identical;
 * 
 * @author burtscher Concrete IAxis implementation - to be used for numeric data
 */
public class GenericLinearAxis extends AbstractAxis<Number>
{
  private final NumberDataOperator m_numberAdapter;

  private final StringOperator m_stringAdapter;

  private final CalendarDataOperator m_calendarAdapter;

  private IDataRange<Number> m_numericRange;

  private final DATATYPE m_type;

  public GenericLinearAxis( final String id, final String label, final PROPERTY prop, final POSITION pos, final DIRECTION dir, final String[] valueArray, final DATATYPE type )
  {
    super( id, label, prop, pos, dir, new ComparableComparator(), Number.class );
    m_type = type;
    m_numberAdapter = new NumberDataOperator( new ComparableComparator() );
    if( valueArray != null && valueArray.length > 1 )
    {
      m_stringAdapter = new StringOperator( valueArray );
    }
    else
    {
      m_stringAdapter = null;
    }
    m_calendarAdapter = new CalendarDataOperator( new ComparableComparator(), "" );
  }

  public double logicalToNormalized( final Number value )
  {
    return numericToNormalized( value );
  }

  public double numericToNormalized( final Number value )
  {
    final IDataRange<Number> dataRange = getNumericRange();
    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();
    final double norm = (value.doubleValue() - dataRange.getMin().doubleValue()) / r;
    return norm;
  }

  public Number normalizedToLogical( final double value )
  {
    return normalizedToNumeric( value );
  }

  private Number normalizedToNumeric( double value )
  {
    final IDataRange<Number> dataRange = getNumericRange();

    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    final double logical = value * r + dataRange.getMin().doubleValue();

    return logical;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#logicalToScreen(T)
   */
  public int logicalToScreen( final Number value )
  {
    // wir wollen weg von logical
    assert (false);
    return -100;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#screenToLogical(int)
   */
  public Number screenToLogical( final int value )
  {
    return screenToNumeric( value );
  }

  public int zeroToScreen( )
  {
    return numericToScreen( 0.0 );
  }

  public IDataOperator<Number> getDataOperator( )
  {
    return m_numberAdapter;
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
  public int numericToScreen( Number value )
  {
    if( value == null )
    {
      return 0;
    }
    if( getRegistry() == null )
    {
      return 0;
    }

    final IAxisComponent comp = getRegistry().getComponent( this );
    if( comp == null )
    {
      return 0;
    }

    return comp.normalizedToScreen( logicalToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#screenToNumeric(int)
   */
  public Number screenToNumeric( int value )
  {
    if( getRegistry() == null )
    {
      return Double.NaN;
    }

    final IAxisComponent comp = getRegistry().getComponent( this );
    if( comp == null )
    {
      return Double.NaN;
    }

    return normalizedToNumeric( comp.screenToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#setNumericRange(org.kalypso.chart.framework.model.data.IDataRange)
   */
  public void setNumericRange( IDataRange<Number> range )
  {
    m_numericRange = range;
    getEventHandler().fireMapperRangeChanged( this );
  }

  @SuppressWarnings("unchecked")
  public <T> IDataOperator<T> getDataOperator( Class<T> clazz )
  {
    if( Number.class.isAssignableFrom( clazz ) )
    {
      return (IDataOperator<T>) m_numberAdapter;
    }
    if( String.class.isAssignableFrom( clazz ) )
    {
      return (IDataOperator<T>) m_stringAdapter;
    }
    if( Calendar.class.isAssignableFrom( clazz ) )
    {
      return (IDataOperator<T>) m_calendarAdapter;
    }
    return null;
  }

  /**
   * @see org.kalypso.chart.ext.base.axis.AbstractAxis#setLogicalRange(org.kalypso.chart.framework.model.data.IDataRange)
   */
  @Override
  public void setLogicalRange( IDataRange<Number> dataRange )
  {
    // Nix machen! Wir wollen auf logical Range verzichten
    assert (false);
  }

  /**
   * @see org.kalypso.chart.ext.base.axis.AbstractAxis#getLogicalRange()
   */
  @Override
  public IDataRange<Number> getLogicalRange( )
  {
    assert (false);
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#getType()
   */
  public DATATYPE getDataType( )
  {
    return m_type;
  }

}
