package org.kalypso.ogc.sensor.timeseries.interpolation.worker;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.parser.IParser;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.zml.ZmlFactory;

public class LocalCalculationStackValue
{
  private final IAxis m_axis;

  private Object m_defaultValue;

  private double m_value1;

  private double m_value2;

  public LocalCalculationStackValue( final IAxis axis )
  {
    m_axis = axis;
  }

  public IAxis getAxis( )
  {
    return m_axis;
  }

  protected Object getDefaultValue( final IInterpolationFilter filter ) throws SensorException
  {
    if( Objects.isNotNull( m_defaultValue ) )
      return m_defaultValue;

    try
    {
      if( KalypsoStatusUtils.isStatusAxis( m_axis ) )
        return filter.getDefaultStatus();
      else
      {
        final IParser parser = ZmlFactory.createParser( m_axis );
        m_defaultValue = parser.parse( filter.getDefaultValue() );

        return m_defaultValue;
      }
    }
    catch( final Exception e )
    {
      throw new SensorException( e );
    }
  }

  public double getValue1( )
  {
    return m_value1;
  }

  public double getValue2( )
  {
    return m_value2;
  }

  public void setValue1( final double value1 )
  {
    m_value1 = value1;
  }

  public void setValue2( final double value2 )
  {
    m_value2 = value2;
  }
}