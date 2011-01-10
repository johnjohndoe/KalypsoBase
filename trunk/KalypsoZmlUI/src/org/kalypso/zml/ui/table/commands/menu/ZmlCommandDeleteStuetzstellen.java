/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  DenickestraÃŸe 22
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
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;
import org.kalypso.ogc.sensor.timeseries.interpolation.InterpolationFilter;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.references.ZmlValueRefernceHelper;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandDeleteStuetzstellen extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event )
  {
    try
    {
      final IZmlTable table = ZmlHandlerUtil.getTable( event );
      final IZmlTableColumn column = table.getActiveColumn();
      if( column == null )
        throw new IllegalStateException( "Konnte aktive Spalte nicht ermitteln. Bitte Linkklick in der zu bearbeitenden Spalte ausführen und Aktion erneut versuchen." );

      final IZmlModelColumn modelColumn = column.getModelColumn();
      final ITupleModel model = modelColumn.getTupleModel();

      final IAxis statusAxis = AxisUtils.findStatusAxis( model.getAxisList() );
      final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( model.getAxisList() );
      final String src = String.format( "%s%s", DataSourceHelper.FILTER_SOURCE, InterpolationFilter.FILTER_ID ); //$NON-NLS-1$
      final DataSourceHandler dataSourceHandler = new DataSourceHandler( modelColumn.getMetadata() );

      final IZmlTableCell[] cells = column.getSelectedCells();
      for( final IZmlTableCell cell : cells )
      {
        try
        {
          final IZmlValueReference reference = cell.getValueReference();
          if( ZmlValueRefernceHelper.isStuetzstelle( reference ) )
          {
            model.set( reference.getTupleModelIndex(), statusAxis, KalypsoStati.BIT_CHECK );
            model.set( reference.getTupleModelIndex(), dataSourceAxis, dataSourceHandler.addDataSource( src, src ) );
          }
        }
        catch( final Throwable t )
        {
          KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
        }
      }

      // FIXME improve update value handling
      final IObservation observation = modelColumn.getObservation();
      observation.setValues( model );
      observation.fireChangedEvent( this );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return Status.OK_STATUS;

  }
}
