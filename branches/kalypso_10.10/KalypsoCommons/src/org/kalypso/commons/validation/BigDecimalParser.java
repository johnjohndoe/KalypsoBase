/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.commons.validation;

import java.math.BigDecimal;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.kalypso.contribs.java.lang.NumberUtils;

/**
 * @author Gernot Belger
 */
public class BigDecimalParser implements IParser
{
  private final boolean m_allowBlankString;

  private final Integer m_scale;

  public BigDecimalParser( final boolean allowBlankString )
  {
    this( allowBlankString, null );
  }

  /**
   * @param scale
   *          If set to non-<code>null</code>, the scale of the parsed {@link java.math.BigDecimal} will be set to this
   *          value.
   */
  public BigDecimalParser( final boolean allowBlankString, final Integer scale )
  {
    m_allowBlankString = allowBlankString;
    m_scale = scale;
  }

  /**
   * @see org.kalypso.commons.validation.IParser#parse(java.lang.String)
   */
  @Override
  public Object parse( final String text ) throws ParseException
  {
    if( StringUtils.isBlank( text ) )
    {
      if( m_allowBlankString )
        return null;

      throw new ParseException( "String is empty", 0 );
    }

    try
    {
      final BigDecimal parsedValue = NumberUtils.parseBigDecimal( text );
      if( m_scale == null )
        return parsedValue;

      return parsedValue.setScale( m_scale, BigDecimal.ROUND_HALF_UP );
    }
    catch( final NumberFormatException e )
    {
      throw new ParseException( e.getLocalizedMessage(), 0 );
    }
  }

}
