package org.kalypso.contribs.eclipse.ui.browser.commandable;

import java.net.URL;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.kalypso.contribs.eclipse.ui.browser.AbstractBrowserView;

/**
 * BrowserView that can handle and excecute CommandURLs via CommandLocationListener<br>
 * 
 * @author kuepfer
 */
public class CommandURLBrowserView extends AbstractBrowserView
{
  public static final String WEB_BROWSER_VIEW_ID = "org.kalypso.contribs.ui.browser.commandable.CommandURLBrowserView"; //$NON-NLS-1$

  protected CommandLocationListener m_listener = new CommandLocationListener( this );

  public CommandURLBrowserView( )
  {
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.browser.AbstractBrowserView#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( Composite parent )
  {
    super.createPartControl( parent );
    addLocationListener( m_listener );
  }

  protected Browser getBrowser( )
  {
    return m_viewer.getBrowser();
  }

  protected URL getContext( )
  {
    return m_context;
  }

  public void setUrl( final String url )
  {

    super.setURL( url );
  }
}
