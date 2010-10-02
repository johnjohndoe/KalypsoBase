package org.kalypso.project.database.client.ui.composites;

import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.program.Program;

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
      event.doit = false;
      // TODO: check, if this is always ok
      Program.launch( event.location );
    }

    m_firstEventReceived = true;
  }
}