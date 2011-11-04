package de.openali.odysseus.service.ods.util;

import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.logging.impl.Logger;

/**
 * Helper class for accessing a display from server-side Eclipse. At first, a new thread is created which is only
 * running the Display. Every object using graphics in server context should use the Display from this class.
 * 
 * @author Alexander Burtscher, Holger Albert
 */
public class DisplayHelper extends Thread
{
  /**
   * The display.
   */
  private Display m_display;

  /**
   * True, if the event loop should keep running.
   */
  private boolean m_runEventLoop;

  /**
   * The instance.
   */
  private static DisplayHelper INSTANCE = null;

  /**
   * The constructor.
   */
  private DisplayHelper( )
  {
    super( "ODS Display Thread" );

    m_display = null;
    m_runEventLoop = false;

    start();
  }

  /**
   * This function returns the instance of the display helper.
   * 
   * @return The instance of the display helper.
   */
  public static synchronized DisplayHelper getInstance( )
  {
    if( INSTANCE == null )
      INSTANCE = new DisplayHelper();

    return INSTANCE;
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run( )
  {
    /* Create display. */
    if( Display.getCurrent() != null )
    {
      m_display = Display.getCurrent();
      Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Using current display." );
    }

    if( m_display == null )
    {
      m_display = new Display();
      if( m_display.getThread() != Thread.currentThread() )
        Logger.logError( Logger.TOPIC_LOG_GENERAL, String.format( "Another thread (%s) has the display. Maybe you should check that the ods plugin is the first plugin to open the display.", m_display.getThread().getName() ) );
    }

    /* Run the event loop. */
    runEventLoop( m_display );
  }

  /**
   * This function returns the display.
   * 
   * @return The display.
   */
  public synchronized Display getDisplay( )
  {
    /* Wait until display is initialized. */
    while( m_display == null )
    {
      try
      {
        Thread.sleep( 10 );
      }
      catch( final InterruptedException e )
      {
        e.printStackTrace();
      }
    }

    return m_display;
  }

  /**
   * This function disposes the display helper.
   */
  public synchronized void dispose( )
  {
    m_runEventLoop = false;

    if( m_display != null && !m_display.isDisposed() )
      m_display.dispose();
    m_display = null;
  }

  /**
   * This function runs an event loop for the workbench.
   * 
   * @param display
   *          The display.
   */
  private void runEventLoop( final Display display )
  {
    m_runEventLoop = true;
    while( m_runEventLoop )
    {
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
}