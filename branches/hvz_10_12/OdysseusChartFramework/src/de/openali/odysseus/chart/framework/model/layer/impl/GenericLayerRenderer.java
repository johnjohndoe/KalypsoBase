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
package de.openali.odysseus.chart.framework.model.layer.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author kimwerner
 */
public class GenericLayerRenderer
{
  private EditInfo m_editInfo;

  private final Map<IChartLayer, Point> m_layerPanOffsets = new HashMap<IChartLayer, Point>();

  private Rectangle m_dragArea = null;

  /**
   * the plot will be rendered to its AxesScreenHeight, so set the Axis.screenHeight first
   */

 

  /**
   * @param bufferLayers
   *          if set to true, each layer is buffered on an individual image; set this to true if you plan to offer
   *          panning of single layers in the chart front end
   */

  public EditInfo getTooltipInfo( )
  {
    return m_editInfo;
  }

  public void paintDragArea( final GC gcw )
  {
    // Wenn ein DragRectangle da ist, dann muss nur das gezeichnet werden
    if( m_dragArea != null )
    {
      gcw.setLineWidth( 1 );
      gcw.setForeground( gcw.getDevice().getSystemColor( SWT.COLOR_BLACK ) );

      gcw.setBackground( gcw.getDevice().getSystemColor( SWT.COLOR_BLUE ) );
      final Rectangle r = RectangleUtils.createNormalizedRectangle( m_dragArea );// new Rectangle( x, y, w, h ) );

      // TODO: SWT-Bug mit drawFocus (wird nicht immer gezeichnet),
      // irgendwann mal wieder berprfen
      gcw.setAlpha( 50 );
      gcw.fillRectangle( r.x, r.y, r.width, r.height );
      gcw.setAlpha( 255 );
      gcw.setLineStyle( SWT.LINE_DASH );
      gcw.drawRectangle( r.x, r.y, r.width, r.height );
    }
  }

  
  public void paintEditInfo( final GC gcw )
  {
    ChartUtilities.resetGC( gcw );
    if( m_editInfo == null )
      return;

    // draw hover shape
    if( m_editInfo.m_hoverFigure != null )
      m_editInfo.m_hoverFigure.paint( gcw );
    // draw edit shape
    if( m_editInfo.m_editFigure != null )
      m_editInfo.m_editFigure.paint( gcw );

    // draw tooltip
    ChartUtilities.resetGC( gcw );

    final Rectangle screen = gcw.getClipping();

    String tooltiptext = m_editInfo.m_text;
    final Point mousePos = m_editInfo.m_pos;
    if( (tooltiptext != null) && (mousePos != null) )
    {
      tooltiptext = tooltiptext.replace( '\r', ' ' );

      final int TOOLINSET = 3;

      final Font oldFont = gcw.getFont();
      final Font bannerFont = JFaceResources.getTextFont();
      gcw.setFont( bannerFont );
      gcw.setBackground( PlatformUI.getWorkbench().getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
      gcw.setForeground( PlatformUI.getWorkbench().getDisplay().getSystemColor( SWT.COLOR_INFO_FOREGROUND ) );
      final Point toolsize = gcw.textExtent( tooltiptext );

      /*
       * Positionieren der Tooltip-Box: der ideale Platz liegt rechts unter dem Mauszeiger. Wenn rechts nicht genÔøΩgend
       * Platz ist, dann wird er nach links verschoben. Der Startpunkt soll dabei immer im sichtbaren Bereich liegen.
       */
      int toolx = mousePos.x + 3 + TOOLINSET;
      if( toolx + toolsize.x > screen.width )
      {
        toolx = screen.width - 5 - toolsize.x;
        if( toolx < 5 )
          toolx = 5;
      }

      int tooly = mousePos.y + 3 + TOOLINSET + 20;
      if( (tooly + toolsize.y > screen.height) && ((mousePos.y - 3 - TOOLINSET - toolsize.y - 20) > 0) )
        tooly = mousePos.y - 3 - TOOLINSET - toolsize.y - 20;

      gcw.setLineWidth( 1 );
      final Rectangle toolrect = new Rectangle( toolx - TOOLINSET, tooly - TOOLINSET, toolsize.x + TOOLINSET * 2, toolsize.y + TOOLINSET * 2 );
      gcw.fillRectangle( toolrect );
      gcw.drawRectangle( toolrect );

      gcw.drawText( tooltiptext, toolx, tooly, true );

      gcw.setFont( oldFont );
    }
  }

  public void setDragArea( final Rectangle dragArea )
  {
    m_dragArea = dragArea;
  }

  /**
   * sets an offset to which the paint buffer is moved in case of a pan action;
   * 
   * @param layers
   *          if null, all layers are moved; else, only mentioned layers are moved
   * @param offset
   *          positive value moves buffer to right / down, negative value to left / up
   */
  public void setPanOffset( final IChartLayer[] layers, final Point offset )
  {
    for( final IChartLayer iChartLayer : layers )
      m_layerPanOffsets.put( iChartLayer, offset );
  }

  public void setTooltipInfo( final EditInfo hoverInfo )
  {
    m_editInfo = hoverInfo;
  }

}