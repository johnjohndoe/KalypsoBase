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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

/**
 * This class provides functions for dealing with cron jobs.
 * 
 * @author Holger Albert
 */
public class CronJobUtilities
{
  /**
   * This listener is responsible for rescheduling the cron jobs.
   */
  public static final IJobChangeListener CRON_JOB_CHANGE_LISTENER = new CronJobChangeListener();

  /**
   * The constructor.
   */
  private CronJobUtilities( )
  {
  }

  /**
   * This function reads the extension registry and creates a list of the registered cron jobs. If there are none
   * registered, the list will be emtpy.
   * 
   * @return The list of registered cron jobs.
   */
  public static List<CronJob> getCronJobs( ) throws CoreException
  {
    /* The memory for the results. */
    List<CronJob> jobs = new ArrayList<CronJob>();

    /* Get the extension registry. */
    IExtensionRegistry registry = Platform.getExtensionRegistry();

    /* Get all elements for the extension point. */
    IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.contribs.eclipsercp.cronJobs" );

    /* Create all cron jobs. */
    for( IConfigurationElement element : elements )
    {
      /* Get the identifier. */
      String identifier = element.getAttribute( "identifier" );

      /* Get the name. */
      String name = element.getAttribute( "name" );

      /* Create the cron job. */
      CronJob job = (CronJob) element.createExecutableExtension( "job" );
      job.setIdentifier( identifier );
      job.setName( name );

      /* Only use the job, if it has a valid schedule delay. */
      if( job.getScheduleDelay() >= 0 )
        jobs.add( job );
    }

    return jobs;
  }

  /**
   * This function starts the cron job.
   * 
   * @param job
   *          The cron job.
   * @return A status, indicating, if the cron job was started.
   */
  public static IStatus startCronJob( Job job )
  {
    /* Is it a cron job? */
    if( !(job instanceof CronJob) )
      return StatusUtilities.createWarningStatus( "The job ('" + job.getName() + "') should not be scheduled, because it is no cron job ..." );

    /* Cast. */
    CronJob cronJob = (CronJob) job;

    /* Get the schedule delay. */
    long scheduleDelay = cronJob.getScheduleDelay();

    /* This job should not be started. */
    if( scheduleDelay < 0 )
      return StatusUtilities.createWarningStatus( "The cron job ('" + cronJob.getName() + "') should not be scheduled, due to a negative delay ..." );

    /* Get the job manager. */
    IJobManager jobManager = CronJob.getJobManager();

    /* Search all running cron jobs. */
    Job[] runningCronJobs = jobManager.find( CronJob.CRON_JOB_FAMILY );

    /* If the given cron job is already running, ignore it. */
    for( int i = 0; i < runningCronJobs.length; i++ )
    {
      /* Get running the cron job. */
      Job runningJob = runningCronJobs[i];

      /* Don't handle other jobs, which should happen to have the same family, but are no cron jobs. */
      if( !(runningJob instanceof CronJob) )
        continue;

      /* Cast. */
      CronJob runningCronJob = (CronJob) runningJob;

      /* Our cron job is already running, ignore it. */
      if( runningCronJob.getIdentifier().equals( cronJob.getIdentifier() ) )
        return StatusUtilities.createWarningStatus( "The cron job ('" + cronJob.getName() + "') should not be scheduled, because a cron job with its id is already running ..." );
    }

    /* Okay, he can be started. */

    /* Add the job change listener. */
    /* The listener will remove itself, if the job should not be executed anymore. */
    cronJob.addJobChangeListener( CRON_JOB_CHANGE_LISTENER );

    /* Finally start it. */
    cronJob.schedule( scheduleDelay );

    return StatusUtilities.createInfoStatus( "The cron job ('" + cronJob.getName() + "') was scheduled ..." );
  }

  /**
   * This function cancels all running or scheduled cron jobs.
   */
  public static void cancelAllCronJobs( )
  {
    /* Get the job manager. */
    IJobManager jobManager = CronJob.getJobManager();

    /* Search all running cron jobs. */
    Job[] runningCronJobs = jobManager.find( CronJob.CRON_JOB_FAMILY );

    /* If the given cron job is already running, ignore it. */
    for( int i = 0; i < runningCronJobs.length; i++ )
    {
      /* Get running the cron job. */
      Job runningCronJob = runningCronJobs[i];
      runningCronJob.cancel();
    }
  }
}