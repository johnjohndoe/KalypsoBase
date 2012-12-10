package org.kalypso.project.database.client.ui.composites;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * Blocks changing events from a {@link org.eclipse.swt.browser.Browser} and opens the link instead in the systems
 * default browser.
 * 
 * @author Gernot Belger
 */
final class OpenExternalLocationAdapter extends LocationAdapter
{
  private boolean m_firstEventReceived = true;

  public OpenExternalLocationAdapter( final boolean ignoreFirstEvent )
  {
    if( ignoreFirstEvent )
      m_firstEventReceived = false;
  }

  @Override
  public void changing( final LocationEvent event )
  {
    // TRICKY: we sometimes do not want to react the first event
    // as probably the used browser is still (asyn) loading its own content.
    if( m_firstEventReceived == true )
    {
      try
      {
        event.doit = false;

        final IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
        final IWebBrowser externalBrowser = browserSupport.getExternalBrowser();
        externalBrowser.openURL( new URL( event.location ) );
      }
      catch( final PartInitException e )
      {
        e.printStackTrace();
        final String msg = "Failed to open external browser. Please check Kalypso settings.";
        MessageDialog.openError( event.widget.getDisplay().getActiveShell(), "Open Browser", msg );
      }
      catch( final MalformedURLException e )
      {
        e.printStackTrace();
        final String msg = String.format( "Bad location: %s", event.location );
        MessageDialog.openError( event.widget.getDisplay().getActiveShell(), "Open Browser", msg );
      }
    }

    m_firstEventReceived = true;
  }
}