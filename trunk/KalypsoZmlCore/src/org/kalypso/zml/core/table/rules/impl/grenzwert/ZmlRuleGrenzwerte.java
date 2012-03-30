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
package org.kalypso.zml.core.table.rules.impl.grenzwert;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.binding.rule.instructions.AbstractZmlRuleInstructionType;
import org.kalypso.zml.core.table.binding.rule.instructions.ZmlMetadataBoundaryInstruction;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.rules.AbstractZmlCellRuleImplementation;

/**
 * @author Dirk Kuch
 */
public class ZmlRuleGrenzwerte extends AbstractZmlCellRuleImplementation
{
  public static final String ID = "org.kalypso.zml.ui.core.rule.grenzwerte"; //$NON-NLS-1$

  @Override
  protected boolean doApply( final ZmlCellRule rule, final IZmlModelCell reference )
  {
    if( !(reference instanceof IZmlModelValueCell) )
      return false;

    final AbstractZmlRuleInstructionType[] instructions = rule.getInstructions();
    for( final AbstractZmlRuleInstructionType instruction : instructions )
    {
      try
      {
        if( instruction.matches( reference ) )
          return true;
      }
      catch( final SensorException e )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return false;
  }

  @Override
  public CellStyle getCellStyle( final ZmlCellRule rule, final IZmlModelCell reference ) throws CoreException
  {
    if( !(reference instanceof IZmlModelValueCell) )
      return rule.getPlainStyle();

    final AbstractZmlRuleInstructionType[] instructions = rule.getInstructions();
    for( final AbstractZmlRuleInstructionType instruction : instructions )
    {
      try
      {
        if( instruction.matches( reference ) )
          return instruction.getStyle( reference );
      }
      catch( final SensorException e )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return rule.getPlainStyle();
  }

  @Override
  public String getLabel( final ZmlCellRule rule, final IZmlModelCell reference )
  {
    if( !(reference instanceof IZmlModelValueCell) )
      return rule.getRuleType().getLabel();

    final AbstractZmlRuleInstructionType[] instructions = rule.getInstructions();
    for( final AbstractZmlRuleInstructionType instruction : instructions )
    {
      try
      {
        if( instruction.matches( reference ) )

          return instruction.getLabel( rule );

      }
      catch( final SensorException e )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return rule.getRuleType().getLabel();
  }

  @Override
  public String getIdentifier( )
  {
    return ID;
  }

  @Override
  public String update( final ZmlCellRule rule, final IZmlModelCell reference, final String text ) throws SensorException
  {
    if( !(reference instanceof IZmlModelValueCell) )
      return text;

    final AbstractZmlRuleInstructionType[] instructions = rule.getInstructions();
    for( final AbstractZmlRuleInstructionType instruction : instructions )
    {
      if( instruction instanceof ZmlMetadataBoundaryInstruction )
      {
        final ZmlMetadataBoundaryInstruction impl = (ZmlMetadataBoundaryInstruction) instruction;
        if( instruction.matches( reference ) )
          return impl.update( text );
      }
    }

    return text;
  }

  @Override
  public Double getSeverity( final ZmlCellRule rule, final IZmlModelCell reference )
  {
    if( !(reference instanceof IZmlModelValueCell) )
      return 1.0;

    final AbstractZmlRuleInstructionType[] instructions = rule.getInstructions();
    for( final AbstractZmlRuleInstructionType instruction : instructions )
    {
      try
      {
        if( instruction.matches( reference ) )

          return instruction.getSeverity();

      }
      catch( final SensorException e )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return 1.0;
  }
}
