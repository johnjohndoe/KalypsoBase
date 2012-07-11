package org.kalypso.commons.java.io;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.commons.process.StreamStreamer;

/**
 * Wraps a process and allows to wait until execution is terminated or has been cancelled.
 * <p>
 * Clients may inherit from this class and perform specific business in processCanceled() and/or processTerminated().
 *
 * @author schlienger
 */
public abstract class ProcessWraper
{
  private final Process m_proc;

  private final OutputStream m_logWriter;

  private int m_ms;

  /**
   * Construct a wrapper over the given process with the given logWriter
   *
   * @param proc
   * @param logWriter
   *          [optional, can be null]
   */
  public ProcessWraper( final Process proc, final OutputStream logWriter )
  {
    m_proc = proc;
    m_logWriter = logWriter;
  }

  /**
   * Sets the sleep time in milliseconds for a loop cycle
   *
   * @param ms
   */
  public synchronized void setCycleSleepTime( final int ms )
  {
    m_ms = ms;
  }

  /**
   * Starts to wait for the given process
   *
   * @param killProcessOnCancel
   *          If <code>true</code>, the process will be killed, if the monitor is cancelled.
   * @param monitor
   *          If this monitor is cancelled while waiting, the method returns.
   * @throws IOException
   * @throws InterruptedException
   */
  public synchronized void waitForProcess( final boolean killProcessOnCancel, final IProgressMonitor monitor ) throws InterruptedException
  {
    final String taskName = String.format( "Waiting for process: %s", m_proc );

    monitor.beginTask( taskName, IProgressMonitor.UNKNOWN );

    final OutputStream nul_dev = new NullOutputStream();
    final OutputStream log;
    if( m_logWriter == null )
      log = new NullOutputStream();
    else
      log = m_logWriter;

    try
    {
      new StreamStreamer( m_proc.getInputStream(), log );
      new StreamStreamer( m_proc.getErrorStream(), nul_dev );
      // new StreamStreamer( rIn, process.getOutputStream() );

      while( true )
      {
        try
        {
          final int rc = m_proc.exitValue();

          processTerminated( rc );

          return;
        }
        catch( final IllegalThreadStateException e )
        {
          // noch nicht fertig
        }

        if( monitor.isCanceled() )
        {
          if( killProcessOnCancel )
            m_proc.destroy();

          processCanceled();

          return;
        }

        monitor.worked( 1 );

        Thread.sleep( m_ms );
      }
    }
    finally
    {
      IOUtils.closeQuietly( nul_dev );
      if( m_logWriter == null )
        IOUtils.closeQuietly( log );
    }
  }

  /**
   * Called after the process has been told to be cancelled
   */
  public abstract void processCanceled( );

  /**
   * Called after the process has stopped execution
   *
   * @param returnCode
   *          the return code of the process
   */
  public abstract void processTerminated( int returnCode );
}