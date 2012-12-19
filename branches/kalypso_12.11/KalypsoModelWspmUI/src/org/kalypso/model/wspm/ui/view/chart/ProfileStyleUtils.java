/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.ui.view.chart;

import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;

/**
 * Helper for styling profile layers.
 * 
 * @author Gernot Belger
 */
public final class ProfileStyleUtils
{
  private static final float[] HOVER_DASH = new float[] { 1, 1, 1 };

  private ProfileStyleUtils( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Derive a hover style form an existing point style. Works best for solid dots.
   */
  public static IPointStyle deriveHoverStyle( final IPointStyle style )
  {
    final IPointStyle hoverStyle = style.clone();

    /* double size, less transparent */
    hoverStyle.setWidth( style.getWidth() + 5 );
    hoverStyle.setHeight( style.getHeight() + 5 );

    hoverStyle.setAlpha( Math.max( 255, style.getAlpha() * 2 ) );

    /* solid stroke, less transparent */
    final ILineStyle stroke = style.getStroke();

    final ILineStyle hoverStroke = stroke.clone();
    hoverStroke.setDash( 0.0f, null );
    hoverStroke.setAlpha( Math.max( 255, stroke.getAlpha() * 2 ) );
    hoverStroke.setWidth( stroke.getWidth() + 1 );

    hoverStyle.setStroke( hoverStroke );

    /* empty fill */
    hoverStyle.setFillVisible( true );
    hoverStyle.setInlineColor( new RGB( 255, 255, 255 ) );

    return hoverStyle;
  }

  public static ILineStyle deriveHoverStyle( final ILineStyle lineStyle )
  {
    final ILineStyle hoverStyle = lineStyle.clone();

    hoverStyle.setDash( 0f, HOVER_DASH );
    hoverStyle.setLineCap( LINECAP.FLAT );

    hoverStyle.setWidth( lineStyle.getWidth() + 3 );

    return hoverStyle;
  }
}