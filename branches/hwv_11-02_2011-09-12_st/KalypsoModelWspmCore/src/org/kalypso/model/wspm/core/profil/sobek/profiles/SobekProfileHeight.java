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
 * This class containes data for one height of a tabulated sobek profile.
 * 
 * @author Holger Albert
 */
public class SobekProfileHeight
{
  /**
   * The height.
   */
  private BigDecimal m_height;

  /**
   * The full width.
   */
  private BigDecimal m_fullWidth;

  /**
   * The flow width.
   */
  private BigDecimal m_flowWidth;

  /**
   * The constructor.
   * 
   * @param height
   *          The height.
   * @param fullWidth
   *          The full width.
   * @param flowWidth
   *          The flow width.
   */
  public SobekProfileHeight( BigDecimal height, BigDecimal fullWidth, BigDecimal flowWidth )
  {
    m_height = height;
    m_fullWidth = fullWidth;
    m_flowWidth = flowWidth;
  }

  /**
   * This function returns the height.
   * 
   * @return The height.
   */
  public BigDecimal getHeight( )
  {
    return m_height;
  }

  /**
   * This function returns the full width.
   * 
   * @return The full width.
   */
  public BigDecimal getFullWidth( )
  {
    return m_fullWidth;
  }

  /**
   * This function returns the flow width.
   * 
   * @return The flow width.
   */
  public BigDecimal getFlowWidth( )
  {
    return m_flowWidth;
  }
}