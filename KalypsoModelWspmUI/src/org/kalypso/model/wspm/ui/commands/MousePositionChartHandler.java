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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileWrapper;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.util.img.ChartTooltipPainter;
import de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class MousePositionChartHandler extends AbstractProfilePointHandler
{

  public MousePositionChartHandler( final IChartComposite chart )
  {
    super( chart );
  }

  @Override
  public void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );

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

    setProfile( new ProfileWrapper( theme.getProfil() ) );
    setBreite( mapper.getDomainAxis().screenToNumeric( position.x ).doubleValue() );
    setPoint( getProfile().hasPoint( getBreite().doubleValue(), 0.1 ) );

    if( isOutOfRange() )
    {
      doReset();
      return;
    }

    final Number hoehe = mapper.getTargetAxis().screenToNumeric( position.y );

    final StringBuilder builder = new StringBuilder();
    builder.append( "Position: " );
    builder.append( String.format( "Breite %6.2f m, ", getBreite() ) );
    builder.append( String.format( "Höhe %6.2f m", hoehe ) );

    final String msg = builder.toString();
    final Point p = calculatePosition( chart, msg );

    final EditInfo info = new EditInfo( theme, null, null, getBreite(), msg, p );
    setToolInfo( info );
  }

  private Point calculatePosition( final IChartComposite composite, final String msg )
  {
    final Composite c = (Composite) composite;
    final Rectangle bounds = c.getBounds();

    final ChartTooltipPainter painter = getTooltipPainter();

    final IChartLabelRenderer renderer = painter.getLabelRenderer();
    renderer.getTitleTypeBean().setText( msg );

    final Rectangle size = renderer.getSize(); // FIXME ask kim
    final int x = bounds.width - size.width / 4;
    final int y = bounds.y + bounds.height;

    return new Point( x, y );
  }

  private boolean isValid( final Rectangle bounds, final Point position )
  {
    if( position.x < 0 )
      return false;
    else if( position.x > bounds.width )
      return false;

    return true;
  }

  @Override
  public CHART_HANDLER_TYPE getType( )
  {
    return CHART_HANDLER_TYPE.eBackground;
  }

}
