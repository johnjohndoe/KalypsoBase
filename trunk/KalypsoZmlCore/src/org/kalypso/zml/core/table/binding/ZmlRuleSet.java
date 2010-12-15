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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.schema.RuleRefernceType;
import org.kalypso.zml.core.table.schema.RuleSetType;
import org.kalypso.zml.core.table.schema.RuleType;

/**
 * @author Dirk Kuch
 */
public class ZmlRuleSet
{
  private final RuleSetType m_type;

  private ZmlRule[] m_rules;

  public ZmlRuleSet( final RuleSetType type )
  {
    m_type = type;
  }

  public String getIdentifier( )
  {
    return m_type.getId();
  }

  public ZmlRule[] getRules( )
  {
    if( ArrayUtils.isNotEmpty( m_rules ) )
      return m_rules;

    final List<ZmlRule> rules = new ArrayList<ZmlRule>();
    final ZmlRuleResolver resolver = ZmlRuleResolver.getInstance();

    for( final Object objRule : m_type.getRuleOrRule() )
    {
      if( objRule instanceof RuleType )
      {
        final RuleType ruleType = (RuleType) objRule;
        rules.add( new ZmlRule( ruleType ) );
      }
      else if( objRule instanceof RuleRefernceType )
      {
        try
        {
          final RuleRefernceType reference = (RuleRefernceType) objRule;
          final ZmlRule rule = resolver.findRule( null, reference );
          if( rule != null )
            rules.add( rule );
        }
        catch( final CoreException e )
        {
          KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
        }
      }
    }

    m_rules = rules.toArray( new ZmlRule[] {} );

    return m_rules;
  }

  public ZmlRule find( final String identifier )
  {
    final ZmlRule[] rules = getRules();
    for( final ZmlRule rule : rules )
    {
      if( rule.getIdentifier().equals( identifier ) )
        return rule;
    }

    return null;
  }
}
