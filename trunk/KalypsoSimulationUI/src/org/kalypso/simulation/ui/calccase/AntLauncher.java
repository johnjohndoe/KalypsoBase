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
package org.kalypso.simulation.ui.calccase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ant.internal.launching.AntLaunching;
import org.eclipse.ant.internal.launching.AntLaunchingUtil;
import org.eclipse.ant.launching.IAntLaunchConstants;
import org.eclipse.core.externaltools.internal.launchConfigurations.ExternalToolsCoreUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public class AntLauncher
{
  private final IFile m_launchFile;

  private final Properties m_antProperties;

  private ILaunchConfigurationWorkingCopy m_launchConfiguration;

  private File m_logFile;

  public AntLauncher( final IFile launchFile, final Properties antProperties )
  {
    m_launchFile = launchFile;
    m_antProperties = antProperties;
  }

  public void init( ) throws CoreException
  {
    initLaunch();
    initLogFile();
  }

  private void checkInit( )
  {
    if( m_launchConfiguration == null )
      throw new IllegalStateException( "AntLauncher not initialized" );
  }

  @SuppressWarnings("unchecked")
  private void initLaunch( ) throws CoreException
  {
    final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();

    m_launchConfiguration = launchManager.getLaunchConfiguration( m_launchFile ).getWorkingCopy();

    // add user-variables to LaunchConfiguration
    final Map<Object, Object> attribute = m_launchConfiguration.getAttribute( IAntLaunchConstants.ATTR_ANT_PROPERTIES, new HashMap<Object, Object>() ); //$NON-NLS-1$
    attribute.putAll( m_antProperties );
    m_launchConfiguration.setAttribute( IAntLaunchConstants.ATTR_ANT_PROPERTIES, attribute ); //$NON-NLS-1$

    // We do not need an input handler, and also the input handler lives in org.eclipse.ant.ui, we do not want the
    // dependency to that plug-in.
    m_launchConfiguration.setAttribute( AntLaunching.SET_INPUTHANDLER, false );
  }

  private void initLogFile( ) throws CoreException
  {
    checkInit();

    m_logFile = findLogFile();
    if( m_logFile != null )
      m_logFile.getParentFile().mkdirs();
  }

  private File findLogFile( ) throws CoreException
  {
    final String[] arguments = ExternalToolsCoreUtil.getArguments( m_launchConfiguration );

    if( arguments == null )
      return null;
    for( int j = 0; j < arguments.length; j++ )
    {
      if( arguments[j].equals( "-l" ) || arguments[j].equals( "-logfile" ) && j != arguments.length - 1 ) //$NON-NLS-1$ //$NON-NLS-2$
      {
        final String logfile = arguments[j + 1];
        final File logFileFile = new File( logfile );
        return logFileFile;
      }
    }

    return null;
  }

  public File getLogFile( )
  {
    return m_logFile;
  }

  public IStatus execute( final IProgressMonitor monitor ) throws InterruptedException
  {
    checkInit();

    try
    {
      if( AntLaunchingUtil.isLaunchInBackground( m_launchConfiguration ) )
        return executeAndWait( monitor );

      return executeBlocking( monitor );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return e.getStatus();
    }
  }

  private IStatus executeBlocking( final IProgressMonitor monitor ) throws CoreException
  {
    final ILaunch launch = m_launchConfiguration.launch( ILaunchManager.RUN_MODE, monitor );
    Assert.isTrue( launch.isTerminated() );
    return Status.OK_STATUS;
  }

  private IStatus executeAndWait( final IProgressMonitor monitor ) throws InterruptedException, CoreException
  {
    final String name = m_launchConfiguration.getName();
    monitor.beginTask( String.format( "Launching %s", name ), IProgressMonitor.UNKNOWN );

    final ILaunch launch = m_launchConfiguration.launch( ILaunchManager.RUN_MODE, new SubProgressMonitor( monitor, 1000 ) );

    // TODO: timeout konfigurierbar machen?
    final int minutes = 720;
//    monitor.subTask( Messages.getString( "org.kalypso.simulation.ui.calccase.ModelNature.4" ) ); //$NON-NLS-1$
    for( int i = 0; i < 60 * minutes; i++ )
    {
      if( monitor.isCanceled() )
      {
        launch.terminate();
        return Status.CANCEL_STATUS;
      }

      if( launch.isTerminated() )
      {
        final String[] arguments = ExternalToolsCoreUtil.getArguments( m_launchConfiguration );
        if( arguments == null )
          return Status.OK_STATUS;

        return Status.OK_STATUS;
      }
      Thread.sleep( 1000 );
      monitor.worked( 1 );
    }

    // TODO better ask for termination, but continue task in background?
    launch.terminate();

    return StatusUtilities.createStatus( IStatus.WARNING, "Operation wegen Timeout abgebrochen", null );
  }

}
