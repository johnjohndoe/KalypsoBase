/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.zml.ui.table.debug;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.zml.core.table.binding.rule.AbstractZmlRule;
import org.kalypso.zml.core.table.binding.rule.instructions.AbstractZmlRuleInstructionType;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.rules.AppliedRule;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.RuleRefernceType;

/**
 * @author Dirk Kuch
 */
public class DebugZmlModelLabelProvider extends LabelProvider implements IBaseLabelProvider
{
  private static final Image IMG_TABLE_COLUMN = new Image( null, DebugZmlModelLabelProvider.class.getResourceAsStream( "icons/table.png" ) ); //$NON-NLS-1$

  private static final Image IMG_MODEL_COLUMN = new Image( null, DebugZmlModelLabelProvider.class.getResourceAsStream( "icons/model.png" ) ); //$NON-NLS-1$

  private static final Image IMG_AXIS = new Image( null, DebugZmlModelLabelProvider.class.getResourceAsStream( "icons/achsen.png" ) ); //$NON-NLS-1$

  private static final Image IMG_RULE = new Image( null, DebugZmlModelLabelProvider.class.getResourceAsStream( "icons/regel.png" ) ); //$NON-NLS-1$

  @Override
  public Image getImage( final Object element )
  {
    if( element instanceof IZmlModelColumn )
      return IMG_MODEL_COLUMN;
    else if( element instanceof IAxis )
      return IMG_AXIS;
    else if( element instanceof AbstractZmlRule )
      return IMG_RULE;
    else if( element instanceof AppliedRule )
      return IMG_RULE;

    return super.getImage( element );
  }

  @Override
  public String getText( final Object element )
  {
    if( element instanceof IZmlModelColumn )
    {
      final IZmlModelColumn column = (IZmlModelColumn) element;

      return String.format( "ZmlModelColumn: id=%s, label=%s", column.getIdentifier(), column.getLabel() );
    }
    else if( element instanceof AbstractColumnType )
    {
      final AbstractColumnType type = (AbstractColumnType) element;

      return String.format( "ColumnType: id=%s", type.getId() );
    }
    else if( element instanceof RuleRefernceType )
    {
      final RuleRefernceType reference = (RuleRefernceType) element;

      return String.format( "Rule Reference: %s", reference.getUrl() );
    }
    else if( element instanceof IAxis )
    {
      final IAxis axis = (IAxis) element;

      return String.format( "Achse: id=%s, label=%s, type=%s", axis.getType(), axis.getName(), axis.getUnit() );
    }
    else if( element instanceof AbstractZmlRule )
    {
      final AbstractZmlRule rule = (AbstractZmlRule) element;

      return String.format( "Regel: %s", rule.getIdentifier() );
    }
    else if( element instanceof AbstractZmlRuleInstructionType )
    {
      final AbstractZmlRuleInstructionType instruction = (AbstractZmlRuleInstructionType) element;

      return String.format( "Instruction: %s", instruction.getType().toString() );
    }
    else if( element instanceof AppliedRule )
    {
      final AppliedRule applied = (AppliedRule) element;

      return String.format( "AppliedRule: %s", applied.getLabel() );
    }
    return super.getText( element );
  }
}
