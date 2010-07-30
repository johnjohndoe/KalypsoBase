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
package org.kalypso.ogc.sensor.timeseries;


/**
 * @author Dirk Kuch
 */
public interface IRepositoryConstants extends TimeserieConstants
{
  String MD_PROPERTY_TIME_LEVEL = "Wiski_Aufloesung";

  String MD_PROPERTY_DISTANCE = "Wiski_Distanz";

  String MD_PROPERTY_DISTANCE_UNIT = "Wiski_Distanz_Einheit";

  String MD_PROPERTY_VALUE_TYPE = "Wiski_Einheiten_Art";

  String MD_PROPERTY_KEEP_METADATA_PREFEX = "reload_keepMetaData";

  /**
   * position of the station id - 0 based<br>
   * wiski://HVZ_Modelle_Elbe.Elbe_Prio_1.501060.Q
   */
  int STATION_ID_LOCATION = 2;
}
