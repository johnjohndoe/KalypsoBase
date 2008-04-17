package org.kalypso.contribs.java.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    m_is = is;
    m_type = type;
    m_debug = debug;
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
          System.out.println( m_type + ": " + line );
      }
    }
    catch( final IOException ioe )
    {
      ioe.printStackTrace();
    }
  }
}