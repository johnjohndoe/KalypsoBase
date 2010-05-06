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
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * @author Gernot Belger
 */
abstract class FieldFormatter
{
  private static final byte[] EMPY_BYTES = new byte[0];

  private final String m_pattern;

  public FieldFormatter( final String pattern )
  {
    m_pattern = pattern;
  }

  /**
   * @see org.kalypsodeegree_impl.io.shpapi.FieldFormatter#toBytes(java.lang.Object, java.lang.String)
   */
  public byte[] toBytes( final Object value, @SuppressWarnings("unused") final Charset charset ) throws DBaseException
  {
    if( value == null )
      return EMPY_BYTES;

    try
    {
      final String format = String.format( Locale.US, m_pattern, value );
      return format.getBytes();
    }
    catch( final IllegalFormatException e )
    {
      throw new DBaseException( "Unable to format value as number: " + value );
    }
  }

  public abstract Object fromBytes( byte[] bytes, Charset charset ) throws DBaseException;
}
