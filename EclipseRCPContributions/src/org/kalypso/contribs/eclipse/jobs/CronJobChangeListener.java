/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.contribs.eclipse.jobs;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.kalypso.contribs.eclipse.EclipseRCPContributionsPlugin;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.utils.Debug;

/**
 * This listener will check, if the job has to be rescheduled.
 * 
 * @author Holger Albert
 */
public class CronJobChangeListener extends JobChangeAdapter
{
  /**
   * The constructor.
   */
  public CronJobChangeListener( )
  {
  }

  /**
   * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
   */
  @Override
  public void done( IJobChangeEvent event )
  {
    /* Get the job. */
    Job job = event.getJob();

    /* Is it a cron job? */
    if( !(job instanceof CronJob) )
      return;

    /* Cast. */
    CronJob cronJob = (CronJob) job;

    /* Get the name, mutex string and reschedule delay. */
    String name = cronJob.getName();
    String mutexString = cronJob.getMutexString();
    long rescheduleDelay = cronJob.getRescheduleDelay();

    /* Log. */
    if( Debug.CRON_JOB.isEnabled() )
    {
      EclipseRCPContributionsPlugin.getDefault().getLog().log( StatusUtilities.createInfoStatus( String.format( "The cron job ('%s') has finished execution...", name ) ) );
      EclipseRCPContributionsPlugin.getDefault().getLog().log( cronJob.getResult() );
    }

    /* Don't reschedule, if there is a negative value given. */
    if( rescheduleDelay < 0 )
    {
      /* Log. */
      if( Debug.CRON_JOB.isEnabled() )
        EclipseRCPContributionsPlugin.getDefault().getLog().log( StatusUtilities.createInfoStatus( String.format( "The cron job ('%s') will be deactivated...", name ) ) );

      /* Remove myself as listener. */
      job.removeJobChangeListener( this );

      return;
    }

    /* Log. */
    if( Debug.CRON_JOB.isEnabled() )
      EclipseRCPContributionsPlugin.getDefault().getLog().log( StatusUtilities.createInfoStatus( String.format( "The cron job ('%s') will stay activated with a reschedule delay of %d ms (mutex used: %s)...", name, rescheduleDelay, mutexString ) ) );

    /* Schedule (leave myself as listener). */
    job.schedule( rescheduleDelay );
  }
}