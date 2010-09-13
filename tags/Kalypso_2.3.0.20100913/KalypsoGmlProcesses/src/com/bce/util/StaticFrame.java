package com.bce.util;

import javax.swing.JFrame;

/**
 * @author belger
 */
public class StaticFrame extends JFrame
{
  private static JFrame THE_FRAME = new StaticFrame();

  public static JFrame getStaticFrame( )
  {
    return THE_FRAME;
  }

  private StaticFrame( )
  {
    // singleton
  }

  @SuppressWarnings("deprecation") //$NON-NLS-1$
  @Override
  public void show( )
  {
    // This frame will never be shown
  }

  @Override
  public synchronized void dispose( )
  {
    try
    {
      getToolkit().getSystemEventQueue();
      super.dispose();
    }
    catch( final Exception e )
    {
      // untrusted code not allowed to dispose
    }
  }
}
