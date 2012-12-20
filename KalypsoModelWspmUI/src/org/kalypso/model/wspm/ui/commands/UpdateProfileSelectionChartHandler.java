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
package org.kalypso.model.wspm.ui.commands;

import org.apache.commons.lang3.Range;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler;
import org.kalypso.chart.ui.editor.mousehandler.EventUtils;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.util.resource.Pair;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Kim werner
 */
public class UpdateProfileSelectionChartHandler extends AbstractChartHandler
{
  private Integer m_p0 = null;

  private Integer m_p1 = null;

  private Integer m_p2 = null;

  public UpdateProfileSelectionChartHandler( final IChartComposite chart )
  {
    super( chart );
    super.setCursor( SWT.CURSOR_CROSS );
  }

  @Override
  public CHART_HANDLER_TYPE getType( )
  {
    return CHART_HANDLER_TYPE.eRadio;
  }

  @Override
  public void mouseDown( final MouseEvent e )
  {
    super.mouseDown( e );

    if( isOutOfRange( new Point( e.x, e.y ) ) )
    {
      return;
    }

    m_p0 = e.x;
    m_p1 = null;

  }

  @Override
  public void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );
    m_p2 = e.x;
    if( isOutOfRange( new Point( e.x, e.y ) ) )
    {
      m_p1 = null;
      m_p2 = null;
      return;
    }
    if( EventUtils.isStateButton1( e ) )
    {
      m_p1 = e.x;
    }
    else
    {
      SelectionChartHandlerHelper.updateCursor( getChart(), e.x );
    }
  }

  @SuppressWarnings( "rawtypes" )
  @Override
  public void mouseUp( final MouseEvent e )
  {
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( getChart() );
    if( theme == null )
    {
      m_p0 = null;
      m_p1 = null;
      return;
    }
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final IAxis domAxis = mapper.getDomainAxis();
    final IProfile profile = theme.getProfil();
    final boolean isClicked = m_p1 == null || Math.abs( m_p0 - m_p1 ) < 5;
    final Double x1 = domAxis.screenToNumeric( m_p0 );
    final Double x2 = isClicked ? x1 : domAxis.screenToNumeric( m_p1 );
    final IRangeSelection selection = profile.getSelection();

    if( isClicked )
    {
      selection.setRange( Range.is( x1 ) );
    }
    else
    {
      selection.setRange( Range.between( x1, x2 ) );
    }
    m_p0 = null;
    m_p1 = null;
  }

  @Override
  public void paintControl( final PaintEvent e )
  {
    super.paintControl( e );
    final IChartComposite chart = getChart();
    SelectionChartHandlerHelper.paintMouse( chart, e, m_p2 );
    final Pair<Integer, Integer> selection = SelectionChartHandlerHelper.selectionToScreen( chart );
    if( selection != null )
    {
      SelectionChartHandlerHelper.paintSelection( chart, e, selection.getDomain(), selection.getTarget() );
    }
    if( m_p0 == null || m_p1 == null )
    {
      return;
    }
    SelectionChartHandlerHelper.paintRange( getChart(), e, m_p0, m_p1 );

  }
}
