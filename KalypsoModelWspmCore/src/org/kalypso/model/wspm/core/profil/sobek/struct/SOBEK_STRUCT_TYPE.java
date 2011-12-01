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

public enum SOBEK_STRUCT_TYPE
{
  _unknown_("_Unknown_", -1),
  river_weir("River weir", 0), // (River module only)
  river_advanced_weir("River advanced weir", 1), //
  general_structure("General structure", 2), //
  river_pump("River pump", 3), //
  database_structure("Database structure", 4), // (River module only)
  _5("", 5), //
  weir("Weir", 6), //
  orifice("Orifice", 7), //
  _8("", 8), //
  pump("Pump", 9), //
  culvert_siphon_inverse_siphon("Culvert, Siphon and Inverse siphon", 10), //
  universal_weir("Universal weir", 11), //
  bridge("Bridge", 12), //
  breach_growth_1d_dam_break_node("Breach growth 1D Dam break node", 13), //
  breach_growth_2d_dam_break_node("Breach growth 2D Dam break node", 112);

  private final String m_label;

  private final int m_ty;

  private SOBEK_STRUCT_TYPE( final String label, final int ty )
  {
    m_label = label;
    m_ty = ty;
  }

  public int getTy( )
  {
    return m_ty;
  }
  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString( )
  {
    return m_label;
  }
}