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
package org.kalypso.commons.junit;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

/**
 * Junit helper class for testing files.
 * 
 * @author Gernot Belger
 */
public class FileAssert extends Assert
{
  /**
   * Asserts that the contents of two files is equal.
   */
  public static void assertFileContentEquals( final File expected, final File actual )
  {
    try
    {
      final List< ? > actualLines = FileUtils.readLines( actual, null );
      final List< ? > expectedLines = FileUtils.readLines( expected, null );

      final Iterator< ? > actualIt = actualLines.iterator();
      final Iterator< ? > expectedIt = expectedLines.iterator();

      int count = 0;
      while( expectedIt.hasNext() && actualIt.hasNext() )
      {
        count++;

        final Object expectedLine = expectedIt.next();
        final Object actualLine = actualIt.next();

        try
        {
          Assert.assertEquals( expectedLine, actualLine );
        }
        catch( final AssertionError e )
        {
          final String msg = String.format( "Line %d: %s", count, e.getLocalizedMessage() );
          fail( msg );
        }
      }

      assertIteratorIsEmpty( "Actual file is longer than expected: ", actualIt );
      assertIteratorIsEmpty( "Expected file is longer than actual: ", expectedIt );
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      Assert.fail( "Failed to access file: " + e.getLocalizedMessage() );
    }
  }

  private static void assertIteratorIsEmpty( final String msg, final Iterator< ? > actualIt )
  {
    final String dumpActual = dumpIterator( actualIt );
    if( dumpActual != null )
      fail( msg + dumpActual );
  }

  private static String dumpIterator( final Iterator< ? > iter )
  {
    if( !iter.hasNext() )
      return null;

    final StringBuilder builder = new StringBuilder();
    while( iter.hasNext() )
      builder.append( iter.next() );
    return builder.toString();
  }

}
