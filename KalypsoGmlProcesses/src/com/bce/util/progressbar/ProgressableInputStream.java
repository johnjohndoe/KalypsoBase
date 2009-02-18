package com.bce.util.progressbar;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

/**
 * Variant of the ProgressMonitorInputStream for Progressables
 * 
 * @author belger
 */
public class ProgressableInputStream extends FilterInputStream
{
  private int size = 0;

  private int nread = 0;

  private final Progressable m_p;

  public ProgressableInputStream( final InputStream in, final Progressable p )
  {
    super( in );

    try
    {
      size = in.available();
    }
    catch( final IOException ioe )
    {
      size = 0;
    }

    p.reset( 0, size );

    m_p = p;
  }

  private void abort( ) throws IOException
  {
    final InterruptedIOException exc = new InterruptedIOException( "progress" ); //$NON-NLS-1$
    exc.bytesTransferred = nread;
    throw exc;
  }

  @Override
  public int read( ) throws IOException
  {
    int c = in.read();
    if( c >= 0 )
      m_p.setCurrent( ++nread );

    if( m_p.isCanceled() )
      abort();

    return c;
  }

  @Override
  public int read( byte b[] ) throws IOException
  {
    final int nr = in.read( b );
    if( nr > 0 )
      m_p.setCurrent( nread += nr );
    if( m_p.isCanceled() )
      abort();

    return nr;
  }

  @Override
  public int read( byte b[], int off, int len ) throws IOException
  {
    final int nr = in.read( b, off, len );
    if( nr > 0 )
      m_p.setCurrent( nread += nr );
    if( m_p.isCanceled() )
      abort();

    return nr;
  }

  @Override
  public long skip( long n ) throws IOException
  {
    final long nr = in.skip( n );
    if( nr > 0 )
      m_p.setCurrent( nread += nr );
    return nr;
  }

  @Override
  public synchronized void reset( ) throws IOException
  {
    in.reset();
    nread = size - in.available();
    m_p.setCurrent( nread );
  }
}
