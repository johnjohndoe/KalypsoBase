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
package org.kalypso.zml.core.table.model.references;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.core.table.rules.IZmlCellRuleImplementation;

/**
 * @author Dirk Kuch
 */
public class SumValueLabelingStrategy extends AbstractValueLabelingStrategy
{

  public SumValueLabelingStrategy( )
  {
  }

  @Override
  public String getText( final ZmlModelViewport viewport, final IZmlModelValueCell cell ) throws SensorException, CoreException
  {
    final int resolution = viewport.getResolution();
    if( resolution == 0 )
    {
      return getAsOriginValue( viewport, cell, true );
    }

    return getAsAggregatedValue( viewport, cell, true );
  }

  @Override
  public String getPlainText( final ZmlModelViewport viewport, final IZmlModelValueCell cell ) throws SensorException, CoreException
  {
    final int resolution = viewport.getResolution();
    if( resolution == 0 )
    {
      return getAsOriginValue( viewport, cell, false );
    }

    return getAsAggregatedValue( viewport, cell, false );
  }

  private String getAsAggregatedValue( final ZmlModelViewport viewport, final IZmlModelValueCell current, final boolean doApplyRules ) throws CoreException, SensorException
  {
    final IZmlModel zml = viewport.getModel();
    final IZmlModelColumn column = current.getColumn();

    IZmlModelValueCell previous = viewport.findPreviousCell( current );
    if( previous == null )
    {
      /* get first invisible value (first value will is not part of the table!) */

      final IZmlModelRow baseRow = zml.getRowAt( 0 );
      previous = baseRow.get( column );
    }
    else
    {
      final Integer index = previous.getModelIndex();
      final IZmlModelRow baseRow = zml.getRowAt( index + 1 );
      previous = baseRow.get( column );
    }

    if( previous == null )
      return null;

    final DateRange daterange = new DateRange( previous.getIndexValue(), current.getIndexValue() );

    final SumValuesVisitor visitor = new SumValuesVisitor();
    column.accept( visitor, daterange );

    final Double value = visitor.getValue();

    final CellStyle style = current.getStyle( viewport );
    String text = String.format( style.getTextFormat(), value );

    if( !doApplyRules )
      return text;

    final ZmlCellRule[] rules = current.findActiveRules( viewport );
    for( final ZmlCellRule rule : rules )
    {
      try
      {
        final IZmlCellRuleImplementation impl = rule.getImplementation();
        text = impl.update( rule, current, text );
      }
      catch( final SensorException e )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return text;
  }

  private String getAsOriginValue( final ZmlModelViewport viewport, final IZmlModelValueCell cell, final boolean doApplyRules ) throws CoreException, SensorException
  {
    final CellStyle style = cell.getStyle( viewport );

    String text = String.format( style.getTextFormat(), cell.getValue() );

    if( !doApplyRules )
      return text;

    final ZmlCellRule[] rules = cell.findActiveRules( viewport );
    for( final ZmlCellRule rule : rules )
    {
      try
      {
        final IZmlCellRuleImplementation impl = rule.getImplementation();
        text = impl.update( rule, cell, text );
      }
      catch( final SensorException e )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return text;
  }

}
