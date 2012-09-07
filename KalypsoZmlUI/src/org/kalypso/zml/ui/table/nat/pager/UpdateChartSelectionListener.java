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
package org.kalypso.zml.ui.table.nat.pager;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.selection.event.CellSelectionEvent;
import net.sourceforge.nattable.selection.event.RowSelectionEvent;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.chart.layer.themes.ZmlSelectionLayer;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.AbstractChartLayerVisitor;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class UpdateChartSelectionListener implements ILayerListener
{

  private final IZmlTableSelection m_selection;

  public UpdateChartSelectionListener( final IZmlTableSelection selection )
  {
    m_selection = selection;
  }

  @Override
  public void handleLayerEvent( final ILayerEvent event )
  {
    if( isSelectionChangeEvent( event ) )
    {
      final IServiceLocator serviceLocator = PlatformUI.getWorkbench();
      final IEvaluationService service = (IEvaluationService) serviceLocator.getService( IEvaluationService.class );
      final IChartComposite chart = ChartHandlerUtilities.getChart( service.getCurrentState() );
      if( Objects.isNull( chart ) )
        return;

      final ILayerManager layerManager = chart.getChartModel().getLayerManager();
      final IZmlModelRow[] rows = m_selection.getSelectedRows();
      final Date[] selection = convert( rows );

      if( ArrayUtils.isEmpty( selection ) )
        doResetSelection( layerManager );
      else if( selection.length == 1 )
        doSingleSelection( layerManager, selection[0] );
      else
        doMultiSelection( layerManager, selection );
    }

  }

  private boolean isSelectionChangeEvent( final ILayerEvent event )
  {
    if( event instanceof RowSelectionEvent )
      return true;
    else if( event instanceof CellSelectionEvent )
      return true;

    return false;
  }

  private void doResetSelection( final ILayerManager layerManager )
  {
    layerManager.accept( new AbstractChartLayerVisitor()
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
    layerManager.accept( new AbstractChartLayerVisitor()
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
    layerManager.accept( new AbstractChartLayerVisitor()
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

  protected Date[] convert( final IZmlModelRow[] rows )
  {
    final Set<Date> dates = new TreeSet<>();

    for( final IZmlModelRow row : rows )
    {
      dates.add( row.getIndex() );
    }

    return dates.toArray( new Date[] {} );
  }
}
