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
package org.kalypso.contribs.eclipse.core.runtime;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An input stream that reports read bytes to a {@link IProgressMonitor}.<br/>
 * Any read byte results in a tick to the monitor.
 *
 * @author Gernot Belger
 */
public class ProgressInputStream extends FilterInputStream
{
  /** This exception is thrown, if the monitor is cancelled while reading bytes. */
  public static final IOException CANCEL_EXCEPTION = new IOException( "Monitor canceled" );

  private static final String[] UNITS = new String[] { "Bytes", "KB", "MB", "GB", "TB" };

  private final IProgressMonitor m_monitor;

  private final long m_totalBytes;

  private final int m_unitPow;

  private int m_readBytes;

  private int m_step;

  public ProgressInputStream( final InputStream is, final long length, final IProgressMonitor monitor )
  {
    super( is );
    m_totalBytes = length;

    m_unitPow = findUnit( m_totalBytes );

    m_monitor = monitor;
  }

  private int findUnit( final long totalBytes )
  {
    int unitPow = 1;
    while( Math.pow( 1024, unitPow ) < totalBytes && unitPow < 4 )
      unitPow += 1;

    return unitPow - 1;
  }

  /**
   * @see java.io.FilterInputStream#read()
   */
  @Override
  public int read( ) throws IOException
  {
    m_monitor.worked( 1 );
    return super.read();
  }

  /**
   * @see java.io.FilterInputStream#read(byte[], int, int)
   */
  @Override
  public int read( final byte[] b, final int off, final int len ) throws IOException
  {
    final int read = super.read( b, off, len );
    worked( read );
    return read;
  }

  /**
   * @see java.io.FilterInputStream#read(byte[])
   */
  @Override
  public int read( final byte[] b ) throws IOException
  {
    final int read = super.read( b );
    worked( read );
    return read;
  }

  /**
   * @see java.io.FilterInputStream#skip(long)
   */
  @Override
  public long skip( final long n ) throws IOException
  {
    worked( (int) n );

    return super.skip( n );
  }

  private void worked( final int read ) throws IOException
  {
    if( m_monitor.isCanceled() )
      throw CANCEL_EXCEPTION;

    m_monitor.worked( read );

    m_readBytes += read;
    m_step += read;

    final String unit = UNITS[m_unitPow];
    if( m_unitPow == 0 )
      m_monitor.subTask( String.format( "read %d (%d) %s", m_readBytes, m_totalBytes, unit ) );
    else
    {
      final double unitDivider = Math.pow( 1024, m_unitPow );

      // PERFORMANCE: m_step used to check if we should update the monitor, else we may update every byte...
      if( m_step / unitDivider > 0.5 )
      {
        final double readKB = m_readBytes / unitDivider;
        final double totalKB = m_totalBytes / unitDivider;
        m_monitor.subTask( String.format( "%.1f (%.1f) %s", readKB, totalKB, unit ) );
        m_step = 0;
      }
    }
  }

}
