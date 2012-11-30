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

import org.kalypso.model.wspm.core.i18n.Messages;

public enum SOBEK_STRUCT_TYPE
{
  _unknown_(Messages.getString( "SOBEK_STRUCT_TYPE_2" ), -1), //$NON-NLS-1$
  river_weir(Messages.getString( "SOBEK_STRUCT_TYPE_1" ), 0), // (River module only) //$NON-NLS-1$
  river_advanced_weir(Messages.getString( "SOBEK_STRUCT_TYPE_0" ), 1), // //$NON-NLS-1$
  general_structure(Messages.getString( "SOBEK_STRUCT_TYPE_3" ), 2), // //$NON-NLS-1$
  river_pump(Messages.getString( "SOBEK_STRUCT_TYPE_4" ), 3), // //$NON-NLS-1$
  database_structure(Messages.getString( "SOBEK_STRUCT_TYPE_5" ), 4), // (River module only) //$NON-NLS-1$
  _5("", 5), // //$NON-NLS-1$
  weir(Messages.getString( "SOBEK_STRUCT_TYPE_7" ), 6), // //$NON-NLS-1$
  orifice(Messages.getString( "SOBEK_STRUCT_TYPE_8" ), 7), // //$NON-NLS-1$
  _8("", 8), // //$NON-NLS-1$
  pump(Messages.getString( "SOBEK_STRUCT_TYPE_10" ), 9), // //$NON-NLS-1$
  culvert_siphon_inverse_siphon(Messages.getString( "SOBEK_STRUCT_TYPE_11" ), 10), // //$NON-NLS-1$
  universal_weir(Messages.getString( "SOBEK_STRUCT_TYPE_12" ), 11), // //$NON-NLS-1$
  bridge(Messages.getString( "SOBEK_STRUCT_TYPE_13" ), 12), // //$NON-NLS-1$
  breach_growth_1d_dam_break_node(Messages.getString( "SOBEK_STRUCT_TYPE_14" ), 13), // //$NON-NLS-1$
  breach_growth_2d_dam_break_node(Messages.getString( "SOBEK_STRUCT_TYPE_15" ), 112); //$NON-NLS-1$

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