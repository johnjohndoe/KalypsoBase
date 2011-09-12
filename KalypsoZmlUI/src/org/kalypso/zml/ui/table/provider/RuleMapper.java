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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.ZmlRule;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.rules.IZmlRuleImplementation;

/**
 * @author Dirk Kuch
 */
public class RuleMapper
{
  private ZmlRule[] m_rules;

  private final Map<ZmlRule, AppliedRule> m_applied = new LinkedHashMap<ZmlRule, AppliedRule>();

  private final BaseColumn m_column;

  private IZmlValueReference m_lastReference;

  public RuleMapper( final BaseColumn column )
  {
    m_column = column;
  }

  public ZmlRule[] findActiveRules( final IZmlValueReference reference )
  {
    if( m_lastReference == reference )
      return m_rules;

    final List<ZmlRule> rules = new ArrayList<ZmlRule>();
    if( Objects.isNotNull( reference ) )
    {
      final ZmlRule[] columnRules = m_column.getRules();
      for( final ZmlRule rule : columnRules )
      {
        final IZmlRuleImplementation impl = rule.getImplementation();
        if( impl.apply( rule, reference ) )
        {
          addRule( rule, impl, reference );

          rules.add( rule );
        }
      }
    }

    m_rules = rules.toArray( new ZmlRule[] {} );
    m_lastReference = reference;

    return m_rules;
  }

  private void addRule( final ZmlRule rule, final IZmlRuleImplementation implementation, final IZmlValueReference reference )
  {
    try
    {
      final Double severity = implementation.getSeverity( rule, reference );

      final AppliedRule applied = m_applied.get( rule );
      if( Objects.isNull( applied ) )
      {
        final CellStyle style = implementation.getCellStyle( rule, reference );
        final String label = implementation.getLabel( rule, reference );

        m_applied.put( rule, new AppliedRule( rule, style, label, severity ) );
      }
      else
      {
        if( applied.getSeverity() >= severity )
          return;

        final CellStyle style = implementation.getCellStyle( rule, reference );
        final String label = implementation.getLabel( rule, reference );

        m_applied.put( rule, new AppliedRule( rule, style, label, severity ) );
      }
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }
  }

  public void reset( )
  {
    m_applied.clear();
    m_lastReference = null;
  }

  public AppliedRule[] getAppliedRules( )
  {
    return m_applied.values().toArray( new AppliedRule[] {} );
  }
}
