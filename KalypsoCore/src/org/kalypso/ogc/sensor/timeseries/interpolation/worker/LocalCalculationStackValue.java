package org.kalypso.ogc.sensor.timeseries.interpolation.worker;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.parser.IParser;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.timeseries.interpolation.InterpolationFilter;
import org.kalypso.ogc.sensor.zml.ZmlFactory;

public class LocalCalculationStackValue
{
  private final IAxis m_axis;

  private TupleModelDataSet m_defaultValue;

  private TupleModelDataSet m_value1;

  private TupleModelDataSet m_value2;

  public LocalCalculationStackValue( final IAxis axis )
  {
    m_axis = axis;
  }

  public IAxis getAxis( )
  {
    return m_axis;
  }

  @Override
  public String toString( )
  {
    final StringBuilder builder = new StringBuilder();
    builder.append( String.format( "Axis:\t\t%s\n", m_axis.getName() ) ); //$NON-NLS-1$

    if( Objects.isNotNull( m_value1 ) )
      builder.append( String.format( "Value 1:\n%s\n", m_value1 ) ); //$NON-NLS-1$

    if( Objects.isNotNull( m_value2 ) )
      builder.append( String.format( "Value 2:\n%s\n", m_value2 ) ); //$NON-NLS-1$

    if( Objects.isNotNull( m_defaultValue ) )
      builder.append( String.format( "Default Value:\n%s\n", m_defaultValue ) ); //$NON-NLS-1$

    return builder.toString();
  }

  protected TupleModelDataSet getDefaultValue( final IInterpolationFilter filter ) throws SensorException
  {
    if( Objects.isNotNull( m_defaultValue ) )
      return m_defaultValue;

    try
    {
      final IParser parser = ZmlFactory.createParser( m_axis );
      final Object defaultValue = parser.parse( filter.getDefaultValue() );
      final Integer status = filter.getDefaultStatus();

      m_defaultValue = new TupleModelDataSet( m_axis, defaultValue, status, InterpolationFilter.DATA_SOURCE );

      return m_defaultValue;
    }
    catch( final Exception e )
    {
      throw new SensorException( e );
    }
  }

  public TupleModelDataSet getValue1( )
  {
    return m_value1;
  }

  public TupleModelDataSet getValue2( )
  {
    return m_value2;
  }

  public void setValue1( final TupleModelDataSet value1 )
  {
    m_value1 = value1;
  }

  public void setValue2( final TupleModelDataSet value2 )
  {
    m_value2 = value2;
  }
}