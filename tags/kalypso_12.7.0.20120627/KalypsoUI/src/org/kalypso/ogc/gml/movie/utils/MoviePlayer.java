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
package org.kalypso.ogc.gml.movie.utils;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.kalypso.ogc.gml.movie.IMovieImageProvider;
import org.kalypso.ogc.gml.movie.controls.MovieComposite;

/**
 * The movie player.
 * 
 * @author Holger Albert
 */
public class MoviePlayer
{
  /**
   * The movie image provider.
   */
  protected IMovieImageProvider m_imageProvider;

  /**
   * The parent movie composite.
   */
  protected MovieComposite m_parent;

  /**
   * The movie player job.
   */
  protected MoviePlayerJob m_job;

  /**
   * The frame delay.
   */
  private int m_frameDelay;

  /**
   * The constructor.
   * 
   * @param imageProvider
   *          The movie image provider.
   */
  public MoviePlayer( final IMovieImageProvider imageProvider )
  {
    m_imageProvider = imageProvider;
    m_parent = null;
    m_job = null;
    m_frameDelay = 250;
  }

  /**
   * This function initializes the player.
   * 
   * @param parent
   *          The parent movie composite.
   */
  public void initialize( final MovieComposite parent )
  {
    m_parent = parent;
  }

  public void updateControls( )
  {
    if( m_parent == null || m_parent.isDisposed() )
      return;

    m_parent.updateControls();
  }

  public synchronized void start( )
  {
    if( m_job != null )
      return;

    m_job = new MoviePlayerJob( this );
    m_job.setSystem( false );
    m_job.setUser( false );
    m_job.setPriority( Job.LONG );
    m_job.addJobChangeListener( new JobChangeAdapter()
    {
      /**
       * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
       */
      @Override
      public void done( final IJobChangeEvent event )
      {
        handleJobStopped();
      }
    } );

    m_job.schedule();
  }

  public synchronized void stop( )
  {
    if( m_job == null )
      return;

    m_job.cancel();
  }

  protected synchronized void handleJobStopped( )
  {
    m_job = null;
  }

  public IMovieImageProvider getImageProvider( )
  {
    return m_imageProvider;
  }

  public IMovieFrame getCurrentFrame( )
  {
    return m_imageProvider.getCurrentFrame();
  }

  public void stepTo( final int step )
  {
    m_imageProvider.stepTo( step );
  }

  public void stepAndWait( final int step )
  {
    final MovieResolution resolution = m_parent.getResolution();
    m_imageProvider.stepAndWait( step, resolution.getWidth(), resolution.getHeight() );
  }

  public int getCurrentStep( )
  {
    return m_imageProvider.getCurrentStep();
  }

  public int getEndStep( )
  {
    return m_imageProvider.getEndStep();
  }

  public void dispose( )
  {
    if( m_job != null )
      stop();

    if( m_imageProvider != null )
      m_imageProvider.dispose();

    m_imageProvider = null;
    m_parent = null;
    m_job = null;
    m_frameDelay = 250;
  }

  public void updateFrameDelay( final int frameDelay )
  {
    m_frameDelay = frameDelay;
  }

  public int getFrameDelay( )
  {
    return m_frameDelay;
  }
}