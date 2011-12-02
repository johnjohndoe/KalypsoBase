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

import java.text.NumberFormat;
import java.text.ParseException;

import org.kalypso.commons.parser.AbstractParser;
import org.kalypso.commons.parser.ParserException;

/**
 * Ein Parser für Integer, Long, und Short Objekte.
 *
 * @author schlienger
 */
public class IntegerParser extends AbstractParser
{
  private final NumberFormat m_nf;

  private final String m_format;

  /**
   * Default Constructor
   */
  public IntegerParser()
  {
    this( "" ); //$NON-NLS-1$
  }

  /**
   * @param format
   *          hat keine Bedeutung für Integers
   */
  public IntegerParser( final String format )
  {
    m_format = format;

    m_nf = NumberFormat.getIntegerInstance();
  }

  /**
   * @see org.kalypso.commons.parser.IParser#getObjectClass()
   */
  @Override
  public Class<Integer> getObjectClass( )
  {
    return Integer.class;
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
      return m_nf.parse( text );
    }
    catch( final ParseException e )
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
    return m_nf.format( obj );
  }

  /**
   * @see org.kalypso.commons.parser.IParser#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare( final Object value1, final Object value2 )
  {
    final int n1 = ( (Number)value1 ).intValue();
    final int n2 = ( (Number)value2 ).intValue();

    if( n1 < n2 )
      return -1;

    if( n1 > n2 )
      return 1;

    return 0;
  }
}