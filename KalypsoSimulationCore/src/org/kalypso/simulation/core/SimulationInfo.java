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
package org.kalypso.simulation.core;

import java.io.Serializable;

import org.kalypso.simulation.core.i18n.Messages;

/**
 * <p>
 * Enth�lt die aktuellen Daten eines {@link org.kalypso.services.calculation.job.ICalcJob}
 * </p>
 * 
 * @author Belger
 */
public class SimulationInfo implements Serializable, ISimulationMonitor
{
  /** ID des beschriebenen Jobs */
  private String m_id = null;

  /**
   * Textuelle Beschreibung des Jobs, beim Erzeugen des Jobs an den Service �bergeben
   */
  private String m_description;

  /** Der Berechnungstyp des Jobs */
  private String m_type;

  /** Status des Jobs */
  private ISimulationConstants.STATE m_state = ISimulationConstants.STATE.UNKNOWN;

  /** Fortschritt des Jobs, ziwschen 0 und 100, -1 bedeutet: unbekannt */
  private int m_progress = -1;

  /**
   * Beschreibung des Job-Zustandes, falls Status der Fehlerstatus: die Fehlermeldung
   */
  private String m_message = Messages.getString( "org.kalypso.simulation.core.SimulationInfo.0" ); //$NON-NLS-1$

  /**
   * Wird vom Client nach erfolgter Berechnung dargestellt. Dient dazu, dem Benutzer ggfls. Hinweise auf Logdateien im
   * Fehlerfall o.�. zu geben.
   */
  private String m_finishText = ""; //$NON-NLS-1$

  private int m_status = 0; // = IStatus.OK;

  private Throwable m_exception = null;

  public SimulationInfo( )
  {
    // nur f�r wscompile
  }

  public SimulationInfo( final String id, final String description, final String type, final ISimulationConstants.STATE state, final int progress, final String finishText )
  {
    m_id = id;
    m_description = description;
    m_state = state;
    m_progress = progress;
    m_type = type;
    m_finishText = finishText;
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
  @Override
  public int getProgress( )
  {
    return m_progress;
  }

  public ISimulationConstants.STATE getState( )
  {
    return m_state;
  }

  /**
   * @see org.kalypso.services.calculation.job.ICalcMonitor#getMessage()
   */
  @Override
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

  @Override
  public void setProgress( final int i )
  {
    m_progress = i;
  }

  public void setState( final ISimulationConstants.STATE state )
  {
    m_state = state;
  }

  /**
   * @see org.kalypso.services.calculation.job.ICalcMonitor#setMessage(java.lang.String)
   */
  @Override
  public void setMessage( final String message )
  {
    m_message = message;
  }

  public final String getType( )
  {
    return m_type;
  }

  public final void setType( final String type )
  {
    m_type = type;
  }

  /**
   * @see org.kalypso.services.calculation.job.ICalcMonitor#cancel()
   */
  @Override
  public void cancel( )
  {
    m_state = ISimulationConstants.STATE.CANCELED;
  }

  /**
   * @see org.kalypso.services.calculation.job.ICalcMonitor#isCanceled()
   */
  @Override
  public boolean isCanceled( )
  {
    return m_state == ISimulationConstants.STATE.CANCELED;
  }

  @Override
  public String getFinishText( )
  {
    return m_finishText;
  }

  public void setFinishText( final String finishText )
  {
    m_finishText = finishText;
  }

  /**
   * @see org.kalypso.services.calculation.job.ICalcMonitor#setFinishInfo(int, java.lang.String)
   */
  @Override
  public void setFinishInfo( final int status, final String text )
  {
    setFinishStatus( status );
    setFinishText( text );
  }

  public void setFinishStatus( final int status )
  {
    m_status = status;
  }

  @Override
  public int getFinishStatus( )
  {
    return m_status;
  }

  public void setException( final Throwable exception )
  {
    m_exception = exception;
  }

  public Throwable getException( )
  {
    return m_exception;
  }
}