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
package org.kalypso.zml.core.table.rules.impl.forecast;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.binding.rule.ZmlRule;
import org.kalypso.zml.core.table.binding.rule.instructions.AbstractZmlRuleInstructionType;
import org.kalypso.zml.core.table.binding.rule.instructions.ZmlMetadataDaterangeInstruction;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;

/**
 * @author Dirk Kuch
 */
public class ZmlRuleForecast extends AbstractForecastRule
{

  /**
   * @see org.kalypso.zml.core.table.rules.IZmlRuleImplementation#getIdentifier()
   */
  @Override
  public String getIdentifier( )
  {
    return "org.kalypso.zml.ui.core.rule.forecast.value"; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.zml.core.table.rules.impl.AbstractZmlTableRule#doApply(org.kalypso.zml.core.table.binding.rule.ZmlRule,
   *      org.kalypso.zml.core.table.model.references.IZmlValueReference)
   */
  @Override
  protected boolean doApply( final ZmlRule rule, final IZmlValueReference reference )
  {
    final AbstractZmlRuleInstructionType[] instructions = rule.getInstructions();
    for( final AbstractZmlRuleInstructionType instruction : instructions )
    {
      if( !(instruction instanceof ZmlMetadataDaterangeInstruction) )
      {
        continue;
      }

      try
      {
        final ZmlMetadataDaterangeInstruction impl = (ZmlMetadataDaterangeInstruction) instruction;
        if( impl.matches( reference ) )
          return true;
      }
      catch( final SensorException e )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return false;
  }

}
