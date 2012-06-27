package org.kalypso.simulation.core;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

public abstract class AbstractInternalStatusJob
{
  /**
   * Abstract class used by classes extending Job, for easier status manipulation.
   */

  private IStatus m_status = StatusUtilities.createInfoStatus( "Init", new Object[0] ); //$NON-NLS-1$

  protected static enum STATUS
  {
    OK,
    INFO,
    ERROR
  }

  protected void setStatus( final STATUS status, final String message )
  {
    switch( status )
    {
      case OK:
        m_status = StatusUtilities.createOkStatus( message );
        break;
      case INFO:
        m_status = StatusUtilities.createInfoStatus( message );
        break;
      default:
        m_status = StatusUtilities.createErrorStatus( message );
        break;
    }
  }

  protected boolean isOkStatus( )
  {
    return m_status.isOK();
  }

}
