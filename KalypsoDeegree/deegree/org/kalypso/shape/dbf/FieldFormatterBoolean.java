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
package org.kalypso.shape.dbf;

import java.nio.charset.Charset;

import org.eclipse.core.runtime.Assert;

/**
 * @author Gernot Belger
 */
class FieldFormatterBoolean extends FieldFormatter
{
  private static final char CHAR_NULL = ' ';

  private static final char CHAR_TRUE = 'T';

  private static final char CHAR_FALSE = 'F';

  public FieldFormatterBoolean( )
  {
    super( null );
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.FieldFormatter#toBytes(java.lang.Object, java.nio.charset.Charset)
   */
  @Override
  public byte[] toBytes( final Object value, final Charset charset ) throws DBaseException
  {
    if( value == null )
      return new byte[] { CHAR_NULL };

    if( !(value instanceof Boolean) )
      throw new DBaseException( "Invalid data (should be Boolean): " + value );

    final Boolean logical = (Boolean) value;

    if( logical.booleanValue() )
      return new byte[] { CHAR_TRUE };
    else
      return new byte[] { CHAR_FALSE };
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.FieldFormatter#fromBytes(byte[], java.nio.charset.Charset)
   */
  @Override
  public Object fromBytes( final byte[] bytes, final Charset charset ) throws DBaseException
  {
    Assert.isTrue( bytes.length == 1 );

    switch( bytes[0] )
    {
      case ' ':
        return null;

      case CHAR_FALSE:
      case 'f':
      case 'N':
      case 'n':
        return Boolean.FALSE;

      case CHAR_TRUE:
      case 't':
      case 'Y':
      case 'y':
        return Boolean.TRUE;

      default:
        throw new DBaseException( "Unable to parse boolean value: " + new String( bytes, charset ) );
    }

  }

}
