package org.kalypso.zml.core.table.binding.rule;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.ZmlStyleResolver;
import org.kalypso.zml.core.table.binding.rule.instructions.ZmlMetadataBoundaryInstruction;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.rules.IZmlRuleImplementation;
import org.kalypso.zml.core.table.schema.AbstractRuleInstructionType;
import org.kalypso.zml.core.table.schema.CellStyleType;
import org.kalypso.zml.core.table.schema.MetadataBoundaryInstructionType;
import org.kalypso.zml.core.table.schema.RuleInstructionsType;
import org.kalypso.zml.core.table.schema.RuleType;
import org.kalypso.zml.core.table.schema.StyleReferenceType;

/**
 * ZmlRuleType binding class
 * 
 * @author Dirk Kuch
 */
public class ZmlRule
{
  private final RuleType m_rule;

  private ZmlMetadataBoundaryInstruction[] m_instructions;

  private CellStyle m_baseStyle;

  private boolean m_enabled;

  public ZmlRule( final RuleType rule )
  {
    m_rule = rule;

    setEnabled( rule.isEnabled() );
  }

  public CellStyle getStyle( final IZmlModelRow row, final BaseColumn column ) throws CoreException
  {
    if( m_baseStyle == null )
    {
      final ZmlStyleResolver resolver = ZmlStyleResolver.getInstance();
      final StyleReferenceType styleReference = m_rule.getStyleReference();
      m_baseStyle = resolver.findStyle( styleReference );
    }

    return extendStyle( row, column );
  }

  private CellStyle extendStyle( final IZmlModelRow row, final BaseColumn column )
  {
    final IZmlValueReference reference = row.get( column.getType() );
    if( reference == null )
      return m_baseStyle;

    CellStyleType base = m_baseStyle.getType();

    final ZmlMetadataBoundaryInstruction[] instructions = getInstructions();
    for( final ZmlMetadataBoundaryInstruction instruction : instructions )
    {
      try
      {
        if( instruction.matches( reference ) )
        {
          final CellStyle style = instruction.getStyle();
          if( style != null )
            base = CellStyle.merge( base, style.getType() );
        }
      }
      catch( final Throwable t )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    return new CellStyle( base );
  }

  public IZmlRuleImplementation getImplementation( )
  {
    return KalypsoZmlCore.getDefault().findRule( m_rule.getRuleReference() );
  }

  public ZmlMetadataBoundaryInstruction[] getInstructions( )
  {
    if( ArrayUtils.isNotEmpty( m_instructions ) )
      return m_instructions;

    final List<ZmlMetadataBoundaryInstruction> myInstructions = new ArrayList<ZmlMetadataBoundaryInstruction>();

    final RuleInstructionsType type = m_rule.getRuleInstructions();
    if( type == null )
      return new ZmlMetadataBoundaryInstruction[] {};

    final List<JAXBElement< ? extends AbstractRuleInstructionType>> abstractInstructions = type.getAbstractRuleInstruction();
    for( final JAXBElement< ? extends AbstractRuleInstructionType> element : abstractInstructions )
    {
      final AbstractRuleInstructionType abstractType = element.getValue();
      if( abstractType instanceof MetadataBoundaryInstructionType )
      {
        myInstructions.add( new ZmlMetadataBoundaryInstruction( (MetadataBoundaryInstructionType) abstractType ) );
      }
    }

    m_instructions = myInstructions.toArray( new ZmlMetadataBoundaryInstruction[] {} );

    return m_instructions;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_rule.getRuleReference();
  }

  public boolean isEnabled( )
  {
    return m_enabled;
  }

  public void setEnabled( final boolean enabled )
  {
    m_enabled = enabled;
  }

  public boolean hasHeaderIcon( )
  {
    return m_rule.isSetHeaderIcon();
  }

  public CellStyle getPlainStyle( ) throws CoreException
  {
    final ZmlStyleResolver resolver = ZmlStyleResolver.getInstance();

    return resolver.findStyle( m_rule.getStyleReference() );
  }

  public String getLabel( )
  {
    return m_rule.getLabel();
  }

  public String getIdentifier( )
  {
    return m_rule.getId();
  }
}
