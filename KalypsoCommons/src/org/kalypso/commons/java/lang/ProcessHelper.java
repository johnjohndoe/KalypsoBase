/**
 * ---------------- FILE HEADER KALYPSO ------------------------------------------
 *
 * This file is part of kalypso. Copyright (C) 2004 by:
 *
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestraße 22 21073
 * Hamburg, Germany http://www.tuhh.de/wb
 *
 * and
 *
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact:
 *
 * E-Mail: g.belger@bjoernsen.de m.schlienger@bjoernsen.de v.doemming@tuhh.de
 *
 * ---------------------------------------------------------------------------
 */
package org.kalypso.commons.java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.internal.i18n.Messages;
import org.kalypso.commons.process.StreamStreamer;
import org.kalypso.contribs.java.lang.ICancelable;

/**
 * @author Thül
 *         TODO: merge with IProcess / DefaultProcess stuff.
 */
public class ProcessHelper
{
  private final String m_commandLine;

  private final String[] m_commands;

  private final String[] m_environment;

  private final File m_workDir;

  private final ICancelable m_cancelable;

  private final long m_timeOut;

  private final OutputStream m_outStream;

  private final OutputStream m_errStream;

  private final InputStream m_inStream;

  private int m_sleepTime = 100;

  private Runnable m_idleWorker = null;

  private boolean m_killOnCancel = true;

  private int m_returnValue;

  private Process m_process;

  private ProcessControlThread m_procCtrlThread;

  /**
   * startet Prozess (sCmd, envp, fleExeDir), schreibt Ausgaben nach wLog, wErr, beendet den Prozess automatisch nach
   * iTOut ms (iTOut = 0 bedeutet, dass der Prozess nicht abgebrochen wird), die Abarbeitung des Prozesses beachtet auch
   * den Cancel-Status von cancelable
   * 
   * @param sCmd
   * @param envp
   * @param fleExeDir
   * @param cancelable
   * @param lTimeOut
   *          Time-out in milliseconds
   * @param wLog
   *          Gets connected to the output stream of the process.
   * @param wErr
   *          Gets connected to the error stream of the process.
   * @param rIn
   *          Gets connected to the input stream of the process.
   * @throws IOException
   * @throws ProcessTimeoutException
   */
  public static int startProcess( final String sCmd, final String[] envp, final File fleExeDir, final ICancelable cancelable, final long lTimeOut, final OutputStream wLog, final OutputStream wErr, final InputStream rIn ) throws IOException, ProcessTimeoutException
  {
    final ProcessHelper helper = new ProcessHelper( sCmd, envp, fleExeDir, cancelable, lTimeOut, wLog, wErr, rIn );
    return helper.start();
  }

  /**
   * startet Prozess (sCmd, envp, fleExeDir), schreibt Ausgaben nach wLog, wErr, beendet den Prozess automatisch nach
   * iTOut ms (iTOut = 0 bedeutet, dass der Prozess nicht abgebrochen wird), die Abarbeitung des Prozesses beachtet auch
   * den Cancel-Status von cancelable
   * 
   * @param sCmd
   * @param envp
   * @param fleExeDir
   * @param cancelable
   * @param lTimeOut
   *          Time-out in milliseconds
   * @param wLog
   *          Gets connected to the output stream of the process.
   * @param wErr
   *          Gets connected to the error stream of the process.
   * @param rIn
   *          Gets connected to the input stream of the process.
   * @throws IOException
   * @throws ProcessTimeoutException
   */
  public static int startProcess( final String[] sCmd, final String[] envp, final File fleExeDir, final ICancelable cancelable, final long lTimeOut, final OutputStream wLog, final OutputStream wErr, final InputStream rIn ) throws IOException, ProcessTimeoutException
  {
    final ProcessHelper helper = new ProcessHelper( sCmd, envp, fleExeDir, cancelable, lTimeOut, wLog, wErr, rIn );
    return helper.start();
  }

  /**
   * @param idleWorker
   *          This runnable gets called for every loop, while checking the running process.
   */
  public void setIdleWorker( final Runnable idleWorker )
  {
    m_idleWorker = idleWorker;
  }

  /**
   * @param sleepTime
   *          Sleep time for each loop, for checking the running process.
   */
  public void setSleepTime( final int sleepTime )
  {
    m_sleepTime = sleepTime;
  }

  /**
   * Set how the helper should act if the {@link ICancelable} is canceled while waiting for the process.
   * 
   * @param killOnCancel
   *          If set to <code>true</code>, the process will be killed if waitign is canceled, else the waiting methdo just returns.
   */
  public void setKillOnCancel( final boolean killOnCancel )
  {
    m_killOnCancel = killOnCancel;
  }

  /**
   * @param sCmd
   * @param envp
   * @param fleExeDir
   * @param cancelable
   * @param lTimeOut
   *          Time-out in milliseconds
   * @param wLog
   *          Gets connected to the output stream of the process.
   * @param wErr
   *          Gets connected to the error stream of the process.
   * @param rIn
   *          Gets connected to the input stream of the process.
   * @param sleepTime
   *          Sleep time for each loop, for checking the running process.
   * @param idleWorker
   *          This runnable gets called for every loop, while checking the running process.
   */
  public ProcessHelper( final String commandLine, final String[] environment, final File workDir, final ICancelable cancelable, final long lTimeOut, final OutputStream outStream, final OutputStream errStream, final InputStream inStream )
  {
    m_commandLine = commandLine;
    m_commands = null;
    m_environment = environment;
    m_workDir = workDir;
    m_cancelable = cancelable;
    m_timeOut = lTimeOut;
    m_outStream = outStream;
    m_errStream = errStream;
    m_inStream = inStream;
  }

  /**
   * @param sCmd
   * @param envp
   * @param fleExeDir
   * @param cancelable
   * @param lTimeOut
   *          Time-out in milliseconds
   * @param wLog
   *          Gets connected to the output stream of the process.
   * @param wErr
   *          Gets connected to the error stream of the process.
   * @param rIn
   *          Gets connected to the input stream of the process.
   * @param sleepTime
   *          Sleep time for each loop, for checking the running process.
   * @param idleWorker
   *          This runnable gets called for every loop, while checking the running process.
   */
  public ProcessHelper( final String[] commands, final String[] environment, final File workDir, final ICancelable cancelable, final long lTimeOut, final OutputStream outStream, final OutputStream errStream, final InputStream inStream )
  {
    m_commandLine = null;
    m_commands = commands;
    m_environment = environment;
    m_workDir = workDir;
    m_cancelable = cancelable;
    m_timeOut = lTimeOut;
    m_outStream = outStream;
    m_errStream = errStream;
    m_inStream = inStream;
  }

  /**
   * startet Prozess (sCmd, envp, fleExeDir), schreibt Ausgaben nach wLog, wErr, beendet den Prozess automatisch nach
   * iTOut ms (iTOut = 0 bedeutet, dass der Prozess nicht abgebrochen wird), die Abarbeitung des Prozesses beachtet auch
   * den Cancel-Status von cancelable
   * 
   * @throws IOException
   * @throws ProcessTimeoutException
   */
  public int start( ) throws IOException, ProcessTimeoutException
  {
    try
    {
      m_process = createProcess();

      if( m_timeOut > 0 )
      {
        m_procCtrlThread = new ProcessControlThread( m_process, m_timeOut );
        m_procCtrlThread.start();
      }

      new StreamStreamer( m_process.getInputStream(), m_outStream );
      new StreamStreamer( m_process.getErrorStream(), m_errStream );
      new StreamStreamer( m_inStream, m_process.getOutputStream() );

      // TODO: separate creation of process from waiting for process

      while( true )
      {
        try
        {
          m_returnValue = m_process.exitValue();
          break;
        }
        catch( final IllegalThreadStateException e )
        {
          // Prozess noch nicht fertig, weiterlaufen lassen
        }

        if( checkForCancel() )
          return m_returnValue;

        runIdle();

        Thread.sleep( m_sleepTime );
      }

      if( m_procCtrlThread != null )
        m_procCtrlThread.endProcessControl();
    }
    catch( final InterruptedException e )
    {
      // kann aber eigentlich gar nicht passieren
      // (wird geworfen von Thread.sleep( 100 ))
      e.printStackTrace();
    }

    if( m_procCtrlThread != null && m_procCtrlThread.procDestroyed() )
    {
      final String command = m_commands == null ? m_commandLine : StringUtils.join( m_commands );
      throw new ProcessTimeoutException( Messages.getString( "org.kalypso.commons.java.lang.ProcessHelper.1", command ) ); //$NON-NLS-1$
    }

    return m_returnValue;
  }

  private void runIdle( )
  {
    if( m_idleWorker == null )
      return;

    try
    {
      m_idleWorker.run();
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }
  }

  private boolean checkForCancel( ) throws InterruptedException
  {
    // TODO: canceling the process does not always work
    if( m_cancelable == null || !m_cancelable.isCanceled() )
      return false;

    if( m_killOnCancel )
    {
      m_process.destroy();

      if( m_procCtrlThread != null )
        m_procCtrlThread.endProcessControl();

      // Sometimes, the process is not yet really killed, when process.exitValue() is called,
      // causing an IllegalThreadStateException. In order to avoid this we wait a bit here,
      // maybe it works...
      Thread.sleep( 250 );

      m_returnValue = m_process.exitValue();
    }

    return true;
  }

  private Process createProcess( ) throws IOException
  {
    if( m_commandLine != null )
      return Runtime.getRuntime().exec( m_commandLine, m_environment, m_workDir );
    else
      return Runtime.getRuntime().exec( m_commands, m_environment, m_workDir );
  }
}