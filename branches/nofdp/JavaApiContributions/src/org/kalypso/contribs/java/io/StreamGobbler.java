/** This file is part of Kalypso
 *
 *  Copyright (c) 2008 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.java.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * This class can handle one stream from an external process.
 * 
 * @author Holger Albert
 */
public class StreamGobbler extends Thread
{
  private final InputStream m_is;

  private final String m_type;

  private final boolean m_debug;

  private final PrintStream m_output;

  /**
   * The constructor.
   * 
   * @param is
   *            The input stream to handle.
   * @param type
   *            The type of the stream.
   * @param debug
   *            True, if the stream should be written to the console.
   */
  public StreamGobbler( final InputStream is, final String type, final boolean debug )
  {
    this( is, type, debug, null );
  }

  /**
   * @param output
   *            flush input stream to output stream...
   */
  public StreamGobbler( final InputStream is, final String type, final boolean debug, PrintStream output )
  {
    m_is = is;
    m_type = type;
    m_debug = debug;
    m_output = output;
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run( )
  {
    try
    {
      final InputStreamReader isr = new InputStreamReader( m_is );
      final BufferedReader br = new BufferedReader( isr );
      String line = null;
      while( (line = br.readLine()) != null )
      {
        if( m_debug )
        {
          System.out.println( m_type + ": " + line );
        }

        if( m_output != null )
          m_output.println( line );
      }

    }
    catch( final IOException ioe )
    {
      ioe.printStackTrace();
    }
  }
}