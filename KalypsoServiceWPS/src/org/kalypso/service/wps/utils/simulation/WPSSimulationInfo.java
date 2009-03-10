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
package org.kalypso.service.wps.utils.simulation;

import java.io.Serializable;
import java.util.List;

import net.opengeospatial.wps.IOValueType;

import org.kalypso.simulation.core.ISimulationConstants;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.SimulationException;

/**
 * Contains the the actual data of a simulation.
 * 
 * @author Gernot Belger (original), Holger Albert (changes)
 */
public class WPSSimulationInfo implements Serializable, ISimulationMonitor
{
  /**
   * The id of the descriped thread (job).
   */
  private String m_id = null;

  /**
   * Description of the job, which has been given at creation time to the service.
   */
  private String m_description;

  /**
   * Der Berechnungstyp des Jobs.
   */
  private String m_type;

  /**
   * Status of the job.
   */
  private ISimulationConstants.STATE m_state = ISimulationConstants.STATE.UNKNOWN;

  /**
   * Progress of the job, between 0 to 100. -1 means: unknown
   */
  private int m_progress = -1;

  /**
   * Description of the job state, if it is an error: the error message.
   */
  private String m_message = "Warte auf Ausführung...";

  /**
   * The result eater that knows about the current results
   */
  private final WPSSimulationResultEater m_resultEeater;

  /**
   * Is displayed from the client after calculation. It is usefull, to give the user the hint to log files, if an error
   * has occured.
   */
  private String m_finishText = "";

  /**
   * State.
   */
  private int m_status = 0; // = IStatus.OK;

  /**
   * The construtor.
   */
  public WPSSimulationInfo( )
  {
    // nur für wscompile
    m_resultEeater = null;
  }

  /**
   * The constructor.
   */
  public WPSSimulationInfo( final String id, final String description, final String type, final ISimulationConstants.STATE state, final int progress, final WPSSimulationResultEater eater )
  {
    m_id = id;
    m_description = description;
    m_state = state;
    m_progress = progress;
    m_type = type;
    m_resultEeater = eater;
  }

  public String getDescription( )
  {
    return m_description;
  }

  public String getId( )
  {
    return m_id;
  }

  /**
   * @see org.kalypso.services.calculation.job.ICalcMonitor#getProgress()
   */
  public int getProgress( )
  {
    return m_progress;
  }

  public ISimulationConstants.STATE getState( )
  {
    return m_state;
  }

  public String getMessage( )
  {
    return m_message;
  }

  public void setDescription( final String string )
  {
    m_description = string;
  }

  public void setId( final String string )
  {
    m_id = string;
  }

  public void setProgress( final int i )
  {
    m_progress = i;
  }

  public void setState( final ISimulationConstants.STATE state )
  {
    m_state = state;
  }

  public void setMessage( final String message )
  {
    m_message = message;
  }

  public final List<IOValueType> getCurrentResults( ) throws SimulationException
  {
    return m_resultEeater.getCurrentResults();
  }

  public final String getType( )
  {
    return m_type;
  }

  public final void setType( String type )
  {
    m_type = type;
  }

  public void cancel( )
  {
    m_state = ISimulationConstants.STATE.CANCELED;
  }

  public boolean isCanceled( )
  {
    return m_state == ISimulationConstants.STATE.CANCELED;
  }

  public String getFinishText( )
  {
    return m_finishText;
  }

  public void setFinishText( String finishText )
  {
    m_finishText = finishText;
  }

  public void setFinishInfo( final int status, final String text )
  {
    setFinishStatus( status );
    setFinishText( text );
  }

  public void setFinishStatus( final int status )
  {
    m_status = status;
  }

  public int getFinishStatus( )
  {
    return m_status;
  }
}