package org.kalypso.simulation.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Abstract class used by classes extending Job, for easier status manipulation.
 */
public abstract class AbstractInternalStatusJob
{
  private IStatus m_status = new Status( IStatus.INFO, KalypsoSimulationCorePlugin.getID(), "Init" ); //$NON-NLS-1$

  // FIXME: ugly! always set in run method before return, so it is really a return value -> implement run and override with a internalRun method that returns a status
  protected void setStatus( final int severity, final String message )
  {
    m_status = new Status(severity, KalypsoSimulationCorePlugin.getID(), message );
  }

  protected boolean isOkStatus( )
  {
    return m_status.isOK();
  }
}