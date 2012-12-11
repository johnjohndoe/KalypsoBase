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
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.chart.ui.editor.mousehandler.IChartSelectionChangedListener;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableComposite;
import org.kalypso.zml.ui.table.context.TableSourceUtility;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractChartSelectionListener implements IChartSelectionChangedListener
{
  private final IEvaluationContext m_context;

  public AbstractChartSelectionListener( final IEvaluationContext context )
  {
    m_context = context;
  }

  protected IChartComposite getChartComposite( )
  {
    return ChartHandlerUtilities.getChart( m_context );
  }

  protected IZmlTable getZmlTable( )
  {
    final IZmlTableComposite table = TableSourceUtility.getTable( m_context );
    if( table == null )
      return null;

    return table.getTable();
  }

  protected Date[] convert( final IChartComposite chart, final Point... positions )
  {
    final IAxis axis = chart.getChartModel().getAxisRegistry().getAxis( ITimeseriesConstants.TYPE_DATE ); //$NON-NLS-1$

    final Set<Date> dates = new TreeSet<>();
    for( final Point position : positions )
    {
      final Number screenToNumeric = axis.screenToNumeric( position.x );
      dates.add( new Date( screenToNumeric.longValue() ) );
    }

    return dates.toArray( new Date[] {} );
  }
}
