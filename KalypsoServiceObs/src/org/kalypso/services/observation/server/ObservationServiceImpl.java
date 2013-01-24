/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.services.observation.server;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.jws.WebService;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.kalypso.contribs.eclipse.osgi.FrameworkUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.repository.RepositoryException;
import org.kalypso.services.observation.KalypsoServiceObs;
import org.kalypso.services.observation.sei.DataBean;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.sei.ItemBean;
import org.kalypso.services.observation.sei.ObservationBean;
import org.kalypso.services.observation.sei.StatusBean;

/**
 * Kalypso Observation Service.<br/>
 * It delegates to the correct observation service, which is reinitialized after a period of time.
 * 
 * @author Holger Albert
 */
@WebService(endpointInterface = "org.kalypso.services.observation.sei.IObservationService")
public class ObservationServiceImpl implements IObservationService
{
  /**
   * A listener for job change events.
   */
  private final IJobChangeListener m_listener = new JobChangeAdapter()
  {
    @Override
    public void done( final IJobChangeEvent event )
    {
      /* Get the job. */
      final Job job = event.getJob();

      /* Is it the right one. */
      if( !(job instanceof ObservationServiceJob) )
        return;

      /* Handle this event. */
      onJobFinished( event.getResult() );
    }
  };

  /**
   * The observation service job initializes the observation service.
   */
  private Job m_observationServiceJob;

  /**
   * The status of the observation service job.
   */
  private IStatus m_jobStatus;

  /**
   * This variable stores the reinitialize time interval.
   */
  private final long m_interval;

  /**
   * The observation service delegate. It is reloaded after a period of time.
   */
  private IObservationService m_delegate;

  /**
   * The constructor.
   */
  public ObservationServiceImpl( )
  {
    m_observationServiceJob = null;
    m_jobStatus = new Status( IStatus.INFO, KalypsoServiceObs.ID, "Initialisiere Observation-Service" );
    m_delegate = new NullObservationService();
    m_interval = initInterval();

    initObservationServiceJob();
  }

  private long initInterval( )
  {
    try
    {
      final String reinitStr = FrameworkUtilities.getProperty( KalypsoServiceObs.SYSPROP_REINIT_SERVICE, "600000" ); //$NON-NLS-1$
      return Long.parseLong( reinitStr );
    }
    catch( final NumberFormatException e )
    {
      e.printStackTrace();
      return -1;
    }
  }

  private void initObservationServiceJob( )
  {
    /* Create the observation service job. */
    m_observationServiceJob = new ObservationServiceJob( this );
    m_observationServiceJob.addJobChangeListener( m_listener );

    /* Schedule it. */
    // TRICKY: Give a bit of time for first schedule,
    // as this will access a HttpResource, which may not be accessible right now.
    m_observationServiceJob.schedule( 5000 );
  }

  @Override
  public ObservationBean adaptItem( final ItemBean ib ) throws SensorException
  {
    final IObservationService delegate = getDelegate();
    return delegate.adaptItem( ib );
  }

  @Override
  public void clearTempData( final String dataId ) throws SensorException
  {
    final IObservationService delegate = getDelegate();
    delegate.clearTempData( dataId );
  }

  @Override
  public int getServiceVersion( ) throws RemoteException
  {
    final IObservationService delegate = getDelegate();
    return delegate.getServiceVersion();
  }

  @Override
  public DataBean readData( final String href ) throws SensorException
  {
    final IObservationService delegate = getDelegate();
    return delegate.readData( href );
  }

  @Override
  public ItemBean findItem( final String id ) throws RepositoryException
  {
    final IObservationService delegate = getDelegate();
    return delegate.findItem( id );
  }

  @Override
  public ItemBean[] getChildren( final ItemBean parent ) throws RepositoryException
  {
    final IObservationService delegate = getDelegate();
    return delegate.getChildren( parent );
  }

  @Override
  public boolean hasChildren( final ItemBean parent ) throws RepositoryException
  {
    final IObservationService delegate = getDelegate();
    return delegate.hasChildren( parent );
  }

  @Override
  public void reload( )
  {
    /* When the client requests a reload, do nothing, because every short period of time it is reloaded automatically. */
    /* The client should only refresh itself. */

    // /* Cancel the old one. */
    // m_observationServiceJob.cancel();
    // m_observationServiceJob.removeJobChangeListener( m_listener );
    //
    // /* Set to null, so all requests will wait, not only these after the first initializing. */
    // m_delegate = new NullObservationService();
    //
    // /* Reschedule it with no delay. */
    // m_observationServiceJob = new ObservationServiceJob( this );
    // m_observationServiceJob.addJobChangeListener( m_listener );
    // m_observationServiceJob.schedule();
  }

  /**
   * This function returns the delegate and waits for it to be initialized, if neccessary. If the initialization fails,
   * the result could be a {@link NullObservationService}.
   * 
   * @return The delegate or a {@link NullObservationService}.
   */
  protected IObservationService getDelegate( )
  {
    final IObservationService delegate = getDelegateInternal();
    if( !(delegate instanceof NullObservationService) )
      return delegate;

    try
    {
      /* Wait for the job, if he has finished loading. */
      m_observationServiceJob.join();
    }
    catch( final InterruptedException ex )
    {
      ex.printStackTrace();
    }

    return getDelegateInternal();
  }

  /**
   * This function returns the delegate or null, if not initialized.
   * 
   * @return The delegate or null, if not initialized.
   */
  protected synchronized IObservationService getDelegateInternal( )
  {
    return m_delegate;
  }

  /**
   * This function sets the delegate.
   * 
   * @param delegate
   *          The delegate.
   */
  synchronized void setDelegate( final IObservationService delegate )
  {
    if( delegate == null )
    {
      m_delegate = new NullObservationService();
      return;
    }

    m_delegate = delegate;
  }

  /**
   * This function is executed, if the job has finished (i.e. done, canceled, failure).
   * 
   * @param status
   *          The status of the job.
   */
  protected void onJobFinished( final IStatus status )
  {
    /* Store the status. */
    m_jobStatus = status;

    /* If the status is not okay, log it and reschedule the job. */
    if( !status.isOK() )
    {
      /* Log the error. */
      final KalypsoServiceObs plugin = KalypsoServiceObs.getDefault();
      final ILog log = plugin.getLog();
      log.log( status );

      /* Reschedule the job, regardless of the interval setting. */
      m_observationServiceJob.schedule( 150000 );

      return;
    }

    /* Reschedule the job. */
    if( m_interval > 0 )
      m_observationServiceJob.schedule( m_interval );
  }

  @Override
  public void makeItem( final String identifier ) throws RepositoryException
  {
    final IObservationService delegate = getDelegate();
    delegate.makeItem( identifier );
  }

  @Override
  public void deleteItem( final String identifier ) throws RepositoryException
  {
    final IObservationService delegate = getDelegate();
    delegate.deleteItem( identifier );
  }

  @Override
  public void setItemData( final String identifier, final Object serializable ) throws RepositoryException
  {
    if( !(serializable instanceof Serializable) )
      throw new NotImplementedException();

    final IObservationService delegate = getDelegate();
    delegate.setItemData( identifier, serializable );
  }

  @Override
  public void setItemName( final String identifier, final String name ) throws RepositoryException
  {
    final IObservationService delegate = getDelegate();
    delegate.setItemName( identifier, name );
  }

  @Override
  public boolean isMultipleSourceItem( final String identifier ) throws RepositoryException
  {
    final IObservationService delegate = getDelegate();
    return delegate.isMultipleSourceItem( identifier );
  }

  @Override
  public StatusBean getStatus( final String type )
  {
    /* If the job had an error, return this status. */
    if( !m_jobStatus.isOK() )
      return new StatusBean( m_jobStatus );

    /* Otherwise ask the delegate. */
    final IObservationService delegate = getDelegate();
    return delegate.getStatus( type );
  }

  @Override
  public ItemBean getParent( final String identifier ) throws RepositoryException
  {
    final IObservationService delegate = getDelegate();
    return delegate.getParent( identifier );
  }
}