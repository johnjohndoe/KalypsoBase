package org.kalypso.contribs.eclipse.swt.widgets;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Button;

/**
 * Helper class to dispose the image of a button once the button gets disposed.<br>
 * Usage: <code>
 * Button b = new Button( parent, SWT.PUSH );
 * b.setImage( new Image() );
 * DisposeButtonImageListener.hookToButton( b );
 * </code>
 * 
 * @author Gernot Belger
 */
public class DisposeButtonImageListener implements DisposeListener
{
  public static void hookToButton( final Button b )
  {
    b.addDisposeListener( new DisposeButtonImageListener( b ) );
  }

  private Button m_button;

  public DisposeButtonImageListener( final Button button )
  {
    m_button = button;
  }

  @Override
  public void widgetDisposed( final DisposeEvent e )
  {
    m_button.getImage().dispose();
    m_button = null;
  }
}