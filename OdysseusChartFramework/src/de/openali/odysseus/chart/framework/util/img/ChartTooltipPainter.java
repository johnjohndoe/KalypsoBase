/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package de.openali.odysseus.chart.framework.util.img;

import java.awt.Insets;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;
import de.openali.odysseus.chart.framework.model.style.impl.ColorFill;
import de.openali.odysseus.chart.framework.model.style.impl.TextStyle;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author kimwerner
 */
public class ChartTooltipPainter
{
  private final IChartLabelRenderer m_labelRenderer;

  public ChartTooltipPainter( final IChartLabelRenderer labelRenderer )
  {
    m_labelRenderer = labelRenderer;
  }

  public ChartTooltipPainter( )
  {
    final TitleTypeBean titleType = new TitleTypeBean( null );
    titleType.setInsets( new Insets( 2, 2, 2, 2 ) );
    titleType.setRotation( 0 );
    final FontData fontData = JFaceResources.getTextFont().getFontData()[0];
    final Display display = ChartUtilities.getDisplay();
    final RGB rgbFill = display.getSystemColor( SWT.COLOR_INFO_BACKGROUND ).getRGB();
    final RGB rgbText = display.getSystemColor( SWT.COLOR_INFO_FOREGROUND ).getRGB();
    titleType.setTextStyle( new TextStyle( fontData.getHeight(), fontData.getName(), rgbText, rgbFill, FONTSTYLE.NORMAL, FONTWEIGHT.NORMAL, ALIGNMENT.LEFT, 255, true ) );

    m_labelRenderer = new GenericChartLabelRenderer( titleType );
    final IAreaStyle borderStyle = StyleUtils.getDefaultAreaStyle();
    borderStyle.getStroke().setColor( new RGB( 0, 0, 0 ) );
    borderStyle.getStroke().setWidth( 1 );
    borderStyle.setFill( new ColorFill( rgbFill ) );
    m_labelRenderer.setBorderStyle( borderStyle );
  }

  public IChartLabelRenderer getLabelRenderer( )
  {
    return m_labelRenderer;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.style.IChartLabelRenderer#paint(org.eclipse.swt.graphics.GC)
   */

  public void paint( final GC gcw, final Point mousePos )
  {

    final Rectangle toolsize = getLabelRenderer().getSize();
    if( toolsize.width == 0 || toolsize.height == 0 )
      return;

    final Rectangle clippRect = gcw.getClipping();
    /*
     * Positionieren der Tooltip-Box: der ideale Platz liegt 3 Pixel rechts �ber dem Mauszeiger. Wenn rechts nicht
     * gen�gend Platz ist, dann wird er nach links verschoben. Der Startpunkt soll dabei immer im sichtbaren Bereich
     * liegen.
     */
    ALIGNMENT posX = ALIGNMENT.LEFT;
    ALIGNMENT posY = ALIGNMENT.BOTTOM;
    int offsetX = 3/* Pixel */;
    int offsetY = -3/* Pixel */;

    final boolean mirrorX = toolsize.width + offsetX + mousePos.x > clippRect.x + clippRect.width;
    final boolean mirrorY = toolsize.height - offsetY - mousePos.y > clippRect.y;
    if( mirrorX )
    {
      posX = ALIGNMENT.RIGHT;
      offsetX = -3;
    }
    if( mirrorY )
    {
      posY = ALIGNMENT.TOP;
      offsetY = 3;
      if( toolsize.width < offsetX + mousePos.x )
      {
        posX = ALIGNMENT.RIGHT;
        offsetX = -3;
      }
    }

    getLabelRenderer().getTitleTypeBean().setTextAnchorX( posX );
    getLabelRenderer().getTitleTypeBean().setTextAnchorY( posY );
    getLabelRenderer().paint( gcw, new Point( mousePos.x + offsetX, mousePos.y + offsetY ) );

  }

  public void setTooltip( final String tooltip )
  {
    getLabelRenderer().getTitleTypeBean().setLabel( tooltip );
  }

}
