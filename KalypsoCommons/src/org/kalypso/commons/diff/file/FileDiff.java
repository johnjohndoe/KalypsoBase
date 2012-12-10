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

package org.kalypso.commons.diff.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;

public final class FileDiff implements IArrayDifferenceHandler
{
  private final File m_a;

  private final File m_b;

  private final String[] m_aLines;

  private final String[] m_bLines;

  public FileDiff( final File a, final File b ) throws IOException
  {
    m_a = a;
    m_b = b;

    m_aLines = read( m_a );
    m_bLines = read( m_b );
  }

  public boolean isEqual( final IDifferenceRule[] rules )
  {

    if( m_a == null || m_b == null )
      return false;
    else if( m_a.length() != m_b.length() )
      return false;
    else
    {
      final List<Difference> diffs = (new Diff<String>( m_aLines, m_bLines )).diff();
      for( final IDifferenceRule rule : rules )
      {
        if( !rule.isEqual( this, diffs ) )
          return false;
      }

      return true;
    }
  }

  private String[] read( final File file ) throws IOException
  {
    BufferedReader br = null;

    try
    {
      br = new BufferedReader( new FileReader( file ) );

      final List<String> contents = new ArrayList<String>();

      String in;
      while( (in = br.readLine()) != null )
      {
        contents.add( in );
      }

      return contents.toArray( new String[] {} );
    }
    finally
    {
      if( br != null )
        IOUtils.closeQuietly( br );
    }
  }

  /**
   * @see org.kalypso.commons.diff.file.IArrayDifferenceHandler#getLinesArrayA()
   */
  @Override
  public String[] getRowArrayA( )
  {
    return m_aLines;
  }

  /**
   * @see org.kalypso.commons.diff.file.IArrayDifferenceHandler#getLinesArrayB()
   */
  @Override
  public String[] getRowArrayB( )
  {
    return m_bLines;
  }

  /**
   * @see org.kalypso.commons.diff.file.IArrayDifferenceHandler#getRowA(int)
   */
  @Override
  public String getRowA( final int row )
  {
    if( m_aLines.length <= row )
      return null;

    return m_aLines[row];
  }

  /**
   * @see org.kalypso.commons.diff.file.IArrayDifferenceHandler#getRowB(int)
   */
  @Override
  public String getRowB( final int row )
  {
    if( m_bLines.length <= row )
      return null;

    return m_bLines[row];
  }

}
