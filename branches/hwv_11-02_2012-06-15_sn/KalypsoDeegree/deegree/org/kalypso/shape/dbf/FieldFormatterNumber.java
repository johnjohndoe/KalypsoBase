/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.shape.dbf;


/**
 * @author Gernot Belger
 */
class FieldFormatterNumber extends FieldFormatter
{
  private final short m_length;

  private final short m_precision;

  public FieldFormatterNumber( final short length, final short precision )
  {
    super( createPattern( length, precision ) );
    m_length = length;
    m_precision = precision;
  }

  private static String createPattern( final short length, final short precision )
  {
    final StringBuffer pattern = new StringBuffer();
    pattern.append( '%' );
    pattern.append( length );

    if( precision > 0 )
    {
      // always append decimalcount for float/double value
      pattern.append( '.' );
      pattern.append( precision );
      pattern.append( 'f' );
    }
    else
      pattern.append( 'd' );

    return pattern.toString();
  }

  /**
   * @see org.kalypso.shape.dbf.FieldFormatter#fromString(java.lang.String)
   */
  @Override
  public Object fromString( final String value ) throws DBaseException
  {
    if( value.isEmpty() )
      return null;

    try
    {
      if( m_precision == 0 )
      {
        if( m_length < 10 )
          return new Integer( value );

        return new Long( value );
      }

      if( m_length < 8 )
        return new Float( value );

      return new Double( value );
    }
    catch( final NumberFormatException ex )
    {
      final String msg = String.format( "Failed to parse number: '%s'", value );
      throw new DBaseException( msg, ex );
    }
  }
}
