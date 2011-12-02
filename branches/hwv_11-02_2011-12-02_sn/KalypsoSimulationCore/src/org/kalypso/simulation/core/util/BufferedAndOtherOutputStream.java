package org.kalypso.simulation.core.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Gernot Belger
 */
public final class BufferedAndOtherOutputStream extends BufferedOutputStream
{
  private final PrintStream m_calcOutConsumer;

  public BufferedAndOtherOutputStream( final OutputStream outputStream, final PrintStream otherOutput )
  {
    super( outputStream );
    m_calcOutConsumer = otherOutput;
  }

  // REMARK: also stream stuff into System.out in order to have a log in the console.view
  /**
   * @see java.io.BufferedOutputStream#write(byte[], int, int)
   */
  @Override
  public synchronized void write( final byte[] b, final int off, final int len ) throws IOException
  {
    super.write( b, off, len );
    m_calcOutConsumer.write( b, off, len );
  }

  /**
   * @see java.io.BufferedOutputStream#write(int)
   */
  @Override
  public synchronized void write( final int b ) throws IOException
  {
    super.write( b );
    m_calcOutConsumer.write( b );
  }
}