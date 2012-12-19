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
package de.openali.odysseus.chart.ext.base.layer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.model.figure.impl.FullRectangleFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.AreaStyle;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;

/**
 * Collect the paint rectangles and optimized painting.
 * 
 * @author Gernot Belger
 */
public class BarPaintManager
{
  /**
   * Callback for generation of tooltips. Needed, in order to create tooltips only for visible items, as string operations are usually slow.
   */
  public interface ITooltipCallback
  {
    String buildTooltip( EditInfo info );
  }

  private final HoverIndex m_index = new HoverIndex();

  private final Map<String, FullRectangleFigure> m_figures = new HashMap<>();

  private final GC m_gc;

  private final IStyleSet m_styles;

  private final IAreaStyle m_hoverStyle;

  private final Rectangle m_clipping;

  public BarPaintManager( final GC gc, final IStyleSet styles )
  {
    m_gc = gc;
    m_styles = styles;

    m_clipping = m_gc.getClipping();

    /* init hover style */
    // TODO: hard coded for now; should be either configured or derived from existing styles
    final RGB hoverColor = new RGB( 255, 0, 0 );
    final ILineStyle stroke = new LineStyle( 3, hoverColor, 255, 0.0f, null, LINEJOIN.BEVEL, LINECAP.ROUND, 0, true );
    m_hoverStyle = new AreaStyle( null, 255, stroke, false );
  }

  /**
   * Checks if the given rectangle is visible on the screen.<br/>
   * Public, because sometimes we want to check before the rectangle is added to this manager.
   */
  public boolean isInScreen( final Rectangle rectangle )
  {
    return m_clipping.intersects( rectangle );
  }

  /**
   * @param tooltipCallback
   *          If non-<code>null</code>, will be used to create the tooltip based on the currently set {@link EditInfo}. If <code>null</code>, the curretn tooltip will be used.
   */
  public void addRectangle( final BarRectangle paintRectangle, final ITooltipCallback tooltipCallback )
  {
    final Rectangle rectangle = paintRectangle.getRectangle();

    if( isInScreen( rectangle ) )
    {
      /* REMARK we can now set the hover figure for the edit info, as now the rectangle is known */
      final EditInfo info = paintRectangle.getEditInfo();

      if( info != null )
      {
        final FullRectangleFigure hoverFigure = getHoverFigure();
        final Rectangle hoverRect = RectangleUtils.bufferRect( rectangle, 1 );
        hoverFigure.setRectangle( hoverRect );

        /* build tooltip based on callback */
        final String currenTtooltip = info.getText();
        final String tooltip = tooltipCallback == null ? currenTtooltip : tooltipCallback.buildTooltip( info );

        final EditInfo infoWithFigure = new EditInfo( info.getLayer(), hoverFigure, null, info.getData(), tooltip, null );

        /* index this element */
        final Rectangle indexRect = RectangleUtils.bufferRect( hoverRect, 2 );
        m_index.addElement( indexRect, infoWithFigure );
      }

      /* paint element */
      final String[] styleNames = paintRectangle.getStyles();
      for( final String styleName : styleNames )
      {
        final FullRectangleFigure figure = getFigure( styleName );
        figure.setRectangle( rectangle );
        figure.paint( m_gc );
      }
    }
  }

  private FullRectangleFigure getHoverFigure( )
  {
    return new FullRectangleFigure( m_hoverStyle );
  }

  private FullRectangleFigure getFigure( final String styleName )
  {
    if( !m_figures.containsKey( styleName ) )
    {
      final IStyle style = m_styles.getStyle( styleName );
      if( style instanceof IAreaStyle )
      {
        final FullRectangleFigure figure = new FullRectangleFigure( (IAreaStyle)style );
        m_figures.put( styleName, figure );
      }
    }

    return m_figures.get( styleName );
  }

  public HoverIndex getIndex( )
  {
    return m_index;
  }
}