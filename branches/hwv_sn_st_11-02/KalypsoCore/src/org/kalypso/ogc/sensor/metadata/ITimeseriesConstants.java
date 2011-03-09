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
package org.kalypso.ogc.sensor.metadata;

/**
 * Constants used within the sensor package.
 * 
 * @author schlienger
 */
public interface ITimeseriesConstants extends IMetadataConstants
{

  String MD_PROPERTY_KEEP_METADATA_PREFEX = "reload_keepMetaData";

  /** the alarm-level feature used to show the alarm-levels in some views */
  String FEATURE_ALARMLEVEL = "Alarmstufen"; //$NON-NLS-1$

  /** the forecast feature is used in some of the views to mark the forecast date-range */
  String FEATURE_FORECAST = "Vorhersage";

  String MD_COORDSYS = "Koordinatensystem"; //$NON-NLS-1$

  String MD_DATE_BEGIN = "Datum-Von"; //$NON-NLS-1$

  String MD_DATE_END = "Datum-Bis"; //$NON-NLS-1$

  String MD_FLUSSGEBIET = "Flussgebiet"; //$NON-NLS-1$

  String MD_GEWAESSER = "Gewässer"; //$NON-NLS-1$

  String MD_GKH = "Hochwert"; //$NON-NLS-1$

  String MD_GKR = "Rechtswert"; //$NON-NLS-1$

  String MD_HOEHENANGABEART = "Höhenangabeart"; //$NON-NLS-1$

  /** Stationskennziffer */
  String MD_KENNZIFFER = "Kennziffer"; //$NON-NLS-1$

  String MD_MESSTISCHBLATT = "Messtischblattnummer"; //$NON-NLS-1$

  String MD_PEGELNULLPUNKT = "Pegelnullpunkt"; //$NON-NLS-1$

  /** reference of the first call of a time series (the original source!) */
  String MD_SRC_REPOSITORY = "Quell_Repository";

  /** reference of the first call of a time series (the original source!) */
  String MD_SRC_TIMESERIES = "Quell_Zeitreihe";

  // METADATEN
  String MD_TIMEZONE = "Zeitzone"; //$NON-NLS-1$

  /**
   * Markierung für eine Vorhersage. Wenn die Property gesetzt ist (true), handelt es sich um eine Vorhersage Zeitreihe.
   * 
   * @deprecated Es sollte nur noch {@link #MD_VORHERSAGE_START} und {@link #MD_VORHERSAGE_ENDE} benutzt werden.
   */
  @Deprecated
  String MD_VORHERSAGE = "Vorhersage"; //$NON-NLS-1$

  String MD_VORHERSAGE_ENDE = "Vorhersage Ende"; //$NON-NLS-1$

  String MD_VORHERSAGE_START = "Vorhersage Start"; //$NON-NLS-1$

  /**
   * qualitative Markierungen für eine Vorhersage. Sichere bzw. unsichere Vorhersage. Letzteres heißt auch Abschätzung,
   * Tendenz
   */
  String MD_SICHERE_VORHERSAGE_START = "sichere Vorhersage Start"; //$NON-NLS-1$

  String MD_SICHERE_VORHERSAGE_ENDE = "sichere Vorhersage Ende"; //$NON-NLS-1$

  String MD_UNSICHERE_VORHERSAGE_START = "unsichere Vorhersage Start"; //$NON-NLS-1$

  String MD_UNSICHERE_VORHERSAGE_ENDE = "unsichere Vorhersage Ende"; //$NON-NLS-1$

  String MD_WQTABLE = "WQ-Tabelle"; //$NON-NLS-1$

  String MD_WQWECHMANN = "WQ-Parameter"; //$NON-NLS-1$

  /** AREA [m^2] */
  String TYPE_AREA = "A"; //$NON-NLS-1$

  /** virtual time series data source */
  String TYPE_DATA_SRC = "DATA_SRC"; //$NON-NLS-1$

  /** Datum */
  String TYPE_DATE = "date"; //$NON-NLS-1$

  /** Day - day 0-365 */
  String TYPE_DAY = "DAY"; //$NON-NLS-1$

  String TYPE_DESCRIPTION = "description"; //$NON-NLS-1$

  /** Evaporation [mm] */
  String TYPE_EVAPORATION = "E"; //$NON-NLS-1$

  /** hours [h] */
  String TYPE_HOURS = "H"; //$NON-NLS-1$

  /** Humidity [%] */
  String TYPE_HUMIDITY = "U"; //$NON-NLS-1$

  /** Der Korrekturwert der Verdunstung gegenüber der pot. Verdunstung in einem Nutzungszyklus [-] */
  String TYPE_KC = "KC"; //$NON-NLS-1$

  /** Der Speicherinhalt des Interzeptionsspeichers in einem Nutzungszyklus [mm] */
  String TYPE_LAI = "LAI"; //$NON-NLS-1$

  /** minutes [min] */
  String TYPE_MIN = "min"; //$NON-NLS-1$

  String TYPE_NODEID = "nodeID"; //$NON-NLS-1$

  /** area as norm [A/Asum] */
  String TYPE_NORM = "n"; //$NON-NLS-1$

  /** Normal Null */
  String TYPE_NORMNULL = "NN"; //$NON-NLS-1$

  String TYPE_ORDINAL_NUMBER = "ordinalNr"; //$NON-NLS-1$

  String TYPE_PEGEL = "pegel"; //$NON-NLS-1$

  /** Polder-Kontrolle: an/aus */
  String TYPE_POLDER_CONTROL = "POLDER_CONTROL"; //$NON-NLS-1$

  /** Niederschlag */
  String TYPE_RAINFALL = "N"; //$NON-NLS-1$

  /** Abfluss */
  String TYPE_RUNOFF = "Q"; //$NON-NLS-1$

  String TYPE_DISCHARGE = TYPE_RUNOFF;

  String TYPE_RUNOFF_RHB = "Qrhb"; //$NON-NLS-1$

  String TYPE_RUNOFF_Q2 = "Q2"; //$NON-NLS-1$

  String TYPE_RUNOFF_Q3 = "Q3"; //$NON-NLS-1$

  /** Temperatur */
  String TYPE_TEMPERATURE = "T"; //$NON-NLS-1$

  /** Velocity [m/s] */
  String TYPE_VELOCITY = "v"; //$NON-NLS-1$

  /** Füllung (VOLUMEN) */
  String TYPE_VOLUME = "V"; //$NON-NLS-1$

  /** Wasserstand */
  String TYPE_WATERLEVEL = "W"; //$NON-NLS-1$

  /** Wasserstand gauge in cm */
  String TYPE_WATERLEVEL_GAUGE_CM = "W_GAUGE_CM"; //$NON-NLS-1$

  /** Wasserstand gauge in m */
  String TYPE_WATERLEVEL_GAUGE_M = "W_GAUGE_M"; //$NON-NLS-1$

  /** Die Wurzeltiefe in einem Nutzungszyklus [dm] */
  String TYPE_WT = "WT"; //$NON-NLS-1$

}