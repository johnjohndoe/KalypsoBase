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
package org.kalypso.zml.ui.table.nat.editing;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.ui.matcher.CellPainterMouseEventMatcher;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.MouseEvent;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

/**
 * @author Dirk Kuch
 */
public class ZmlCellPainterMouseEventMatcher extends CellPainterMouseEventMatcher
{
  private final ZmlModelViewport m_viewport;

  public ZmlCellPainterMouseEventMatcher( final String regionName, final int button, final ICellPainter targetCellPainter, final ZmlModelViewport viewport )
  {
    super( regionName, button, targetCellPainter );
    m_viewport = viewport;
  }

  public ZmlCellPainterMouseEventMatcher( final String regionName, final int button, final Class< ? extends ICellPainter> targetCellPainterClass, final ZmlModelViewport viewport )
  {
    super( regionName, button, targetCellPainterClass );
    m_viewport = viewport;
  }

  /**
   * @see net.sourceforge.nattable.ui.matcher.CellPainterMouseEventMatcher#matches(net.sourceforge.nattable.NatTable,
   *      org.eclipse.swt.events.MouseEvent, net.sourceforge.nattable.layer.LabelStack)
   */
  @Override
  public boolean matches( final NatTable natTable, final MouseEvent event, final LabelStack regionLabels )
  {

    if( super.matches( natTable, event, regionLabels ) )
    {
      final int columnPosition = natTable.getColumnPositionByX( event.x );
      final IZmlModelColumn colum = m_viewport.getColum( columnPosition - 1 );

      final String valueAxis = colum.getDataColumn().getValueAxis();

      return StringUtils.equalsIgnoreCase( ITimeseriesConstants.TYPE_POLDER_CONTROL, valueAxis );
    }

    return false;
  }
}
