package de.openali.odysseus.service.ods.util;

import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.logging.impl.Logger;

/**
 * @author burtscher Helper class for accessing a Display from Server-Side Eclipse; at first, a new thread is created
 *         which is only running the Display; every object using graphics in server context should use the Display from
 *         this class
 */
public class DisplayHelper extends Thread
{
  private Display m_display = null;

  private boolean m_runEventLoop = false;

  private static DisplayHelper m_dh = null;

  private DisplayHelper( )
  {
    super( "ODS display thread" );

    start();
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run( )
  {
    // create display
    if( Display.getCurrent() != null )
    {
      m_display = Display.getCurrent();
      Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "using current display" );
    }

    if( m_display == null )
    {
      m_display = Display.getDefault();

      if( m_display.getThread() != Thread.currentThread() )
        Logger.logError( Logger.TOPIC_LOG_GENERAL, "Another thread has the display. Maybe you should check that the ods plugin is the first plugin to open the display. "
            + m_display.getThread().getName() );
    }
    runEventLoop( m_display );

  }

  public static synchronized DisplayHelper getInstance( )
  {
    if( m_dh == null )
      m_dh = new DisplayHelper();

    return m_dh;
  }

  public Display getDisplay( )
  {
    // Wait until display is initialized
    // DIRTY?
    while( m_display == null )
      try
      {
        Thread.sleep( 10 );
      }
      catch( final InterruptedException e )
      {
        e.printStackTrace();
      }

    return m_display;
  }

  public void dispose( )
  {
    m_runEventLoop = false;

    if( (m_display != null) && !m_display.isDisposed() )
      m_display.dispose();
  }

  /*
   * Runs an event loop for the workbench.
   */
  private void runEventLoop( final Display display )
  {
    m_runEventLoop = true;
    while( m_runEventLoop )
      try
      {
        if( !display.readAndDispatch() )
          display.sleep();
      }
      catch( final Throwable t )
      {
        t.printStackTrace();
      }
  }

}
