package org.kalypso.debug.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin
{
  // The plug-in ID
  public static final String PLUGIN_ID = "org.kalypso.debug.servlet";

  // The shared instance
  private static Activator THE_PLUGIN;

  /**
   * The constructor
   */
  public Activator( )
  {
    THE_PLUGIN = this;
  }

  /**
   * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
  }

  /**
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    THE_PLUGIN = null;
    super.stop( context );
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault( )
  {
    return THE_PLUGIN;
  }

  public void appendLogContent( final PrintWriter pw ) throws IOException
  {
    final File logFile = Platform.getLogFileLocation().toFile();
    if( !logFile.exists())
    {
      pw.println( "No log file available" );
      return;
    }
    
    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader( new FileReader( logFile ) );
      while( reader.ready() )
      {
        final String line = reader.readLine();
        if( line == null )
          break;

        pw.println( line );
      }
    }
    finally
    {
      if( reader != null )
        reader.close();
      pw.flush();
    }
  }
}
