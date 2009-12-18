package org.kalypso.ogc.util.copyobservation.source;

import java.util.Date;

import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.ogc.sensor.DateRange;

public class Source
{
  private String m_property;

  private Date m_from;

  private Date m_to;

  private String m_filter;


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

  public final String getProperty( )
  {
    return m_property;
  }

  public final void setProperty( final String prop )
  {
    this.m_property = prop;
  }

  public final Date getFrom( )
  {
    return m_from;
  }

  public final void setFrom( final String lfrom )
  {
    m_from = DateUtilities.parseDateTime( lfrom );
  }

  public final Date getTo( )
  {
    return m_to;
  }

  public final void setTo( final String lto )
  {
    m_to = DateUtilities.parseDateTime( lto );
  }

  public final String getFilter( )
  {
    return m_filter;
  }

  public final void setFilter( final String filt )
  {
    this.m_filter = filt;
  }

  public final DateRange getDateRange( )
  {
    return DateRange.createDateRangeOrNull( m_from, m_to );
  }

}