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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;

/**
 * This class is a writer for tokens (very usable for csv files).
 * 
 * @author Holger Albert
 */
public class TokenWriter
{
  /**
   * The destination file.
   */
  private File m_file;

  /**
   * The character, which separates the tokens.
   */
  private String m_delim;

  /**
   * The cnt of the tokens to be written.
   */
  private int m_cnt;

  /**
   * This list collects all lists of tokens.
   */
  private LinkedList<LinkedList<String>> m_allTokens;

  /**
   * The constructor.
   * 
   * @param file
   *          The destination file.
   * @param delim
   *          The character, which separates the tokens.
   * @param cnt
   *          The cnt of the tokens to be written.
   */
  public TokenWriter( File file, String delim, int cnt )
  {
    m_file = file;
    m_delim = delim;
    m_cnt = cnt;
    m_allTokens = new LinkedList<LinkedList<String>>();
  }

  /**
   * This function writes the csv file.
   */
  public void write( ) throws IOException
  {
    /* The buffered writer. */
    BufferedWriter bwr = null;

    try
    {
      /* Create the buffered reader. */
      bwr = new BufferedWriter( new FileWriter( m_file ) );

      for( int i = 0; i < m_allTokens.size(); i++ )
      {
        LinkedList<String> row = m_allTokens.get( i );

        for( int j = 0; j < row.size(); j++ )
        {
          String col = row.get( j );

          if( j < row.size() - 1 )
            bwr.write( col + m_delim );
          else if( j == row.size() - 1 )
            bwr.write( col );
        }

        /* End the line. */
        bwr.write( "\n" );
      }

      /* Close the writer. */
      bwr.close();
    }
    finally
    {
      /* Close the writer. */
      IOUtils.closeQuietly( bwr );
    }
  }

  /**
   * This function adds a row to the writer.
   * 
   * @param line
   *          The row, which will be added.
   */
  public void addRow( String line ) throws Exception
  {
    /* Tokenize the row. */
    StringTokenizer tokenizer = new StringTokenizer( line, m_delim );

    if( tokenizer.countTokens() == 0 )
      throw new Exception( "No tokens available with the delimiter of '" + m_delim + "'..." );

    if( tokenizer.countTokens() != m_cnt )
      throw new Exception( "Not enough or to much tokens, needs " + String.valueOf( m_cnt ) + " tokens..." );

    LinkedList<String> row = new LinkedList<String>();
    while( tokenizer.hasMoreTokens() )
      row.add( tokenizer.nextToken() );

    m_allTokens.add( row );
  }

  /**
   * This function adds a row to the writer.
   * 
   * @param list
   *          The row, which will be added.
   */
  public void addRow( LinkedList<String> list ) throws Exception
  {
    if( list.size() == 0 )
      throw new Exception( "No tokens available..." );

    if( list.size() < m_cnt )
      throw new Exception( "Not enough tokens, needs " + String.valueOf( m_cnt ) + " tokens..." );

    m_allTokens.add( list );
  }
}