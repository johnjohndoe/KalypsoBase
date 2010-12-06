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
package org.kalypso.zml.ui.table.provider.strategy;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.binding.ZmlRule;
import org.kalypso.zml.ui.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.model.walker.ZmlModelWalker;
import org.kalypso.zml.ui.table.provider.IZmlLabelProvider;
import org.kalypso.zml.ui.table.rules.IZmlRuleImplementation;
import org.kalypso.zml.ui.table.viewmodel.IZmlTableCell;
import org.kalypso.zml.ui.table.viewmodel.IZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class SumValueLabelingStrategy extends AbstractValueLabelingStrategy implements IZmlLabelStrategy
{

  public SumValueLabelingStrategy( final IZmlLabelProvider provider )
  {
    super( provider );
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.strategy.IZmlLabelStrategy#getText(org.kalypso.zml.ui.table.model.IZmlModelRow)
   */
  @Override
  public String getText( final IZmlModelRow row ) throws SensorException, CoreException
  {
    final IZmlTable table = getTable();
    final int resolution = table.getResolution();
    if( resolution == 0 )
    {
      return getAsOriginValue( row );
    }

    return getAsAggregatedValue( row );
  }

  private String getAsAggregatedValue( final IZmlModelRow row ) throws CoreException, SensorException
  {
    final IZmlTableColumn column = getTableColumn( row );

    final IZmlTableCell current = column.findCell( row );
    final IZmlTableCell previous = current.findPreviousCell();

    if( previous == null )
      return getAsOriginValue( row );

    final IZmlValueReference previousReference = previous.getValueReference();
    final IZmlValueReference currentReference = current.getValueReference();

    final int startIndex = previousReference.getTupleModelIndex() + 1;
    final int endIndex = currentReference.getTupleModelIndex();

    final SumOperation operation = new SumOperation();

    final ZmlModelWalker walker = new ZmlModelWalker( column.getModelColumn() );
    walker.walk( operation, startIndex, endIndex );

    final Double value = operation.getValue();

    String text = format( row, value );

    final ZmlRule[] rules = findActiveRules( row );
    for( final ZmlRule rule : rules )
    {
      try
      {
        final IZmlRuleImplementation impl = rule.getImplementation();
        text = impl.update( rule, currentReference, text );
      }
      catch( final SensorException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return text;
  }

  private String getAsOriginValue( final IZmlModelRow row ) throws CoreException, SensorException
  {
    final IZmlValueReference reference = getReference( row );
    if( reference == null )
      return "";

    String text = format( row, reference.getValue() );

    final ZmlRule[] rules = findActiveRules( row );
    for( final ZmlRule rule : rules )
    {
      try
      {
        final IZmlRuleImplementation impl = rule.getImplementation();
        text = impl.update( rule, reference, text );
      }
      catch( final SensorException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return text;
  }

}
