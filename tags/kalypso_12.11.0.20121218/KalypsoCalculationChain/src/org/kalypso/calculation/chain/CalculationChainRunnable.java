package org.kalypso.calculation.chain;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.calculation.chain.i18n.Messages;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.SimulationJobSpecification;
import org.kalypso.simulation.core.refactoring.ISimulationRunner;
import org.kalypso.simulation.core.refactoring.SimulationRunnerFactory;
import org.kalypso.simulation.core.simspec.Modeldata;

public class CalculationChainRunnable implements ICoreRunnableWithProgress
{

  public static enum CHAIN_STATUS
  {
    INIT,
    RUNNING,
    FINISHED
  }

  private final List<SimulationJobSpecification> m_jobSpecificationList = new ArrayList<>();

  private CHAIN_STATUS m_chainStatus;

  private final URL m_context;

  private final ISimulationMonitor m_monitor;

  public CalculationChainRunnable( final URL context )
  {
    this( new ArrayList<SimulationJobSpecification>(), context );
  }

  public CalculationChainRunnable( final List<SimulationJobSpecification> jobSpecificationList, final URL context )
  {
    this( jobSpecificationList, context, null );
  }

  public CalculationChainRunnable( final List<SimulationJobSpecification> jobSpecificationList, final URL context, final ISimulationMonitor monitor )
  {
    m_monitor = monitor;
    m_jobSpecificationList.addAll( jobSpecificationList );
    m_context = context;
    m_chainStatus = CHAIN_STATUS.INIT;
  }

  public void initialize( )
  {
    if( m_chainStatus.compareTo( CHAIN_STATUS.RUNNING ) != 0 )
    {
      m_chainStatus = CHAIN_STATUS.INIT;
      m_jobSpecificationList.clear();
    }
  }

  public void addJob( final SimulationJobSpecification jobSpecification )
  {
    m_jobSpecificationList.add( jobSpecification );
  }

  public void addJob( final int index, final SimulationJobSpecification jobSpecification )
  {
    m_jobSpecificationList.add( index, jobSpecification );
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    m_chainStatus = CHAIN_STATUS.RUNNING;
    IStatus status = Status.OK_STATUS;
    try
    {
      for( final SimulationJobSpecification job : m_jobSpecificationList )
      {
        setTask( String.format( Messages.getString( "CalculationChainRunnable_0" ), job.getDescription() ), monitor ); //$NON-NLS-1$

        if( status.isOK() )
        {
          Logger.getAnonymousLogger().log( Level.INFO, String.format( "Starting calc job: %s [%s]", job.getDescription(), job.getCalculationTypeID() ) ); //$NON-NLS-1$

          final IPath workspace = job.getContainer();
          final IResource workspaceResource = ResourcesPlugin.getWorkspace().getRoot().findMember( workspace );

          URL context;
          // FIXME: use platform constants!
          // FIXME: what is the link between m_context and the workspace resource?
          // If we are working in a tmpDir, why do we still have a workspaceResource?
          if( m_context.toString().startsWith( "platform:/resource//" ) ) //$NON-NLS-1$
          {
            // local processing - project workspace
            context = workspaceResource.getLocationURI().toURL();
          }
          else
          {
            // wps remote "local" processing - m_context points to tmpDir
            context = m_context;
          }

          final Modeldata modeldata = job.getModeldata( m_context );

          final Map<String, Object> inputs = SimulationRunnerFactory.resolveInputs( modeldata.getInput() );
          final List<String> outputs = SimulationRunnerFactory.resolveOutputs( modeldata.getOutput() );

          final ISimulationRunner runner = SimulationRunnerFactory.createRunner( modeldata, context );
          runner.run( inputs, outputs, monitor );
        }
      }

      setTask( String.format( Messages.getString( "CalculationChainRunnable_1" ) ), monitor );
    }
    catch( final Exception e )
    {
      status = StatusUtilities.statusFromThrowable( e );
    }
    m_chainStatus = CHAIN_STATUS.FINISHED;
    return status;
  }

  private void setTask( final String message, final IProgressMonitor monitor )
  {
    if( m_monitor != null )
    {
      m_monitor.setMessage( message );
    }
    else if( monitor != null )
    {
      monitor.setTaskName( message );
    }
  }
}
