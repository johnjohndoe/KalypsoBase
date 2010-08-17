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
package org.kalypso.contribs.eclipse.ui.progress;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.kalypso.contribs.eclipse.EclipseRCPContributionsPlugin;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.CoreRunnableWrapper;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;

/**
 * Helper class with utility methods to handle progress.
 *
 * @author Gernot Belger
 */
public final class ProgressUtilities
{
  private ProgressUtilities( )
  {
    // helper class, do not instantiate
  }

  /**
   * Same as
   * {@link org.eclipse.ui.progress.IProgressService#busyCursorWhile(org.eclipse.jface.operation.IRunnableWithProgress)}
   * but works with {@link ICoreRunnableWithProgress}.
   */
  public static IStatus busyCursorWhile( final ICoreRunnableWithProgress operation )
  {
    return busyCursorWhile( operation, null );
  }

  /**
   * Same as
   * {@link org.eclipse.ui.progress.IProgressService#busyCursorWhile(org.eclipse.jface.operation.IRunnableWithProgress)}
   * but works with {@link ICoreRunnableWithProgress}.
   */
  public static IStatus busyCursorWhile( final ICoreRunnableWithProgress operation, final String errorMessage )
  {
    final CoreRunnableWrapper runnable = new CoreRunnableWrapper( operation );

    final IStatus status = busyCursorWhile( runnable, errorMessage );
    if( !status.isOK() )
      return status;

    return runnable.getStatus();
  }

  public static IStatus busyCursorWhile( final IRunnableWithProgress runnable, final String errorMessage )
  {
    return busyCursorWhile( PlatformUI.getWorkbench().getProgressService(), runnable, errorMessage );
  }

  public static IStatus busyCursorWhile( final IProgressService service, final ICoreRunnableWithProgress operation, final String errorMessage )
  {
    final CoreRunnableWrapper runnable = new CoreRunnableWrapper( operation );

    final IStatus status = busyCursorWhile( service, runnable, errorMessage );
    if( !status.isOK() )
      return status;

    return runnable.getStatus();
  }

  public static IStatus busyCursorWhile( final IProgressService service, final IRunnableWithProgress runnable, final String errorMessage )
  {
    try
    {
      service.busyCursorWhile( runnable );
      return Status.OK_STATUS;
    }
    catch( final InvocationTargetException e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e, errorMessage );
      EclipseRCPContributionsPlugin.getDefault().getLog().log( status );
      return status;
    }
    catch( final InterruptedException e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e, errorMessage );
      EclipseRCPContributionsPlugin.getDefault().getLog().log( status );
      return status;
    }
  }

  /**
   * Calls {@link IProgressMonitor#worked(int)} on the given monitor.
   * <p>
   * In addition, it checks if the monitor is canceled and throws an CoreException with CANCEL_STATUS if this is the
   * cae.
   * </p>
   *
   * @see IProgressMonitor#worked(int)
   */
  public static void worked( final IProgressMonitor monitor, final int work ) throws CoreException
  {
    if( monitor == null )
      return;

    monitor.worked( work );
    if( monitor.isCanceled() )
      throw new CoreException( Status.CANCEL_STATUS );
  }

  /**
   * Calls {@link IProgressMonitor#done()} on the given monitor.
   * <p>
   * In addition, it checks if the monitor is canceled and throws an CoreException with CANCEL_STATUS if this is the
   * cae.
   * </p>
   *
   * @see IProgressMonitor#done())
   */
  public static void done( final IProgressMonitor monitor ) throws CoreException
  {
    if( monitor == null )
      return;

    monitor.done();
    if( monitor.isCanceled() )
      throw new CoreException( Status.CANCEL_STATUS );
  }

}
