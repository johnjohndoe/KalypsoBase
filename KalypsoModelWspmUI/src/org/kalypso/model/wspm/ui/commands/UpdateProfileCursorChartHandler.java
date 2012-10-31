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
package org.kalypso.model.wspm.ui.commands;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.util.resource.Pair;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Kim Werner
 */
public class UpdateProfileCursorChartHandler extends AbstractChartHandler
{
  private Integer m_p1 = null;

  private Integer m_pMin = null;

  private Integer m_pMax = null;

  public UpdateProfileCursorChartHandler( final IChartComposite chart )
  {
    super( chart );

  }

  private final void getSelectionCursor( )
  {
    m_p1 = SelectionChartHandlerHelper.cursorToScreen( getChart() );
    final Pair<Integer, Integer> selection = SelectionChartHandlerHelper.selectionToScreen( getChart() );

    m_pMin = selection == null ? null : selection.getDomain();
    m_pMax = selection == null ? null : selection.getTarget();
  }

  @Override
  public CHART_HANDLER_TYPE getType( )
  {
    return CHART_HANDLER_TYPE.eToggle;
  }

  @SuppressWarnings( { "unused", "rawtypes" } )
  @Override
  public void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );
    if( isOutOfRange( new Point( e.x, e.y ) ) )
    {
      return;
    }
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( getChart() );
    final IChartComposite chart = getChart();
    if( theme == null )
    {
      return;
    }
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domAxis = mapper.getDomainAxis();
    final IProfile profile = theme.getProfil();
    final Double x2 = domAxis.screenToNumeric( e.x );
    final IRangeSelection selection = profile.getSelection();
    selection.setCursor( x2 );
  }

  @Override
  public void paintControl( final PaintEvent e )
  {
    super.paintControl( e );
    getSelectionCursor();
    SelectionChartHandlerHelper.paintMouse( getChart(), e, m_p1 );
    SelectionChartHandlerHelper.paintSelection( getChart(), e, m_pMin, m_pMax );
    m_p1 = null;
    m_pMin = null;
    m_pMax = null;
  }
}
