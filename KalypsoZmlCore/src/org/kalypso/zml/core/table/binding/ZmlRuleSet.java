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
package org.kalypso.zml.core.table.binding;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.binding.rule.AbstractZmlRule;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.binding.rule.ZmlColumnRule;
import org.kalypso.zml.core.table.schema.ColumnRuleType;
import org.kalypso.zml.core.table.schema.RuleRefernceType;
import org.kalypso.zml.core.table.schema.RuleSetType;
import org.kalypso.zml.core.table.schema.RuleType;

/**
 * @author Dirk Kuch
 */
public class ZmlRuleSet
{
  private final RuleSetType m_type;

  private AbstractZmlRule[] m_rules;

  public ZmlRuleSet( final RuleSetType type )
  {
    m_type = type;
  }

  public String getIdentifier( )
  {
    return m_type.getId();
  }

  public AbstractZmlRule[] getRules( )
  {
    if( ArrayUtils.isNotEmpty( m_rules ) )
      return m_rules;

    final List<AbstractZmlRule> rules = new ArrayList<>();
    final ZmlRuleResolver resolver = ZmlRuleResolver.getInstance();

    for( final Object objRule : m_type.getColumnRuleOrRuleOrRule() )
    {
      if( objRule instanceof ColumnRuleType )
      {
        final ColumnRuleType ruleType = (ColumnRuleType) objRule;
        rules.add( new ZmlColumnRule( ruleType ) );
      }
      else if( objRule instanceof RuleType )
      {
        final RuleType ruleType = (RuleType) objRule;
        rules.add( new ZmlCellRule( ruleType ) );
      }
      else if( objRule instanceof RuleRefernceType )
      {
        try
        {
          final RuleRefernceType reference = (RuleRefernceType) objRule;
          final AbstractZmlRule rule = resolver.findRule( null, reference );
          if( Objects.isNotNull( rule ) )
          {
            if( rule instanceof ZmlCellRule )
              ((ZmlCellRule) rule).reset();

            rules.add( rule );
          }
        }
        catch( final CoreException e )
        {
          KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
        }
      }
      else
        throw new UnsupportedOperationException();
    }

    m_rules = rules.toArray( new AbstractZmlRule[] {} );

    return m_rules;
  }

  public AbstractZmlRule find( final String identifier )
  {
    final AbstractZmlRule[] rules = getRules();
    for( final AbstractZmlRule rule : rules )
    {
      final String id = rule.getIdentifier();
      if( id.equals( identifier ) )
        return rule;
    }

    return null;
  }

}
