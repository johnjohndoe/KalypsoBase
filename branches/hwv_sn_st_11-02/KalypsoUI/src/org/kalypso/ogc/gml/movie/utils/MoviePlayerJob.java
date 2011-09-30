/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * This jobs plays the movie.
 * 
 * @author Holger Albert
 */
public class MoviePlayerJob extends Job
{
  /**
   * The movie player.
   */
  private final MoviePlayer m_player;

  /**
   * The constructor.
   * 
   * @param player
   *          The movie player.
   */
  public MoviePlayerJob( final MoviePlayer player )
  {
    super( "MoviePlayer" );

    m_player = player;
  }

  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    try
    {
      while( true )
      {
        final long startTime = System.currentTimeMillis();

        /* Monitor. */
        if( monitor.isCanceled() )
          return new Status( IStatus.CANCEL, KalypsoGisPlugin.getId(), "Abbruch..." );

        /* Get the current step. */
        final int currentStep = m_player.getCurrentStep();

        /* Step and wait. */
        m_player.stepAndWait( currentStep + 1 );

        /* Update the controls. */
        m_player.updateControls();

        /* Wait a bit, if the delay was shorter than the set waiting time */
        final long endTime = System.currentTimeMillis();
        final long delay = endTime - startTime;
        final int frameDelay = m_player.getFrameDelay();
        final long waitTime = frameDelay - delay;

        if( waitTime > 0 )
          Thread.sleep( waitTime );
      }
    }
    catch( final Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex );
    }
  }
}