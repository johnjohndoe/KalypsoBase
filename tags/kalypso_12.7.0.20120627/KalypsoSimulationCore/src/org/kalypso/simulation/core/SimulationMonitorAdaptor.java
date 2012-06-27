package org.kalypso.simulation.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.simulation.core.i18n.Messages;

public class SimulationMonitorAdaptor implements IProgressMonitor
{
  private final ISimulationMonitor m_monitor;

  private double m_worked;

  private double m_totalWork;

  public SimulationMonitorAdaptor( final ISimulationMonitor monitor )
  {
    m_monitor = monitor;
  }

  @Override
  public void beginTask( final String name, final int totalWork )
  {
    m_totalWork = totalWork;
    m_monitor.setMessage( name );
    m_monitor.setProgress( 0 );
  }

  public void done( final IStatus status )
  {
    m_monitor.setProgress( 100 );
    m_monitor.setFinishInfo( status.getSeverity(), status.getMessage() ); //$NON-NLS-1$
  }

  @Override
  public void done( )
  {
    done( new Status( IStatus.OK, KalypsoSimulationCorePlugin.getID(), -1, Messages.getString( "org.kalypso.simulation.core.SimulationMonitorAdaptor.0" ), null ) ); //$NON-NLS-1$
  }

  @Override
  public void internalWorked( final double work )
  {
  }

  @Override
  public boolean isCanceled( )
  {
    return m_monitor.isCanceled();
  }

  @Override
  public void setCanceled( final boolean value )
  {
    if( value )
      m_monitor.cancel();
  }

  @Override
  public void setTaskName( final String name )
  {
    m_monitor.setMessage( name );
  }

  @Override
  public void subTask( final String name )
  {
    m_monitor.setMessage( name );
  }

  @Override
  public void worked( final int work )
  {
    m_monitor.setProgress( (int) (100 * (m_worked += work) / m_totalWork) );
  }

}
