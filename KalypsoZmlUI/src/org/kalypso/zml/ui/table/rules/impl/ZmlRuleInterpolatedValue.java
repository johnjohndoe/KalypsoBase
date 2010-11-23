/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.rules.impl;

import java.util.HashMap;
import java.util.Map;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.provider.ZmlValueReference;
import org.kalypso.zml.ui.table.rules.IZmlTableRule;
import org.kalypso.zml.ui.table.style.CellStyle;

/**
 * @author Dirk Kuch
 */
public class ZmlRuleInterpolatedValue implements IZmlTableRule
{
  public static final String ID = "org.kalypso.zml.ui.table.rule.value.interpolated";

  Map<String, CellStyle> m_styles = new HashMap<String, CellStyle>();

  /**
   * @see org.kalypso.zml.ui.table.rules.IZmlTableRule#getIdentifier()
   */
  @Override
  public String getIdentifier( )
  {
    return ID;
  }

  /**
   * @see org.kalypso.zml.ui.table.rules.IZmlTableRule#addStyle(java.lang.String,
   *      org.kalypso.zml.ui.table.schema.AbstractStyleType)
   */
  @Override
  public void addStyle( final String columnId, final CellStyle style )
  {
    m_styles.put( columnId, style );
  }

  /**
   * @see org.kalypso.zml.ui.table.rules.IZmlTableRule#apply(org.kalypso.zml.ui.table.provider.ZmlValueReference)
   */
  @Override
  public boolean apply( final ZmlValueReference reference )
  {
    try
    {
      final Integer status = reference.getStatus();

      return KalypsoStati.BIT_CHECK == (KalypsoStati.BIT_CHECK & status);
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return false;
  }

  /**
   * @see org.kalypso.zml.ui.table.rules.IZmlTableRule#getStyle(java.lang.String)
   */
  @Override
  public CellStyle getStyle( final String columnId )
  {
    return m_styles.get( columnId );
  }

}
