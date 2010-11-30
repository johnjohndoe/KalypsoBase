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
package org.kalypso.zml.ui.table.commands.menu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.ui.table.IZmlTableComposite;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandInterpolateValues extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    try
    {
      final IZmlTableComposite table = ZmlHandlerUtil.getTable( event );
      final IZmlModelRow[] selected = table.getSelectedRows();
      if( selected.length < 2 )
        throw new ExecutionException( "Interpolation fehlgeschlagen - selektieren Sie eine zweite Zelle!" );

      final IZmlValueReference cell = table.getActiveCell();
      final IZmlModelColumn activeColumn = cell.getColumn();

      final IZmlModelRow row1 = findIntervallStart( selected, activeColumn );
      final IZmlModelRow row2 = findIntervallEnd( selected, activeColumn );

      final IZmlValueReference reference1 = row1.get( activeColumn );
      final IZmlValueReference reference2 = row2.get( activeColumn );

      final Integer modelIndex1 = reference1.getTupleModelIndex();
      final Integer modelIndex2 = reference2.getTupleModelIndex();

      final Number value1 = (Number) reference1.getValue();
      final Number value2 = (Number) reference2.getValue();

      final int indexDifference = Math.abs( modelIndex2 - modelIndex1 );
      final double valueDiffernce = value2.doubleValue() - value1.doubleValue();

      final double stepValue = valueDiffernce / indexDifference;

      for( int index = modelIndex1 + 1; index < modelIndex2; index++ )
      {
        final int step = index - modelIndex1;
        final double value = value1.doubleValue() + step * stepValue;

        activeColumn.update( index, value );
      }

      return Status.OK_STATUS;
    }
    catch( final SensorException e )
    {
      throw new ExecutionException( "Interpolation fehlgeschlagen.", e );
    }
  }

  private IZmlModelRow findIntervallStart( final IZmlModelRow[] selected, final IZmlModelColumn activeColumn )
  {
    IZmlModelRow start = null;
    Integer startIndex = null;

    for( final IZmlModelRow row : selected )
    {
      if( start == null )
      {
        start = row;
        final IZmlValueReference reference = row.get( activeColumn );
        startIndex = reference.getTupleModelIndex();
      }
      else
      {
        final IZmlValueReference reference = row.get( activeColumn );
        final Integer index = reference.getTupleModelIndex();
        if( index < startIndex )
        {
          start = row;
          startIndex = index;
        }
      }
    }

    return start;
  }

  private IZmlModelRow findIntervallEnd( final IZmlModelRow[] selected, final IZmlModelColumn activeColumn )
  {
    IZmlModelRow end = null;
    Integer endIndex = null;

    for( final IZmlModelRow row : selected )
    {
      if( end == null )
      {
        end = row;
        final IZmlValueReference reference = row.get( activeColumn );
        endIndex = reference.getTupleModelIndex();
      }
      else
      {
        final IZmlValueReference reference = row.get( activeColumn );
        final Integer index = reference.getTupleModelIndex();
        if( index > endIndex )
        {
          end = row;
          endIndex = index;
        }
      }
    }

    return end;
  }
}
