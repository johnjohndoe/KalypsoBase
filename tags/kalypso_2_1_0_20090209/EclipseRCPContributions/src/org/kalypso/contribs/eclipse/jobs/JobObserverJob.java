/** This file is part of Kalypso
 *
 *  Copyright (c) 2008 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.kalypso.contribs.java.JavaApiContributionsPlugin;

/**
 * A job that observes a given job and calls a 'jobRunning' method while the observed job is running and a 'jobDone'
 * method when finished.<br>
 * This job registers/unregisters itself on the given job and finishs automatically when the observed job is done.
 *
 * @author Gernot Belger
 */
public abstract class JobObserverJob extends Job
{
  private final IJobChangeListener m_jobListener = new JobChangeAdapter()
  {
    /**
     * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void done( final IJobChangeEvent event )
    {
      handleObservedJobDone( event.getResult() );
    }
  };

  private final long m_sleepMillis;

  private final Job m_observedJob;

  /**
   * @param name
   *          See {@link Job#Job(String)}
   * @param sleepMillis
   *          Time in milliseconds between two calls to work. The thread of this job is sent to sleep wile doing
   *          nothing.
   */
  public JobObserverJob( final String name, final Job observedJob, final long sleepMillis )
  {
    super( name );

    m_observedJob = observedJob;
    m_sleepMillis = sleepMillis;

    m_observedJob.addJobChangeListener( m_jobListener );
  }

  /**
   * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    while( !monitor.isCanceled() )
    {
      try
      {
        Thread.sleep( m_sleepMillis );

        if( m_observedJob.getState() == Job.RUNNING )
          jobRunning();
      }
      catch( final InterruptedException e )
      {
        return new Status( IStatus.ERROR, JavaApiContributionsPlugin.getDefault().getBundle().getSymbolicName(), "repaint thread was interrupted", e ); //$NON-NLS-1$
      }
    }

    return Status.OK_STATUS;
  }

  /**
   * Called repeatedly while the given job is running.<br>
   * Default implementation does nothing. Intended to be overwritten.
   */
  protected void jobRunning( )
  {
  }

  /**
   * Called when the observed job has finished.<br>
   * Default implementation call {@link #jobRunning()}. Intended to be overwritten.
   */
  protected void jobDone( @SuppressWarnings("unused") final IStatus result )
  {
    jobRunning();
  }

  protected void handleObservedJobDone( final IStatus result )
  {
    cancel();
    m_observedJob.removeJobChangeListener( m_jobListener );

    jobDone( result );
  }
}