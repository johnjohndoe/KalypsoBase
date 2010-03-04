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

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.contribs.eclipse.EclipseRCPContributionsPlugin;

/**
 * This is a default implementation for a cron job.
 * 
 * @author Holger Albert
 */
public abstract class CronJob extends Job
{
  /**
   * The family for this jobs.
   */
  public static final String CRON_JOB_FAMILY = "CronJobFamily";

  /**
   * The identifier of this job.
   */
  private String m_identifier;

  /**
   * The mutex string.
   */
  private String m_mutexString;

  /**
   * The constructor.<br>
   * <br>
   * The identifier of this job by default will be "CronJob". <br>
   * <br>
   * If you are using a cron job and you want to start it manually via CronJobUtilities.startCronJob(job), then you must
   * make sure to set the identifier to a unique one. Only one cron job of more with the same identifier will be
   * executed.<br>
   * <br>
   * If you are using the extension point <strong>org.kalypso.contribs.eclipsercp.cronJobs</strong>, also be sure to use
   * an identifier only once.
   */
  public CronJob( )
  {
    super( "CronJob" );

    m_identifier = "CronJob";
    m_mutexString = "none";

    setUser( false );
    setSystem( true );
    setPriority( Job.LONG );
  }

  /**
   * This function sets the identifier of this job.<br>
   * <br>
   * <strong>Note:</strong><br>
   * If there are more then one job with the same identifier (which should never happen, by the way), only one of them
   * can run at a time.
   * 
   * @param identifier
   *          The identifier of this job.
   */
  public void setIdentifier( String identifier )
  {
    m_identifier = identifier;
  }

  /**
   * This function returns the identifier of this job.<br>
   * <br>
   * <strong>Note:</strong><br>
   * If there are more then one jobs with the same identifier (which should never happen, by the way), only one of them
   * will be handled.
   * 
   * @return The identifier of this job.
   */
  public String getIdentifier( )
  {
    return m_identifier;
  }

  /**
   * This function sets the mutex string.<br>
   * <br>
   * <strong>Note:</strong><br>
   * A scheduling rule (a mutex) will be set, using the given mutex string.
   * 
   * @see EclipseRCPContributionsPlugin#getCronJobMutex(String)
   * @param mutexString
   *          The mutex string.
   */
  public void setMutexString( String mutexString )
  {
    /* Reset the mutex string. */
    m_mutexString = "none";

    /* Reset the mutex for this cron job. */
    setRule( null );

    if( mutexString != null && mutexString.length() > 0 )
    {
      /* Store the mutex string. */
      m_mutexString = mutexString;

      /* Get the mutex for this cron job. */
      ISchedulingRule mutex = EclipseRCPContributionsPlugin.getDefault().getCronJobMutex( mutexString );

      /* Set the mutex for this cron job. */
      setRule( mutex );
    }
  }

  /**
   * This function returns the mutex string.<br>
   * <br>
   * <strong>Note:</strong><br>
   * A scheduling rule (a mutex) was set, using the returned mutex string.
   * 
   * @see EclipseRCPContributionsPlugin#getCronJobMutex(String)
   * @return The mutex string.
   */
  public String getMutexString( )
  {
    return m_mutexString;
  }

  /**
   * This function should return the delay in miliseconds for this job to be executed the first time.<br>
   * <br>
   * <strong>Note:</strong><br>
   * A negative delay will result in ignoring this job (why implement the job then?).
   * 
   * @return The delay in miliseconds for this job to be executed the first time.
   */
  public abstract long getScheduleDelay( );

  /**
   * This function should return the delay in miliseconds for this job to be rescheduled, after he has finished.<br>
   * <br>
   * <strong>Note:</strong><br>
   * A negative delay will result in never reschulding this job, after he has finished once.
   * 
   * @return The delay in miliseconds for this job to be rescheduled, after he has finished.
   */
  public abstract long getRescheduleDelay( );

  /**
   * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
   */
  @Override
  public final boolean belongsTo( Object family )
  {
    return CRON_JOB_FAMILY.equals( family );
  }
}