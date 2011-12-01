package org.kalypso.util.command;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;

/**
 * An operation that waits until all feature changes have been committed.
 * 
 * @author Gernot Belger
 */
public final class WaitForFeatureChanges implements ICoreRunnableWithProgress
{
  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws InterruptedException
  {
    final IJobManager manager = Job.getJobManager();
    manager.join( CommandJob.FAMILY, monitor );
    return Status.OK_STATUS;
  }
}