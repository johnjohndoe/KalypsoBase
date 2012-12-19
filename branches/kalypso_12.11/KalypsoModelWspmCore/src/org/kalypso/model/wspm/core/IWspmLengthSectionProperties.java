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
public interface IWspmLengthSectionProperties
{
  String URN_OGC_GML_DICT_KALYPSO_MODEL_WSPM_COMPONENTS = "urn:ogc:gml:dict:kalypso:model:wspm:components"; //$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY = URN_OGC_GML_DICT_KALYPSO_MODEL_WSPM_COMPONENTS + "#";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_RUNOFF = LENGTH_SECTION_PROPERTY + "LengthSectionRunOff";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_STATION = LENGTH_SECTION_PROPERTY + "LengthSectionStation";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_TYPE = LENGTH_SECTION_PROPERTY + "LengthSectionProfileType";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_GROUND = LENGTH_SECTION_PROPERTY + "LengthSectionGround";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_BOE_LI = LENGTH_SECTION_PROPERTY + "LengthSection_Boe_li";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_BOE_RE = LENGTH_SECTION_PROPERTY + "LengthSection_Boe_re";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_WEIR_OK = LENGTH_SECTION_PROPERTY + "LengthSection_WeirOK";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_BRIDGE_OK = LENGTH_SECTION_PROPERTY + "LengthSection_BridgeOK";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_BRIDGE_UK = LENGTH_SECTION_PROPERTY + "LengthSection_BridgeUK";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_TEXT = LENGTH_SECTION_PROPERTY + "LengthSectionText";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_BRIDGE_WIDTH = LENGTH_SECTION_PROPERTY + "LengthSection_BridgeWidth";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_ROHR_DN = LENGTH_SECTION_PROPERTY + "LengthSection_RohrDN";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_WATERLEVEL = LENGTH_SECTION_PROPERTY + "LengthSectionWaterlevel";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_H_BV = LENGTH_SECTION_PROPERTY + "LengthSection_h_BV";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_V_M = LENGTH_SECTION_PROPERTY + "LengthSection_v_m";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_V_LI = LENGTH_SECTION_PROPERTY + "LengthSection_v_li";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_V_FL = LENGTH_SECTION_PROPERTY + "LengthSection_v_fl";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_V_RE = LENGTH_SECTION_PROPERTY + "LengthSection_v_re";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_F = LENGTH_SECTION_PROPERTY + "LengthSection_f";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_F_LI = LENGTH_SECTION_PROPERTY + "LengthSection_f_li";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_F_FL = LENGTH_SECTION_PROPERTY + "LengthSection_f_fl";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_F_RE = LENGTH_SECTION_PROPERTY + "LengthSection_f_re";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_BR = LENGTH_SECTION_PROPERTY + "LengthSection_br";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_BR_LI = LENGTH_SECTION_PROPERTY + "LengthSection_br_li";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_BR_FL = LENGTH_SECTION_PROPERTY + "LengthSection_br_fl";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_BR_RE = LENGTH_SECTION_PROPERTY + "LengthSection_br_re";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_Q_LI = LENGTH_SECTION_PROPERTY + "LengthSection_Q_li";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_Q_FL = LENGTH_SECTION_PROPERTY + "LengthSection_Q_fl";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_Q_RE = LENGTH_SECTION_PROPERTY + "LengthSection_Q_re";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_HEN = LENGTH_SECTION_PROPERTY + "LengthSection_hen";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_FROUDE = LENGTH_SECTION_PROPERTY + "LengthSection_froude";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_EASTING = LENGTH_SECTION_PROPERTY + "LengthSection_easting";//$NON-NLS-1$

  String LENGTH_SECTION_PROPERTY_NORTHING = LENGTH_SECTION_PROPERTY + "LengthSection_northing";//$NON-NLS-1$
}
