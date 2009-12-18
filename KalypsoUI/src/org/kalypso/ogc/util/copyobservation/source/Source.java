package org.kalypso.ogc.util.copyobservation.source;

import java.util.Date;

import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;

public final class Source
{
  private String m_property;

  private Date m_from;

  private Date m_to;

  private String m_filter;

  private IObservation m_observation;

  public Source( )
  {
  }

  public Source( final String property, final DateRange daterange, final String filter )
  {
    m_property = property;
    if( daterange != null )
    {
      m_from = daterange.getFrom();
      m_to = daterange.getTo();
    }
    m_filter = filter;
  }

  public String getProperty( )
  {
    return m_property;
  }

  public void setProperty( final String prop )
  {
    this.m_property = prop;
  }

  public Date getFrom( )
  {
    return m_from;
  }

  public void setFrom( final String lfrom )
  {
    m_from = DateUtilities.parseDateTime( lfrom );
  }

  public Date getTo( )
  {
    return m_to;
  }

  public void setTo( final String lto )
  {
    m_to = DateUtilities.parseDateTime( lto );
  }

  public String getFilter( )
  {
    return m_filter;
  }

  public void setFilter( final String filt )
  {
    this.m_filter = filt;
  }

  public DateRange getDateRange()
  {
    return DateRange.createDateRangeOrNull( m_from, m_to );
  }

  public void setObservation( final IObservation observation )
  {
    m_observation = observation;
  }

  public IObservation getObservation( )
  {
    return m_observation;
  }
}