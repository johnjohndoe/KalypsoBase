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
package org.kalypso.zml.ui.table.binding;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.rules.IZmlRuleImplementation;
import org.kalypso.zml.ui.table.schema.RuleInstruction;
import org.kalypso.zml.ui.table.schema.RuleInstructionsType;
import org.kalypso.zml.ui.table.schema.RuleType;
import org.kalypso.zml.ui.table.schema.StyleReferenceType;
import org.kalypso.zml.ui.table.styles.ZmlStyleResolver;

/**
 * ZmlRuleType binding class
 * 
 * @author Dirk Kuch
 */
public class ZmlRule
{
  private final RuleType m_rule;

  private ZmlRuleInstruction[] m_instructions;

  public ZmlRule( final RuleType rule )
  {
    m_rule = rule;
  }

  public CellStyle getPlainStyle( ) throws CoreException
  {
    final ZmlStyleResolver resolver = ZmlStyleResolver.getInstance();
    final StyleReferenceType styleReference = m_rule.getStyleReference();

    return resolver.findStyle( styleReference );
  }

  public CellStyle getStyle( final BaseColumn column ) throws CoreException
  {
    final ZmlStyleResolver resolver = ZmlStyleResolver.getInstance();
    final StyleReferenceType styleReference = m_rule.getStyleReference();

    final CellStyle baseStyle = column.getDefaultStyle().clone();
    final CellStyle ruleStyle = resolver.findStyle( styleReference );
    CellStyle.merge( baseStyle.getType(), ruleStyle.getType() );

    /** clone - because of cached style properties (invalid cell style members) */
    return ruleStyle.clone();
  }

  public IZmlRuleImplementation getImplementation( )
  {
    return KalypsoZmlUI.getDefault().getRuleImplementation( m_rule.getRuleReference() );
  }

  public ZmlRuleInstruction[] getInstructions( )
  {
    if( ArrayUtils.isNotEmpty( m_instructions ) )
      return m_instructions;

    final List<ZmlRuleInstruction> myInstructions = new ArrayList<ZmlRuleInstruction>();

    final RuleInstructionsType type = m_rule.getRuleInstructions();
    if( type == null )
      return new ZmlRuleInstruction[] {};

    final List<RuleInstruction> instructions = type.getInstruction();
    for( final RuleInstruction instruction : instructions )
    {
      myInstructions.add( new ZmlRuleInstruction( instruction ) );
    }

    m_instructions = myInstructions.toArray( new ZmlRuleInstruction[] {} );

    return m_instructions;
  }
}
