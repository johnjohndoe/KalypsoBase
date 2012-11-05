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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer;

import de.openali.odysseus.chart.framework.model.IChartModel;
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
public class MousePositionChartHandler extends AbstractChartHandler
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
    title.setPositionVertical( ALIGNMENT.TOP );
    title.setTextAnchorY( ALIGNMENT.BOTTOM );

    final IChartModel model = chart.getChartModel();
    if( model == null )
    {
      final FontData fontData = JFaceResources.getTextFont().getFontData()[0];
      final RGB rgbFill = new RGB( 255, 255, 255 );
      final RGB rgbText = new RGB( 0, 0, 0 );
      title.setTextStyle( new TextStyle( fontData.getHeight(), fontData.getName(), rgbText, rgbFill, FONTSTYLE.NORMAL, FONTWEIGHT.NORMAL, ALIGNMENT.LEFT, 255, true ) );
    }
    else
    {
      title.setTextStyle( model.getSettings().getTextStyle() );
    }
    m_labelRenderer = new GenericChartLabelRenderer( title );
  }

  @SuppressWarnings( "rawtypes" )
  @Override
  public void mouseMove( final MouseEvent e )
  {
    final IChartComposite chart = getChart();
    final IProfilChartLayer theme = SelectionChartHandlerHelper.findProfileTheme( chart );
    String msg = ""; //$NON-NLS-1$
    if( theme != null )
    {
      final ICoordinateMapper mapper = theme.getCoordinateMapper();
      final Number hoehe = mapper.getTargetAxis().screenToNumeric( e.y );
      final Number breite = mapper.getDomainAxis().screenToNumeric( e.x );
      msg = String.format( Messages.getString( "MousePositionChartHandler_0" ), breite, hoehe ); //$NON-NLS-1$
    }
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
    if( chart == null || chart.getPlotInfo() == null )
    {
      return;
    }
    final Rectangle rect = chart.getPlotInfo().getLegendRect();
    m_labelRenderer.paint( e.gc, rect );
  }
}