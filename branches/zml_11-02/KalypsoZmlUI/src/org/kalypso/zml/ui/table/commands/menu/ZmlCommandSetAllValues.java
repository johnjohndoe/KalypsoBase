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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.interpolation.ZmlInterpolationWorker;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.transaction.ZmlModelTransaction;
import org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.model.cells.IZmlTableCell;
import org.kalypso.zml.ui.table.model.cells.IZmlTableValueCell;
import org.kalypso.zml.ui.table.model.columns.IZmlTableValueColumn;
import org.kalypso.zml.ui.table.provider.ZmlLabelProvider;
import org.kalypso.zml.ui.table.provider.strategy.editing.IZmlEditingStrategy;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandSetAllValues extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    try
    {
      final IZmlTable table = ZmlHandlerUtil.getTable( event );
      final IZmlTableSelectionHandler selection = table.getSelectionHandler();
      final IZmlTableValueCell active = (IZmlTableValueCell) selection.findActiveCellByPosition();

      final IZmlModelValueCell base = active.getValueReference();
      final IZmlTableValueColumn column = active.getColumn();

      final IZmlEditingStrategy strategy = column.getEditingStrategy();
      if( strategy.isAggregated() )
      {
        final ZmlLabelProvider provider = new ZmlLabelProvider( base.getRow(), column, new ZmlCellRule[] {} );
        final String targetValue = provider.getText();

        final IZmlTableCell[] visibleCells = column.getCells();
        for( final IZmlTableCell cell : visibleCells )
        {
          if( cell instanceof IZmlTableValueCell )
            strategy.setValue( ((IZmlTableValueCell) cell).getRow().getModelRow(), targetValue );
        }
      }
      else
      {
        final IZmlModelValueCell reference = active.getValueReference();
        final Number targetValue = reference.getValue();

        final ZmlModelTransaction transaction = new ZmlModelTransaction();

        column.getModelColumn().accept( new IZmlModelColumnVisitor()
        {
          @Override
          public void visit( final IZmlModelValueCell ref )
          {
            transaction.add( ref, targetValue, IDataSourceItem.SOURCE_MANUAL_CHANGED, KalypsoStati.BIT_USER_MODIFIED );
          }
        } );

        transaction.execute();
      }

      try
      {
        /**
         * reinterpolate complete observation because of table view filter (like 12h view, stueztstellen ansicht, etc)
         */
        final IObservation observation = column.getModelColumn().getObservation();
        final ZmlInterpolationWorker interpolationWorker = new ZmlInterpolationWorker( observation );
        interpolationWorker.execute( new NullProgressMonitor() );
      }
      catch( final CoreException e )
      {
        e.printStackTrace();
      }

      return Status.OK_STATUS;
    }
    catch( final Exception e )
    {
      throw new ExecutionException( "Aktualisieren der Werte fehlgeschlagen.", e );
    }
  }
}
