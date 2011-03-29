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
package org.kalypso.zml.ui.table.selection;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.chart.layer.themes.ZmlSelectionLayer;
import org.kalypso.zml.ui.table.model.IZmlTableRow;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class UpdateChartSelectionListener implements ISelectionChangedListener
{

  private final ZmlTableSelectionHandler m_handler;

  public UpdateChartSelectionListener( final ZmlTableSelectionHandler handler )
  {
    m_handler = handler;
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    final IServiceLocator serviceLocator = PlatformUI.getWorkbench();
    final IEvaluationService service = (IEvaluationService) serviceLocator.getService( IEvaluationService.class );
    final IChartComposite chart = ChartHandlerUtilities.getChart( service.getCurrentState() );
    if( Objects.isNull( chart ) )
      return;

    final ILayerManager layerManager = chart.getChartModel().getLayerManager();

    final IZmlTableRow[] selected = m_handler.getSelectedRows();
    final Date[] dates = convert( selected );

    if( ArrayUtils.isEmpty( dates ) )
      doResetSelection( layerManager );
    else if( dates.length == 1 )
      doSingleSelection( layerManager, dates[0] );
    else
      doMultiSelection( layerManager, dates );
  }

  private void doResetSelection( final ILayerManager layerManager )
  {
    layerManager.accept( new IChartLayerVisitor()
    {
      @Override
      public void visit( final IChartLayer layer )
      {
        if( layer instanceof ZmlSelectionLayer )
        {
          final ZmlSelectionLayer selection = (ZmlSelectionLayer) layer;
          selection.purgeSelection();
        }

        layer.getLayerManager().accept( this );
      }
    } );

  }

  private void doSingleSelection( final ILayerManager layerManager, final Date selected )
  {
    layerManager.accept( new IChartLayerVisitor()
    {
      @Override
      public void visit( final IChartLayer layer )
      {
        if( layer instanceof ZmlSelectionLayer )
        {
          final ZmlSelectionLayer selection = (ZmlSelectionLayer) layer;
          selection.setSelection( selected );
        }

        layer.getLayerManager().accept( this );
      }
    } );
  }

  private void doMultiSelection( final ILayerManager layerManager, final Date[] selected )
  {
    layerManager.accept( new IChartLayerVisitor()
    {
      @Override
      public void visit( final IChartLayer layer )
      {
        if( layer instanceof ZmlSelectionLayer )
        {
          final ZmlSelectionLayer selection = (ZmlSelectionLayer) layer;
          selection.setSelection( new DateRange( selected[0], selected[selected.length - 1] ) );
        }

        layer.getLayerManager().accept( this );
      }
    } );
  }

  private Date[] convert( final IZmlTableRow[] rows )
  {
    final Set<Date> dates = new TreeSet<Date>();

    for( final IZmlTableRow row : rows )
    {
      final IZmlModelRow modelRow = row.getModelRow();
      dates.add( (Date) modelRow.getIndexValue() );
    }

    return dates.toArray( new Date[] {} );
  }
}
