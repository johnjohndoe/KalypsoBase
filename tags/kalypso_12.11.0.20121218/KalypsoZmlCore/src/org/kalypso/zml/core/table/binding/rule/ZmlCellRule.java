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
package org.kalypso.zml.core.table.binding.rule;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.KalypsoZmlCoreExtensions;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.instructions.AbstractZmlRuleInstructionType;
import org.kalypso.zml.core.table.binding.rule.instructions.ZmlMetadataBoundaryInstruction;
import org.kalypso.zml.core.table.binding.rule.instructions.ZmlMetadataDaterangeInstruction;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.rules.IZmlCellRuleImplementation;
import org.kalypso.zml.core.table.schema.AbstractRuleInstructionType;
import org.kalypso.zml.core.table.schema.CellStyleType;
import org.kalypso.zml.core.table.schema.MetadataBoundaryInstructionType;
import org.kalypso.zml.core.table.schema.MetadataDateRangeInstructionType;
import org.kalypso.zml.core.table.schema.RuleInstructionsType;
import org.kalypso.zml.core.table.schema.RuleType;

/**
 * @author Dirk Kuch
 */
public class ZmlCellRule extends AbstractZmlRule
{
  private AbstractZmlRuleInstructionType[] m_instructions;

  public ZmlCellRule( final RuleType rule )
  {
    super( rule );
  }

  public IZmlCellRuleImplementation getImplementation( )
  {
    return KalypsoZmlCoreExtensions.getInstance().findCellRule( getRuleType().getRuleReference() );
  }

  public String getLabel( final IZmlModelValueCell reference )
  {
    final IZmlCellRuleImplementation impl = getImplementation();
    return impl.getLabel( this, reference );
  }

  @Override
  public RuleType getRuleType( )
  {
    return (RuleType) super.getRuleType();
  }

  public AbstractZmlRuleInstructionType[] getInstructions( )
  {
    if( ArrayUtils.isNotEmpty( m_instructions ) )
      return m_instructions;

    final List<AbstractZmlRuleInstructionType> myInstructions = new ArrayList<>();

    final RuleInstructionsType type = getRuleType().getRuleInstructions();
    if( Objects.isNull( type ) )
      return new ZmlMetadataBoundaryInstruction[] {};

    final List<JAXBElement< ? extends AbstractRuleInstructionType>> abstractInstructions = type.getAbstractRuleInstruction();
    for( final JAXBElement< ? extends AbstractRuleInstructionType> element : abstractInstructions )
    {
      final AbstractRuleInstructionType abstractType = element.getValue();
      if( abstractType instanceof MetadataBoundaryInstructionType )
        myInstructions.add( new ZmlMetadataBoundaryInstruction( abstractType ) );
      else if( abstractType instanceof MetadataDateRangeInstructionType )
        myInstructions.add( new ZmlMetadataDaterangeInstruction( abstractType ) );
      else
        throw new UnsupportedOperationException();
    }

    m_instructions = myInstructions.toArray( new AbstractZmlRuleInstructionType[] {} );

    return m_instructions;
  }

  public void reset( )
  {
    m_instructions = null;
  }

  public boolean hasHeaderIcon( )
  {
    return getRuleType().isSetHeaderIcon();
  }

  public CellStyle getStyle( final IZmlModelCell cell ) throws CoreException
  {
    if( Objects.isNull( cell ) )
      return getBaseStyle();

    CellStyleType base = getBaseStyle().getType();

    final CellStyle implStyle = getImplementation().getCellStyle( this, cell );
    base = CellStyle.merge( base, implStyle.getType() );

    final AbstractZmlRuleInstructionType[] instructions = getInstructions();
    for( final AbstractZmlRuleInstructionType instruction : instructions )
    {
      try
      {
        if( instruction.matches( cell ) )
        {
          final CellStyle style = instruction.getStyle( cell );
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
}
