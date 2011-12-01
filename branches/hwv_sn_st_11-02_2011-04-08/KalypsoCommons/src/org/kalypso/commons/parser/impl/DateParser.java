/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.commons.parser.impl;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kalypso.commons.parser.AbstractParser;
import org.kalypso.commons.parser.ParserException;

/**
 * Ein Parser für Date Objekte.
 *
 * @author schlienger
 */
public class DateParser extends AbstractParser
{
  private DateTimeFormatter m_df;

  private final String m_format;

  /**
   * Default constructor.
   */
  public DateParser()
  {
    this( "" ); //$NON-NLS-1$
  }

  /**
   * @param format
   *          siehe Spezifikation in SimpleDateFormat
   * @see SimpleDateFormat
   */
  public DateParser( final String format )
  {
    m_format = format;

    if( format.length() == 0 )
      m_df = DateTimeFormat.forStyle( "SS" );
    else
      m_df = DateTimeFormat.forPattern( m_format );
  }

  /**
   * @see org.kalypso.commons.parser.IParser#getObjectClass()
   */
  @Override
  public Class<Date> getObjectClass( )
  {
    return Date.class;
  }

  /**
   * @see org.kalypso.commons.parser.IParser#getFormat()
   */
  @Override
  public String getFormat()
  {
    return m_format;
  }

  /**
   * @throws ParserException
   * @see org.kalypso.commons.parser.IParser#parse(java.lang.String)
   */
  @Override
  public Object parse( final String text ) throws ParserException
  {
    try
    {
      final DateTime dateTime = m_df.parseDateTime( text );
      return dateTime.toDate();
    }
    catch( final IllegalArgumentException e )
    {
      throw new ParserException( e );
    }
  }

  /**
   * @see org.kalypso.commons.parser.AbstractParser#toStringInternal(java.lang.Object)
   */
  @Override
  public String toStringInternal( final Object obj )
  {
    return m_df.print( new DateTime( obj ) );
  }

  /**
   * @see org.kalypso.commons.parser.IParser#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare( final Object value1, final Object value2 )
  {
    final Date d1 = (Date)value1;
    final Date d2 = (Date)value2;

    return d1.compareTo( d2 );
  }

  public void setTimezone( final TimeZone tz )
  {
    if( tz == null )
      m_df = m_df.withZone( DateTimeZone.getDefault() );
    else
    {
      final DateTimeZone zone = DateTimeZone.forTimeZone( tz );
      m_df = m_df.withZone( zone );
    }
  }
}