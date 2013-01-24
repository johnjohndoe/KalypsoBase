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
package org.kalypso.ogc.sensor.metadata;

/**
 * @author Dirk Kuch
 */
public interface IMetadataConstants
{
  String AUSGABE_ZEITPUNKT = "Zeitreihe_Ausgabezeitpunkt"; //$NON-NLS-1$

  String BEREITSTELLUNGS_ZEITPUNKT = "Zeitreihe_Bereitstellungszeitpunkt"; //$NON-NLS-1$

  String BOUNDARY_PREFIX = "Grenzwert_"; //$NON-NLS-1$

  String BOUNDARY_FORMAT = BOUNDARY_PREFIX + "%s: %s"; //$NON-NLS-1$

  String AUX_BOUNDARY_FORMAT = "DEBUG_AUXILARY_" + BOUNDARY_PREFIX + "%s: %s"; //$NON-NLS-1$

  String LAST_UPDATE = "Letzte_Aktualisierung"; //$NON-NLS-1$

  String LTV_BOUNDARY_FORMAT = BOUNDARY_PREFIX + "%s: Kalypso_Grenzwert_%s"; //$NON-NLS-1$

  /** Description of the observation */
  String MD_DESCRIPTION = "Beschreibung"; //$NON-NLS-1$

  /** Name of the observation */
  String MD_NAME = "Name"; //$NON-NLS-1$

  /** Some information about the Origin of the observation */
  String MD_ORIGIN = "Entstehung"; //$NON-NLS-1$

  String WQ_BOUNDARY_PREFIX = "WQ-Tabelle_"; //$NON-NLS-1$

  String WQ_BOUNDARY_FORMAT = WQ_BOUNDARY_PREFIX + "%s"; //$NON-NLS-1$

  String WQ_BOUNDARY_Q_MIN = String.format( WQ_BOUNDARY_FORMAT, "Q_Min" ); //$NON-NLS-1$

  String WQ_BOUNDARY_Q_MAX = String.format( WQ_BOUNDARY_FORMAT, "Q_Max" ); //$NON-NLS-1$

  String WQ_BOUNDARY_W_MIN = String.format( WQ_BOUNDARY_FORMAT, "W_Min" ); //$NON-NLS-1$

  String WQ_BOUNDARY_W_MAX = String.format( WQ_BOUNDARY_FORMAT, "W_Max" ); //$NON-NLS-1$
  
  

}
