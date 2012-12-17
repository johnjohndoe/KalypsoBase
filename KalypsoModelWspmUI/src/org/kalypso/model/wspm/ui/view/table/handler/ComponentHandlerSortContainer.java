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
package org.kalypso.model.wspm.ui.view.table.handler;

import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler;

/**
 * @author Dirk Kuch
 */
public class ComponentHandlerSortContainer
{
  private final Integer m_index;

  private final IComponentUiHandler m_handler;

  private final String m_identifier;

  public ComponentHandlerSortContainer( final String identifier, final Integer index, final IComponentUiHandler handler )
  {
    m_identifier = identifier;
    m_index = index;
    m_handler = handler;
  }

  public Integer getIndex( )
  {
    return m_index;
  }

  public IComponentUiHandler getHandler( )
  {
    return m_handler;
  }

  /**
   * TODO temporary implementation - priorities should be configured in an external xml!
   */
  public Integer getPriority( )
  {
    if( getIndex() < 0 )
      return 1;

    switch( m_identifier )
    {
      case IWspmPointProperties.POINT_PROPERTY_ID:
        return 1;

      case IWspmPointProperties.POINT_PROPERTY_BREITE:
        return 100;
      case IWspmPointProperties.POINT_PROPERTY_HOEHE:
        return 110;

      case IWspmPointProperties.POINT_PROPERTY_RECHTSWERT:
        return 150;
      case IWspmPointProperties.POINT_PROPERTY_HOCHWERT:
        return 160;

      case IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS:
        return 500;

      case IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS:
        return 510;
      case IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST:
        return 520;
      case IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_FACTOR:
        return 530;

      case IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS:
        return 600;

      case IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX:
        return 610;
      case IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY:
        return 620;
      case IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP:
        return 630;

      case IWspmPointProperties.POINT_PROPERTY_COMMENT:
        return 800;

      case IWspmPointProperties.POINT_PROPERTY_CODE:
        return 810;

      default:
        return 999;
    }
  }

  public String getIdentifier( )
  {
    return m_identifier;
  }
}
