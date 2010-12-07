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

import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.ui.table.binding.ZmlRule;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.rules.IZmlRuleImplementation;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractZmlTableRule implements IZmlRuleImplementation
{

  /**
   * @see org.kalypso.zml.ui.table.rules.IZmlTableRule#update(org.kalypso.zml.ui.table.model.IZmlModelRow,
   *      org.kalypso.zml.ui.table.binding.BaseColumn, java.lang.String)
   */
  @Override
  public String update( final ZmlRule rule, final IZmlValueReference reference, final String text ) throws SensorException
  {
    return text;
  }

  /**
   * @see org.kalypso.zml.ui.table.rules.IZmlRuleImplementation#apply(org.kalypso.zml.ui.table.binding.ZmlRule,
   *      org.kalypso.zml.ui.table.model.references.IZmlValueReference)
   */
  @Override
  public final boolean apply( final ZmlRule rule, final IZmlValueReference reference )
  {
    if( !rule.isEnabled() )
      return false;
    else if( reference == null )
      return false;

    return doApply( rule, reference );
  }

  protected abstract boolean doApply( ZmlRule rule, IZmlValueReference reference );

}
