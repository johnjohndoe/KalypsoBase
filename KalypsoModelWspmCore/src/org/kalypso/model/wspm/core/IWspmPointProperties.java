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
package org.kalypso.model.wspm.core;

/**
 * @author Dirk Kuch
 */
public interface IWspmPointProperties
{
  String POINT_PROPERTY = "urn:ogc:gml:dict:kalypso:model:wspm:profilePointComponents#"; //$NON-NLS-1$

  String POINT_PROPERTY_BEWUCHS = POINT_PROPERTY + "BEWUCHS"; //$NON-NLS-1$

  String POINT_PROPERTY_BEWUCHS_AX = POINT_PROPERTY_BEWUCHS + "_AX"; //$NON-NLS-1$

  String POINT_PROPERTY_BEWUCHS_AY = POINT_PROPERTY_BEWUCHS + "_AY"; //$NON-NLS-1$

  String POINT_PROPERTY_BEWUCHS_DP = POINT_PROPERTY_BEWUCHS + "_DP"; //$NON-NLS-1$

  String POINT_PROPERTY_BEWUCHS_CLASS = POINT_PROPERTY_BEWUCHS + "_CLASS"; //$NON-NLS-1$

  String POINT_PROPERTY_BREITE = POINT_PROPERTY + "BREITE"; //$NON-NLS-1$

  String POINT_PROPERTY_HOCHWERT = POINT_PROPERTY + "HOCHWERT"; //$NON-NLS-1$

  String POINT_PROPERTY_HOEHE = POINT_PROPERTY + "HOEHE"; //$NON-NLS-1$

  String POINT_PROPERTY_COMMENT = POINT_PROPERTY + "COMMENT"; //$NON-NLS-1$

  /**
   * An (optional) (, arbitrary string-) code any profile point can have<br/>
   * Meant to be some kind of (elsewhere defined) classification of a point.
   */
  String POINT_PROPERTY_CODE = POINT_PROPERTY + "CODE"; //$NON-NLS-1$

  /**
   * An (optional) (, arbitrary string-) id any profile point can have.<br/>
   * Meant to be some kind of (possibly unique) identifier of a point.
   */
  String POINT_PROPERTY_ID = POINT_PROPERTY + "ID"; //$NON-NLS-1$

  String POINT_PROPERTY_RAUHEIT = POINT_PROPERTY + "RAUHEIT"; //$NON-NLS-1$

// Die Id im Dictionary bleibt "Rauheit" wird aber im IProfil als Typ "ks" interpretiert
  String POINT_PROPERTY_RAUHEIT_KS = POINT_PROPERTY_RAUHEIT; //$NON-NLS-1$

  String POINT_PROPERTY_RAUHEIT_KST = POINT_PROPERTY_RAUHEIT + "_KST"; //$NON-NLS-1$

  String POINT_PROPERTY_ROUGHNESS_CLASS = POINT_PROPERTY_RAUHEIT + "_CLASS"; //$NON-NLS-1$

  String POINT_PROPERTY_ROUGHNESS_FACTOR = POINT_PROPERTY_RAUHEIT + "_FACTOR"; //$NON-NLS-1$

  String POINT_PROPERTY_RECHTSWERT = POINT_PROPERTY + "RECHTSWERT"; //$NON-NLS-1$

}
