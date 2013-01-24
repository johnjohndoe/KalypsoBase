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
package org.kalypso.commons.java.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Dirk Kuch
 */
public final class Objects
{
  private Objects( )
  {
  }

  public static boolean isNull( final Object... objects )
  {
    if( objects == null )
      return true;

    for( final Object object : objects )
    {
      if( object == null )
        return true;
    }

    return false;
  }

  public static boolean isNotNull( final Object... objects )
  {
    return !isNull( objects );
  }

  public static boolean equal( final Object a, final Object b )
  {
    return com.google.common.base.Objects.equal( a, b );
  }

  public static boolean allNull( final Object... objects )
  {
    if( objects == null )
      return true;

    for( final Object object : objects )
    {
      if( isNotNull( object ) )
        return false;
    }

    return true;
  }

  public static boolean allNotNull( final Object... objects )
  {
    if( objects == null )
      return false;

    for( final Object object : objects )
    {
      if( isNull( object ) )
        return false;
    }

    return true;
  }

  public static boolean notEqual( final Object a, final Object b )
  {
    return !equal( a, b );
  }

  public static Object firstNonNull( final Object a, final Object b )
  {
    return com.google.common.base.Objects.firstNonNull( a, b );
  }

  public static Object clone( final Object object )
  {
    if( Objects.isNull( object ) )
      return null;

    Object clone = null;
    try
    {
      // Write the object out to a byte array
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final ObjectOutputStream out = new ObjectOutputStream( bos );
      out.writeObject( object );
      out.flush();
      out.close();

      // Make an input stream from the byte array and read
      // a copy of the object back in.
      final ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream( bos.toByteArray() ) );
      clone = in.readObject();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
    catch( final ClassNotFoundException cnfe )
    {
      cnfe.printStackTrace();
    }

    return clone;
  }
}
