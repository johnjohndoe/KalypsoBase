/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.service.wps.refactoring;

import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.StatusType;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.service.wps.internal.KalypsoServiceWPSDebug;

/**
 * @author Dirk Kuch
 */
public class AsynchronousWPSWatchdog
{
  private static final int TIMEOUT_STEP = 2000;

  private final IWPSObserver m_observer;

  private final IWPSProcess m_process;

  private final long m_timeout;

  private int m_alreadyWorked;

  public AsynchronousWPSWatchdog( final IWPSProcess process, final IWPSObserver observer, final long timeout )
  {
    m_process = process;
    m_observer = observer;
    m_timeout = timeout;
  }

  public IStatus waitForProcess( final IProgressMonitor monitor )
  {
    try
    {
      KalypsoServiceWPSDebug.DEBUG.printf( "Checking state file of the server ...\n" ); //$NON-NLS-1$

      /* Poll to update the status. */
      final boolean run = true;
      long executed = 0;

      /* Loop, until an result is available, a timeout is reached or the user has cancelled the job. */
      final String title = m_process.getTitle();

      monitor.beginTask( Messages.getString( "org.kalypso.service.wps.refactoring.AsynchronousWPSWatchdog.0" ) + title, 100 ); // 100% //$NON-NLS-1$

      Integer lastPercentage = 0;
      
      while( run )
      {
        try
        {
          Thread.sleep( TIMEOUT_STEP );
          //not a timeout behavior - this is the upper limit for execution time...
//          executed += 2000;
        }
        catch( final InterruptedException e )
        {
          return StatusUtilities.statusFromThrowable( e );
        }

        final ExecuteResponseType exState = m_process.getExecuteResponse();
        if( exState == null )
          return StatusUtilities.createErrorStatus( Messages.getString( "org.kalypso.service.wps.refactoring.AsynchronousWPSWatchdog.1" ) ); //$NON-NLS-1$

        final Integer percentage = m_process.getPercentCompleted();
        final String statusDescription = m_process.getStatusDescription();
        tickMonitor( statusDescription, percentage, monitor );
        if( lastPercentage != percentage ){
          executed = 0;
        }
        else{
          executed += TIMEOUT_STEP;
        }
        lastPercentage = percentage;

        final StatusType state = exState.getStatus();
        if( state.getProcessAccepted() != null )
          m_observer.handleAccepted( exState );
        else if( state.getProcessStarted() != null )
          m_observer.handleStarted( monitor, exState );
        else if( state.getProcessFailed() != null )
          return m_observer.handleFailed( exState );
        else if( state.getProcessSucceeded() != null )
          return m_observer.handleSucceeded( exState );
        else
          return m_observer.handleUnknownState( exState );

        if( monitor.isCanceled() )
          return m_observer.handleCancel();

        //by setting m_timeout to 0 it will switch the timeout off
        if( m_timeout > 0 && executed >= m_timeout )
          return m_observer.handleTimeout();
      }
    }
    catch( final Exception e )
    {
      return StatusUtilities.statusFromThrowable( e );
    }
  }

  private void tickMonitor( final String description, final Integer percent, final IProgressMonitor monitor )
  {
    int percentCompleted = 0;
    if( percent != null )
      percentCompleted = percent.intValue();

    /* If the already updated value is not equal to the new value. Update the monitor values. */
    if( percentCompleted != m_alreadyWorked )
    {
      /* Check, what is already updated here, and update the rest. */
      final int percentWorked = percentCompleted - m_alreadyWorked;

      /* Project percentWorked to the left monitor value. */
      monitor.worked( percentWorked );

      m_alreadyWorked = percentCompleted;
    }

    monitor.subTask( description );
  }

}
