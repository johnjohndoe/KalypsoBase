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
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.figure.impl.FullRectangleFigure;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

/**
 * Collect the paint rectangles and optimized painting.
 *
 * @author Gernot Belger
 */
public class BarPaintManager
{
  private final RectangleIndex<BarRectangle> m_index = new RectangleIndex<>();

  private final Map<String, FullRectangleFigure> m_figures = new HashMap<>();

  private final GC m_gc;

  private final IStyleSet m_styles;

  public BarPaintManager( final GC gc, final IStyleSet styles )
  {
    m_gc = gc;
    m_styles = styles;
  }

  /**
   * Checks if the given rectangle is visible on the screen.<br/>
   * Public, because sometimes we want to check before the rectangle is added to this manager.
   */
  public boolean isInScreen( final Rectangle rectangle )
  {
    final Rectangle clipping = m_gc.getClipping();
    return clipping.intersects( rectangle );
  }

  public void addRectangle( final BarRectangle paintRectangle )
  {
    final Rectangle rectangle = paintRectangle.getRectangle();

    if( isInScreen( rectangle ) )
    {
      /* index this element */
      m_index.addElement( paintRectangle );

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

  private FullRectangleFigure getFigure( final String styleName )
  {
    if( !m_figures.containsKey( styleName ) )
    {
      final IStyle style = m_styles.getStyle( styleName );
      if( style instanceof IAreaStyle )
      {
        final FullRectangleFigure figure = new FullRectangleFigure();
        figure.setStyle( (IAreaStyle) style );
        m_figures.put( styleName, figure );
      }
    }

    return m_figures.get( styleName );
  }

  public RectangleIndex<BarRectangle> getIndex( )
  {
    return m_index;
  }
}