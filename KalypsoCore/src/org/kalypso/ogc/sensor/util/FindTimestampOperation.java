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
package org.kalypso.ogc.sensor.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;

/**
 * This operation finds the timestamp.
 * 
 * @author Holger Albert
 */
public class FindTimestampOperation implements ICoreRunnableWithProgress
{
  /**
   * The timeseries.
   */
  private final IObservation m_observation;

  /**
   * The timestep of the timeseries.
   */
  private final Period m_timestep;

  /**
   * The timestamp or null if this operation was not executed, an error occured or if the timeseries simply has no
   * timestamp.
   */
  private LocalTime m_timestamp;

  /**
   * The constructor.
   * 
   * @param observation
   *          The timeseries.
   * @param timestep
   *          The timestep of the timeseries.
   */
  public FindTimestampOperation( final IObservation observation, final Period timestep )
  {
    m_observation = observation;
    m_timestep = timestep;
    m_timestamp = null;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    try
    {
      /* Guess the timestamp. */
      m_timestamp = TimeseriesUtils.guessTimestamp( m_observation.getValues( null ), m_timestep );

      return new Status( IStatus.OK, KalypsoCorePlugin.getID(), Messages.getString("FindTimestampOperation_0") ); //$NON-NLS-1$
    }
    catch( final SensorException ex )
    {
      ex.printStackTrace();
      return new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), Messages.getString("FindTimestampOperation_1"), ex ); //$NON-NLS-1$
    }
  }

  /**
   * This function returns the timestamp.
   * 
   * @return The timestamp or null if this operation was not executed, an error occured or if the timeseries simply has
   *         no timestamp.
   */
  public LocalTime getTimestamp( )
  {
    return m_timestamp;
  }
}