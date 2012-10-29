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
package org.kalypso.core.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kalypso.core.Debug;

/**
 * This class reads a specific amount of tokens and collects them in an array.
 *
 * @author Holger Albert
 */
public class TokenReader
{
  /**
   * The file to be read. It must be an asccii file.
   */
  private final File m_file;

  /**
   * The character, which splits the tokens.
   */
  private final String m_delim;

  /**
   * This list collects all lists of tokens.
   */
  private final LinkedList<LinkedList<String>> m_allTokens;

  /**
   * The constructor.
   *
   * @param file
   *          The file to be read. It must be an asccii file.
   * @param delim
   *          The character, which splits the tokens.
   */
  public TokenReader( final File file, final String delim )
  {
    m_file = file;
    m_delim = delim;
    m_allTokens = new LinkedList<>();
  }

  /**
   * Reads all tokens out of the file. If more then the count tokens specified exists in one line, they will be ignored.
   * If less exists, there will be an error.
   */
  public void read( ) throws IOException
  {
    /* Reset the vector. */
    m_allTokens.clear();

    /* Memory for the buffered reader. */
    BufferedReader br = null;

    try
    {
      /* Create the buffered reader. */
      br = new BufferedReader( new FileReader( m_file ) );

      /* Memory for one line. */
      String line = null;

      /* Read as long there exists something. */
      while( (line = br.readLine()) != null )
      {
        /* All lines starting with * or # will be ignored. */
        if( line.startsWith( "*" ) || line.startsWith( "#" ) ) //$NON-NLS-1$ //$NON-NLS-2$
        {
          /* Debug-Information. */
          Debug.TOKEN_READER.printf( "TokenReader: Ignore the comment: %s%n", line ); //$NON-NLS-1$

          continue;
        }

        /* Tokenize the line. */
        final String[] splittedLine = StringUtils.split( line, m_delim );

        /* Debug-Information. */
        Debug.TOKEN_READER.printf( "TokenReader: Count tokens in this line: %d%n", splittedLine.length ); //$NON-NLS-1$

        if( splittedLine.length == 0 )
        {
          /* Debug-Information. */
          Debug.TOKEN_READER.printf( "TokenReader: Skipping this line. No tokens in this line: %s%n", line ); //$NON-NLS-1$

          continue;
        }

        /* Debug-Information. */
        Debug.TOKEN_READER.printf( "TokenReader: Adding '%d' tokens ...%n", splittedLine.length ); //$NON-NLS-1$

        /* Create a new row. */
        final LinkedList<String> cols = new LinkedList<>();

        /* Get the token for each loop. */
        for( final String token : splittedLine )
          cols.add( token );

        /* Add the new row. */
        m_allTokens.add( cols );
      }

      br.close();
    }
    finally
    {
      IOUtils.closeQuietly( br );
    }
  }

  /**
   * This function returns the token in the given list (row) at the given position (col).
   *
   * @param row
   *          The index of the list.
   * @param col
   *          The index of the list.
   * @return The specified token.
   */
  public String getToken( final int row, final int col )
  {
    if( m_allTokens.size() == 0 )
      return null;

    if( row < 0 || row >= m_allTokens.size() )
      return null;

    if( col < 0 || col >= m_allTokens.get( 0 ).size() )
      return null;

    return m_allTokens.get( row ).get( col );
  }

  /**
   * This function returns the size of the columns.
   *
   * @return The count columns.
   */
  public int getColSize( )
  {
    if( m_allTokens.size() == 0 )
      return 0;

    return m_allTokens.get( 0 ).size();

  }

  /**
   * This function returns the size of the rows.
   *
   * @return The size of the rows.
   */
  public int getRowSize( )
  {
    return m_allTokens.size();
  }
}