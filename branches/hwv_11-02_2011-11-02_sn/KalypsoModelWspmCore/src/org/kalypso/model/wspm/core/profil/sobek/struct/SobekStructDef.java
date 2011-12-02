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
package org.kalypso.model.wspm.core.profil.sobek.struct;

import java.util.Locale;

/**
 * Represents one entry in a 'struct.def' sobek ascii file.<br/>
 * <br/>
 * SOBEK Documentation says:<br/>
 * 
 * <pre>
 * SOBEK Urban/Rural Bridge:
 * 
 * struct.def:
 * 
 * This file contains general definitions of structures. The following types of structures are distinguished:
 * 
 * 0. River weir (River module only)
 * 1. River advanced weir
 * 2. General structure
 * 3. River pump
 * 4. Database structure (River module only)
 * 5. 
 * 6. Weir
 * 7. Orifice
 * 8. 
 * 9. Pump
 * 10. Culvert, Siphon and Inverse siphon 
 * 11. Universal weir 
 * 12. Bridge
 * 13. Breach growth 1D Dam break node
 * 112. Breach growth 2D Dam break node
 * 
 * STDS id 'bridge1' nm 'bridge' ty 12 tb 1 si 'trapezoidal1' pw 0.5 vf 1.15 li 0.63 lo 0.63 dl 10.0 rl -1.0 stds
 * 
 * where:
 * 
 * ty = type of structure
 * 12 = bridge 
 * tb = type of bridge
 * 2 = pillar bridge
 * 3 = abutment bridge
 * 4 = fixed bed bridge
 * 5 = soil bed bridge
 * si = id of cross section definition (profile.def), only open profiles (if tb =3,4, or 5)
 * pw = total width of pillars in direction of flow (if tb=2) 
 * vf = form factor (if tb=2)
 * li = inlet loss coefficient
 * lo = outlet loss coefficient
 * dl = length of bridge in flow direction.
 * rl = bottom level
 * </pre>
 * 
 * @author Gernot Belger
 */
public final class SobekStructDef
{
  /**
   * @param width
   *          (=dl): length of bridge in flow direction.
   * @param bottomLevel
   *          (=rl): bottom level
   * @param crossSectionReference
   *          (=si). Reference to cross section definition in profile.def
   */
  public static SobekStructDef createAbutmentBridge( final String id, final String name, final double width, final double bottomLevel, final String crossSectionReference )
  {
    return new SobekStructDef( id, name, SOBEK_STRUCT_TYPE.bridge, SOBEK_BRIDGE_TYPE.abutment_bridge, -1, -1, 0, 0, width, bottomLevel, crossSectionReference );
  }

  private String m_id = "notSet";

  private String m_name = "notSet";

  private SOBEK_STRUCT_TYPE m_ty = SOBEK_STRUCT_TYPE._unknown_;

  private SOBEK_BRIDGE_TYPE m_tb = SOBEK_BRIDGE_TYPE._unknown_;

  private double m_pw = -1;

  private double m_vf = -1;

  private double m_li = -1;

  private double m_lo = -1;

  private double m_dl = -1;

  private double m_rl = -1;

  private String m_si = "notSet";

  public SobekStructDef( final String id, final String name, final SOBEK_STRUCT_TYPE ty, final SOBEK_BRIDGE_TYPE tb, final double pw, final double vf, final double li, final double lo, final double dl, final double rl, final String si )
  {
    m_id = id;
    m_name = name;
    m_ty = ty;
    m_tb = tb;
    m_pw = pw;
    m_vf = vf;
    m_li = li;
    m_lo = lo;
    m_dl = dl;
    m_rl = rl;
    m_si = si;
  }

  public String getID( )
  {
    return m_id;
  }

  public String getName( )
  {
    return m_name;
  }

  private SOBEK_STRUCT_TYPE getTy( )
  {
    return m_ty;
  }

  private SOBEK_BRIDGE_TYPE getTb( )
  {
    return m_tb;
  }

  private String getSi( )
  {
    return m_si;
  }

  private double getPw( )
  {
    return m_pw;
  }

  private double getVf( )
  {
    return m_vf;
  }

  private double getLi( )
  {
    return m_li;
  }

  private double getLo( )
  {
    return m_lo;
  }

  private double getDl( )
  {
    return m_dl;
  }

  private double getRl( )
  {
    return m_rl;
  }

  public String serialize( )
  {
    final String id = getID();
    final String name = getName();
    final int type = getTy().getTy();
    final int bridgeType = getTb().getTb();
    final String profileRefId = getSi();

    final double pw = getPw(); // 0.5 = total width of pillars in direction of flow (if tb=2)
    final double vf = getVf(); // 1.15 = form factor (if tb=2)
    final double li = getLi(); // 0.63 = inlet loss coefficient
    final double lo = getLo(); // 0.63 = outlet loss coefficient
    final double dl = getDl(); // 10.0 = length of bridge in flow direction.
    final double rl = getRl(); // -1.0 = bottom level

    return String.format( Locale.US, "STDS id '%s' nm '%s' ty %d tb %d si '%s' pw %.2f vf %.2f li %.2f lo %.2f dl %.2f rl %.2f stds", id, name, type, bridgeType, profileRefId, pw, vf, li, lo, dl, rl );
  }

}
