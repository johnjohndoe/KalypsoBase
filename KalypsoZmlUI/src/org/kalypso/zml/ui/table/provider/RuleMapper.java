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
package org.kalypso.zml.ui.table.provider;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.ZmlRule;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.rules.IZmlRuleImplementation;

/**
 * @author Dirk Kuch
 */
public class RuleMapper
{
  private IZmlModelRow m_lastRow;

  private BaseColumn m_lastColumn;

  private ZmlRule[] m_rules;

  public ZmlRule[] findActiveRules( final IZmlModelRow row, final BaseColumn column )
  {
    if( m_lastRow == row && m_lastColumn == column )
      return m_rules;

    final List<ZmlRule> rules = new ArrayList<ZmlRule>();
    final IZmlValueReference reference = row.get( column.getType() );
    if( reference != null )
    {
      for( final ZmlRule rule : column.getRules() )
      {
        final IZmlRuleImplementation impl = rule.getImplementation();
        if( impl.apply( rule, reference ) )
        {
          rules.add( rule );
        }
      }
    }

    m_lastRow = row;
    m_lastColumn = column;
    m_rules = rules.toArray( new ZmlRule[] {} );

    return m_rules;
  }

}
