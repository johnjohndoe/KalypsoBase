package org.kalypso.ogc.sensor.timeseries.interpolation.worker;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class LocalCalculationStack
{
  private Date m_date1 = null;

  private Date m_date2 = null;

  private final Set<LocalCalculationStackValue> m_values = new LinkedHashSet<>();

  public void setDate2( final Date d2 )
  {
    m_date2 = d2;
  }

  public Date getDate2( )
  {
    return m_date2;
  }

  public void setDate1( final Date d1 )
  {
    m_date1 = d1;
  }

  public Date getDate1( )
  {
    return m_date1;
  }

  public void add( final LocalCalculationStackValue value )
  {
    m_values.add( value );
  }

  public LocalCalculationStackValue[] getValues( )
  {
    return m_values.toArray( new LocalCalculationStackValue[] {} );
  }

  public void copyValues( )
  {
    final LocalCalculationStackValue[] values = getValues();
    for( final LocalCalculationStackValue value : values )
    {
      value.setValue1( value.getValue2() );
    }
  }
}