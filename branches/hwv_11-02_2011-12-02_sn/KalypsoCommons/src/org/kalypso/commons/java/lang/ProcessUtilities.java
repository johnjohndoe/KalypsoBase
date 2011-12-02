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
package org.kalypso.commons.java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.kalypso.contribs.java.io.StreamGobbler;

/**
 * This class contains some additional helper function in addition to {@link ProcessHelper}.
 * 
 * @author Holger Albert
 */
public class ProcessUtilities
{
  /**
   * The time (2.5 minutes) to wait after the timeout was reached once.
   */
  private static final int WAITING_TIME = 150000;

  /**
   * The constructor.
   */
  private ProcessUtilities( )
  {
  }

  /**
   * This function creates the environment for the process.
   * 
   * @param chgEnv
   *          The evironment variables that should be set. May be ones to be replaced or new ones. May be null.
   * @return The environment for the process.
   */
  public static String[] createEnvironment( Map<String, String> chgEnv )
  {
    /* Get the environment variables. */
    Map<String, String> sysEnv = System.getenv();

    /* Rebuild the environment variables in a new map. */
    Map<String, String> newEnv = new HashMap<String, String>();
    for( String key : sysEnv.keySet() )
      newEnv.put( key, sysEnv.get( key ) );

    /* Change with the given environment variables. */
    if( chgEnv != null && chgEnv.size() > 0 )
    {
      /* Existing ones will be replaced, the others will be added. */
      for( String key : chgEnv.keySet() )
        newEnv.put( key, chgEnv.get( key ) );
    }

    /* Build an array. */
    List<String> listEnv = new ArrayList<String>();
    for( String key : newEnv.keySet() )
      listEnv.add( String.format( "%s=%s", key, newEnv.get( key ) ) );

    return listEnv.toArray( new String[] {} );
  }

  public static int executeProcess( String[] cmdLine, String[] env, File directory, long timeout, boolean doNotAskOnTimeout, boolean debug, IProgressMonitor monitor ) throws TimeoutException, IOException
  {
    /* If no monitor was given, create a null progress monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    /* Exit value and actual running time. */
    int exitValue;
    try
    {
      /* Monitor. */
      monitor.beginTask( "", 200 );
      monitor.subTask( "" );

      /* Execute the process. */
      Process exec = Runtime.getRuntime().exec( cmdLine, env, directory );

      /* Create the input streams. */
      InputStream errorStream = exec.getErrorStream();
      InputStream inputStream = exec.getInputStream();

      /* Create the stream gooblers. */
      StreamGobbler error = new StreamGobbler( errorStream, "ERROR_STREAM", debug );
      StreamGobbler input = new StreamGobbler( inputStream, "INPUT_STREAM", debug );

      /* Start the stream gobblers. */
      error.start();
      input.start();

      exitValue = 0;
      int timeRunning = 0;

      /* It is running until the job has finished or the timeout is reached. */
      while( true )
      {
        try
        {
          exitValue = exec.exitValue();
          break;
        }
        catch( RuntimeException e )
        {
          /* The process has not yet finished. */
        }

        /* When the process should be stopped, destroy the process. */
        if( monitor.isCanceled() )
        {
          exec.destroy();
          exitValue = exec.exitValue();
          break;
        }

        if( timeRunning >= timeout )
        {
          /* Ask the user, if he wants to wait a bit more. */
          if( doNotAskOnTimeout || !askUser( Display.getCurrent(), timeout ) )
          {
            /* If not, destroy the process. */
            exec.destroy();
            throw new TimeoutException( "Das Timeout wurde erreicht..." );
          }

          /* If he wants to wait a bit more, reduce the time running variable. */
          timeRunning = timeRunning - WAITING_TIME;
        }

        /* Wait a few milliseconds, before continueing. */
        try
        {
          Thread.sleep( 100 );
        }
        catch( InterruptedException ex )
        {
          /* Should not happen. */
          ex.printStackTrace();
        }

        /* Increase the running time. */
        timeRunning = timeRunning + 100;
      }

      return exitValue;
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }

  /**
   * This function asks the user, if he want to wait a bit longer, because the timeout has been reached.
   * 
   * @param display
   *          The display or null.
   * @param timeout
   *          The timeout.
   * @return True, if the user wants to wait.
   */
  private static boolean askUser( final Display display, final long timeout )
  {
    /* Check the display. */
    if( display == null )
      return false;

    /* The result. */
    final boolean[] result = new boolean[1];

    /* Display the message dialog. */
    display.syncExec( new Runnable()
    {
      /**
       * @see java.lang.Runnable#run()
       */
      @Override
      public void run( )
      {
        /* The title. */
        String title = "Prozessausführung";

        /* The message. */
        String message = String.format( "Das Timeout von %s Minuten wurde überschritten.", String.valueOf( timeout / 1000 / 60 ) );
        String message1 = String.format( "Möchten Sie noch einmal %s Minuten warten?", String.valueOf( WAITING_TIME / 1000 / 60 ) );

        /* Open the question. */
        result[0] = MessageDialog.openQuestion( display.getActiveShell(), title, String.format( "%s%n%s", message, message1 ) );
      }
    } );

    return result[0];
  }
}