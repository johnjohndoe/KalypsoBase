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
package org.kalypso.model.wspm.core.profil.sobek.profiles;

import java.math.BigDecimal;

/**
 * @author Gernot Belger
 */
public class SobekNetworkD12Point
{
  private final String m_id;

  private final String m_name;

  private final String m_carrierID;

  private final BigDecimal m_lc;

  private final BigDecimal m_px;

  private final BigDecimal m_py;

  private final int m_mc;

  private final int m_mr;

  private final String m_srsName;

  public SobekNetworkD12Point( final String id, final String name, final String carrierID, final BigDecimal lc, final BigDecimal px, final BigDecimal py, final int mc, final int mr, final String srsName )
  {
    m_id = id;
    m_name = name;
    m_carrierID = carrierID;
    m_lc = lc;
    m_px = px;
    m_py = py;
    m_mc = mc;
    m_mr = mr;
    m_srsName = srsName;
  }

  public String getID( )
  {
    return m_id;
  }

  public String getName( )
  {
    return m_name;
  }

  public BigDecimal getPX( )
  {
    return m_px;
  }

  public BigDecimal getPY( )
  {
    return m_py;
  }

  public String getSrsName( )
  {
    return m_srsName;
  }
}