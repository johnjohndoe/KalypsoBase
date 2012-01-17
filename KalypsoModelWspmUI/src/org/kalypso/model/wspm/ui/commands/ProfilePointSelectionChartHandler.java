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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileWrapper;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;

import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class ProfilePointSelectionChartHandler extends AbstractProfilePointHandler
{

  public ProfilePointSelectionChartHandler( final IChartComposite chart )
  {
    super( chart );

    super.setCursor( SWT.CURSOR_CROSS );
  }

  @Override
  protected void doMouseMove( final AbstractProfilTheme theme, final Point position )
  {
    final IChartComposite chart = getChart();
    final Rectangle bounds = chart.getPlotRect();

    final EditInfo info = new EditInfo( theme, getHoverFigure( position, bounds ), null, getBreite(), null, null );
    setToolInfo( info );
  }

  private IPaintable getHoverFigure( final Point position, final Rectangle bounds )
  {
    final PolylineFigure figure = new PolylineFigure();
    final ILineStyle lineStyle = new LineStyle( 3, new RGB( 0x8D, 0xC3, 0xFC ), 180, 0F, new float[] { 2, 2, 2 }, LINEJOIN.MITER, LINECAP.ROUND, 1, true );
    figure.setStyle( lineStyle );

    figure.setPoints( new Point[] { new Point( position.x, 0 ), new Point( position.x, bounds.y + bounds.height ) } );

    return figure;
  }

  @Override
  public void mouseDown( final MouseEvent e )
  {
    super.mouseDown( e );

    final IChartComposite chart = getChart();
    final Rectangle bounds = chart.getPlotRect();

    final Point position = ChartHandlerUtilities.screen2plotPoint( new Point( e.x, e.y ), bounds );
    if( !isValid( bounds, position ) )
    {
      doReset();

      return;
    }

    final AbstractProfilTheme theme = findProfileTheme( chart );
    final ICoordinateMapper mapper = theme.getCoordinateMapper();

// setP0( mapper.getDomainAxis().screenToNumeric( position.x ).doubleValue() );
// setPoint( getProfile().hasPoint( getBreite().doubleValue(), 0.1 ) );

// doMouseMove( theme, position );
//
// if( Objects.isNull( getBreite(), getProfile() ) )
// return;

// final IProfil profile = getProfile().getProfile();

  }

  @Override
  public void paintControl( final PaintEvent e )
  {
    final ProfileWrapper profile = getProfile();
    if( Objects.isNull( profile ) )
      return;

    final IRangeSelection selection = profile.getProfile().getSelection();
    final Range<Double> range = selection.getRange();
    if( Objects.isNull( range ) )
      return;

    throw new UnsupportedOperationException();
  }
}
