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
package org.kalypso.service.wps.refactoring;

import net.opengeospatial.ows.ExceptionReport;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.ProcessFailedType;
import net.opengeospatial.wps.StatusType;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.service.wps.client.exceptions.WPSException;
import org.kalypso.service.wps.i18n.Messages;
import org.kalypso.service.wps.internal.KalypsoServiceWPSDebug;
import org.kalypso.service.wps.utils.WPSUtilities;

/**
 * @author Dirk Kuch
 */
public class DefaultWpsObserver implements IWPSObserver
{

  /**
   * @see org.kalypso.service.wps.client.IWPSObserver#handleAccepted(net.opengeospatial.wps.ExecuteResponseType)
   */
  @Override
  public void handleAccepted( final ExecuteResponseType exState )
  {
    // nothing to do
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSObserver#handleCancel()
   */
  @Override
  public IStatus handleCancel( )
  {
    return Status.CANCEL_STATUS;
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSObserver#handleFailed(net.opengeospatial.wps.ExecuteResponseType)
   */
  @Override
  public IStatus handleFailed( final ExecuteResponseType exState )
  {
    final StatusType exStatus = exState.getStatus();

    final ProcessFailedType processFailed = exStatus.getProcessFailed();
    final ExceptionReport exceptionReport = processFailed.getExceptionReport();
    final String messages = WPSUtilities.createErrorString( exceptionReport );

    return StatusUtilities.createErrorStatus( messages );
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSObserver#handleStarted(org.eclipse.core.runtime.IProgressMonitor,
   *      net.opengeospatial.wps.ExecuteResponseType)
   */
  @SuppressWarnings("unused")
  @Override
  public void handleStarted( final IProgressMonitor monitor, final ExecuteResponseType exState ) throws WPSException
  {
    // nothing to do
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSObserver#handleSucceeded(net.opengeospatial.wps.ExecuteResponseType)
   */
  @Override
  public IStatus handleSucceeded( final ExecuteResponseType exState )
  {
    /* Get the process outputs. */
    final net.opengeospatial.wps.ExecuteResponseType.ProcessOutputs processOutputs = exState.getProcessOutputs();

    if( processOutputs == null )
    {
      return StatusUtilities.createErrorStatus( "The process did not return any results." ); //$NON-NLS-1$
    }
    else
    {
      return Status.OK_STATUS;
    }
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSObserver#handleTimeout()
   */
  @Override
  public IStatus handleTimeout( )
  {
    KalypsoServiceWPSDebug.DEBUG.printf( "Timeout reached ...\n" ); //$NON-NLS-1$

    return StatusUtilities.createErrorStatus( Messages.getString( "org.kalypso.service.wps.refactoring.DefaultWpsObserver.2" ) ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.service.wps.client.IWPSObserver#handleUnknownState(net.opengeospatial.wps.ExecuteResponseType)
   */
  @Override
  public final IStatus handleUnknownState( final ExecuteResponseType exState )
  {
    return StatusUtilities.createErrorStatus( Messages.getString( "org.kalypso.service.wps.refactoring.DefaultWpsObserver.3" ) ); //$NON-NLS-1$
  }

}
