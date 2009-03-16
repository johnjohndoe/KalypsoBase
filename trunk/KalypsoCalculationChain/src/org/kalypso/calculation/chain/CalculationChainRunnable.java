package org.kalypso.calculation.chain;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.simulation.ui.calccase.ModelNature;

public class CalculationChainRunnable implements ICoreRunnableWithProgress
{

  public static enum CHAIN_STATUS
  {
    INIT,
    RUNNING,
    FINISHED
  }

  private final List<CalculationChainMemberJobSpecification> m_jobSpecificationList = new ArrayList<CalculationChainMemberJobSpecification>();

  private CHAIN_STATUS m_chainStatus;

  public CalculationChainRunnable( )
  {
    m_chainStatus = CHAIN_STATUS.INIT;
  }

  public CalculationChainRunnable( final List<CalculationChainMemberJobSpecification> jobSpecificationList )
  {
    this();
    m_jobSpecificationList.addAll( jobSpecificationList );
  }

  public void initialize( )
  {
    if( m_chainStatus.compareTo( CHAIN_STATUS.RUNNING ) != 0 )
    {
      m_chainStatus = CHAIN_STATUS.INIT;
      m_jobSpecificationList.clear();
    }
  }

  public void addJob( final CalculationChainMemberJobSpecification jobSpecification )
  {
    m_jobSpecificationList.add( jobSpecification );
  }

  public void addJob( final int index, final CalculationChainMemberJobSpecification jobSpecification )
  {
    m_jobSpecificationList.add( index, jobSpecification );
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException, InvocationTargetException, InterruptedException
  {
    m_chainStatus = CHAIN_STATUS.RUNNING;
    IStatus status = Status.OK_STATUS;
    try
    {
      for( final CalculationChainMemberJobSpecification jobSpecification : m_jobSpecificationList )
      {
        if( status.isOK() )
        {
          System.out.println( jobSpecification.getCalculationTypeID() + " started..." );
          if( jobSpecification.useAntLauncher() )
          {
            final ModelNature nature = (ModelNature) jobSpecification.getContainer().getProject().getNature( ModelNature.ID );
            status = nature.launchAnt( "Progress text", "calc", jobSpecification.getAntProperties(), jobSpecification.getContainer(), monitor );
          }
          else
          {
            if( jobSpecification.useDefaultModelspec() )
            {
              final ModelNature nature = (ModelNature) jobSpecification.getContainer().getProject().getNature( ModelNature.ID );
              status = nature.runCalculation( jobSpecification.getContainer(), monitor );
            }
            else
            {
              status = ModelNature.runCalculation( jobSpecification.getContainer(), monitor, jobSpecification.getModeldata() );
            }
          }
          System.out.println( jobSpecification.getCalculationTypeID() + " finished, status: " + (status.isOK() ? "OK" : "NOT OK") );
          if( !status.isOK() )
          {
            System.out.println( "Status NOT OK, message: " + status.getMessage() );
          }
        }
      }
    }
    catch( final Exception e )
    {
      System.out.println( "ERROR: " + e.getLocalizedMessage() );
      e.printStackTrace();
      // ErrorDialog.openError(shell, "Error",
      // e.getLocalizedMessage(), Status.CANCEL_STATUS);
      status = Status.CANCEL_STATUS;
    }
    m_chainStatus = CHAIN_STATUS.FINISHED;
    return status;
  }
}
