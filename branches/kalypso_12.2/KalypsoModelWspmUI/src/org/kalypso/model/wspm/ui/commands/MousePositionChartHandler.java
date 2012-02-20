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

import java.awt.Insets;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;
import de.openali.odysseus.chart.framework.model.style.impl.TextStyle;
import de.openali.odysseus.chart.framework.util.img.GenericChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class MousePositionChartHandler extends AbstractProfilePointHandler
{
  private final IChartLabelRenderer m_labelRenderer;

  public MousePositionChartHandler( final IChartComposite chart )
  {
    super( chart );

    final TitleTypeBean title = new TitleTypeBean();
    title.setInsets( new Insets( 2, 2, 2, 2 ) );
    title.setRotation( 0 );

    title.setPositionHorizontal( ALIGNMENT.RIGHT );
    title.setTextAnchorX( ALIGNMENT.RIGHT );
    title.setPositionVertical( ALIGNMENT.BOTTOM );
    title.setTextAnchorY( ALIGNMENT.BOTTOM );

    // FIXME: use font of chart
    final FontData fontData = JFaceResources.getTextFont().getFontData()[0];

    final RGB rgbFill = new RGB( 255, 255, 255 );
    final RGB rgbText = new RGB( 0, 0, 0 );
    title.setTextStyle( new TextStyle( fontData.getHeight(), fontData.getName(), rgbText, rgbFill, FONTSTYLE.NORMAL, FONTWEIGHT.NORMAL, ALIGNMENT.LEFT, 255, true ) );

    m_labelRenderer = new GenericChartLabelRenderer( title );
  }

  @Override
  protected void doMouseMove( final AbstractProfilTheme theme, final Point position )
  {
    final ICoordinateMapper mapper = theme.getCoordinateMapper();
    final Number hoehe = mapper.getTargetAxis().screenToNumeric( position.y );

    final String msg = String.format( Messages.getString("MousePositionChartHandler_0"), getBreite(), hoehe ); //$NON-NLS-1$

    m_labelRenderer.getTitleTypeBean().setLabel( msg );

    forceRedrawEvent();
  }

  @Override
  public CHART_HANDLER_TYPE getType( )
  {
    return CHART_HANDLER_TYPE.eBackground;
  }

  @Override
  public void paintControl( final PaintEvent e )
  {
    final IChartComposite chart = getChart();

    if( chart instanceof Composite )
    {
      // FIXME: it should not be necessary to cast in order to get the screen bounds
      final Rectangle bounds = ((Composite) chart).getBounds();
      final GC gc = e.gc;

      // FIXME: necessary to reset the transformation because of the transformation in
      // ChartImageComposite#handlePaint (which makes no sense, see comment there).

      final Transform oldTransform = new Transform( gc.getDevice() );
      final Transform transform = new Transform( gc.getDevice() );

      gc.getTransform( oldTransform );
      gc.setTransform( transform );

      m_labelRenderer.paint( gc, bounds );

      gc.setTransform( oldTransform );

      oldTransform.dispose();
      transform.dispose();
    }
  }
}