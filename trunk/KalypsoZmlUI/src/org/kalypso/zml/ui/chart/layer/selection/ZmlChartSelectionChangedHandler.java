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
package org.kalypso.zml.ui.chart.layer.selection;

import java.util.Date;

import net.sourceforge.nattable.selection.command.SelectRowsCommand;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.chart.layer.themes.ZmlSelectionLayer;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.nat.layers.BodyLayerStack;
import org.kalypso.zml.ui.table.nat.pager.DateRangeVisitor;
import org.kalypso.zml.ui.table.nat.pager.FindClosestDateVisitor;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor2;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class ZmlChartSelectionChangedHandler extends AbstractChartSelectionListener
{
  public ZmlChartSelectionChangedHandler( final IEvaluationContext context )
  {
    super( context );
  }

  @Override
  public void selctionChanged( final Point... positions )
  {
    try
    {
      if( ArrayUtils.isEmpty( positions ) )
        return;

      final IChartComposite chart = getChartComposite();
      final IZmlTable table = getZmlTable();
      if( Objects.isNull( table ) )
        return;

      final Date[] dates = convert( chart, positions );
      if( ArrayUtils.isEmpty( dates ) )
        return;
      else if( dates.length == 1 )
        doSingleSelect( chart, table, dates[0] );
      else
        doMultiSelect( chart, table, dates );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }
  }

  private void doMultiSelect( final IChartComposite chart, final IZmlTable table, final Date[] selected )
  {
    final DateRange dateRange = new DateRange( selected[0], selected[selected.length - 1] );
    final DateRangeVisitor visitor = new DateRangeVisitor( dateRange );
    final ZmlModelViewport viewport = table.getModelViewport();
    viewport.accept( visitor );

    final IZmlModelRow[] rows = visitor.getRows();
    if( ArrayUtils.isNotEmpty( rows ) )
    {
      final IZmlModelRow r1 = rows[0];
      final IZmlModelRow rn = rows[rows.length - 1];

      final int i1 = ArrayUtils.indexOf( viewport.getRows(), r1 );
      final int in = ArrayUtils.indexOf( viewport.getRows(), rn );

      final BodyLayerStack bodyLayer = table.getBodyLayer();
      table.getTable().doCommand( new SelectRowsCommand( bodyLayer.getSelectionLayer(), 0, new int[] { i1, in }, true, false, i1 ) );
    }

    chart.getChartModel().accept( new IChartLayerVisitor2()
    {
      @Override
      public boolean getVisitDirection( )
      {
        return true;
      }

      @Override
      public boolean visit( final IChartLayer layer )
      {
        if( layer instanceof ZmlSelectionLayer )
        {
          final ZmlSelectionLayer selection = (ZmlSelectionLayer) layer;
          selection.setSelection( visitor.getDateRange() );
        }

        return true;
      }
    } );
  }

  private void doSingleSelect( final IChartComposite chart, final IZmlTable table, final Date selected )
  {
    if( Objects.isNull( table ) )
      return;

    final ZmlModelViewport viewport = table.getModelViewport();

    final FindClosestDateVisitor visitor = new FindClosestDateVisitor( selected );
    viewport.accept( visitor );

    final IZmlModelRow row = visitor.getRow();
    if( Objects.isNotNull( row ) )
    {
      final int index = ArrayUtils.indexOf( viewport.getRows(), row );

      final BodyLayerStack bodyLayer = table.getBodyLayer();

      table.getTable().doCommand( new SelectRowsCommand( bodyLayer.getSelectionLayer(), 0, index, false, false ) );
    }

    chart.getChartModel().accept( new IChartLayerVisitor2()
    {
      @Override
      public boolean getVisitDirection( )
      {
        return true;
      }

      @Override
      public boolean visit( final IChartLayer layer )
      {
        if( layer instanceof ZmlSelectionLayer )
        {
          final ZmlSelectionLayer selection = (ZmlSelectionLayer) layer;
          if( row != null )
            selection.setSelection( row.getIndex() );
        }

        return true;
      }
    } );
  }
}