package org.kalypso.zml.core.table.rules.impl;

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

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.rules.AbstractZmlCellRuleImplementation;

/**
 * @author Dirk Kuch
 */
public class ZmlDerivedValue extends AbstractZmlCellRuleImplementation
{
  public static final String ID = "org.kalypso.zml.ui.core.rule.derived.value"; //$NON-NLS-1$

  @Override
  public String getIdentifier( )
  {
    return ID;
  }

  @Override
  protected boolean doApply( final ZmlCellRule rule, final IZmlModelCell reference )
  {
    if( !(reference instanceof IZmlModelValueCell) )
      return false;

    final IZmlModelValueCell cell = (IZmlModelValueCell) reference;

    final IAxis axis = cell.getColumn().getValueAxis();
    if( axis == null )
      return false;

    return !axis.isPersistable();
  }

}
