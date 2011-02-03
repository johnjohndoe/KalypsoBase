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
package de.openali.odysseus.chart.framework.util.img;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;

/**
 * @author kimwerner
 */
public class ChartTitlePainter2
{
  private final Map<String, List<IChartLabelRenderer>> m_titleTypes = new HashMap<String, List<IChartLabelRenderer>>();

  private Point m_size = null;

  private Point m_leftTop;

  private Point m_leftBottom;

  private Point m_leftCenter;

  private Point m_centerTop;

  private Point m_centerBottom;

  private Point m_rightTop;

  private Point m_rightBottom;

  private Point m_rightCenter;

  private Point m_centerCenter;

  private int m_leftWidth;

  private int m_rightWidth;

  private int m_topHeight;

  private int m_bottomHeight;

  private int m_centerWidth;

  private int m_centerHeight;

  public final void addTitle( final TitleTypeBean... titles )
  {
    for( final TitleTypeBean title : titles )
    {
      final String key = title.getPositionHorizontal().name() + title.getPositionVertical().name();
      final List<IChartLabelRenderer> titleList;
      if( m_titleTypes.get( key ) == null )
      {
        titleList = new ArrayList<IChartLabelRenderer>();
        m_titleTypes.put( key, titleList );
      }
      else
        titleList = m_titleTypes.get( key );
      titleList.add( new GenericChartLabelRenderer( title ) );
    }
    m_size = null;

  }

  public final Point getSize( )
  {
    if( m_size == null )
      m_size = calcSize();
    return m_size;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.style.IChartLabelRenderer#paint(org.eclipse.swt.graphics.GC)
   */

  private Point calcSize( final List<IChartLabelRenderer> renderers )
  {
    if( renderers == null )
      return new Point( 0, 0 );
    int x = 0;
    int y = 0;
    for( final IChartLabelRenderer renderer : renderers )
    {
      final Point size = renderer.getSize();
      y += size.y;
      x = Math.max( x, size.x );
    }
    return new Point( x, y );
  }

  public final void clear( )
  {
    m_titleTypes.clear();
  }

  private Point calcSize( )
  {
    final List<IChartLabelRenderer> leftTopRenderer = m_titleTypes.get( ALIGNMENT.LEFT.name() + ALIGNMENT.TOP.name() );
    final List<IChartLabelRenderer> leftCenterRenderer = m_titleTypes.get( ALIGNMENT.LEFT.name() + ALIGNMENT.CENTER.name() );
    final List<IChartLabelRenderer> leftBottomRenderer = m_titleTypes.get( ALIGNMENT.LEFT.name() + ALIGNMENT.BOTTOM.name() );
    final List<IChartLabelRenderer> centerTopRenderer = m_titleTypes.get( ALIGNMENT.CENTER.name() + ALIGNMENT.TOP.name() );
    final List<IChartLabelRenderer> centerBottomRenderer = m_titleTypes.get( ALIGNMENT.CENTER.name() + ALIGNMENT.BOTTOM.name() );
    final List<IChartLabelRenderer> centerCenterRenderer = m_titleTypes.get( ALIGNMENT.CENTER.name() + ALIGNMENT.CENTER.name() );
    final List<IChartLabelRenderer> rightTopRenderer = m_titleTypes.get( ALIGNMENT.RIGHT.name() + ALIGNMENT.TOP.name() );
    final List<IChartLabelRenderer> rightBottomRenderer = m_titleTypes.get( ALIGNMENT.RIGHT.name() + ALIGNMENT.BOTTOM.name() );
    final List<IChartLabelRenderer> rightCenterRenderer = m_titleTypes.get( ALIGNMENT.RIGHT.name() + ALIGNMENT.CENTER.name() );

    m_leftTop = calcSize( leftTopRenderer );
    m_leftBottom = calcSize( leftBottomRenderer );
    m_leftCenter = calcSize( leftCenterRenderer );
    m_centerTop = calcSize( centerTopRenderer );
    m_centerBottom = calcSize( centerBottomRenderer );
    m_rightTop = calcSize( rightTopRenderer );
    m_rightBottom = calcSize( rightBottomRenderer );
    m_rightCenter = calcSize( rightCenterRenderer );
    m_centerCenter = calcSize( centerCenterRenderer );

    m_leftWidth = Math.max( Math.max( m_leftTop.x, m_leftCenter.x ), m_leftBottom.x );
    m_rightWidth = Math.max( Math.max( m_rightTop.x, m_rightCenter.x ), m_rightBottom.x );
    m_topHeight = Math.max( Math.max( m_leftTop.y, m_centerTop.y ), m_rightTop.y );
    m_bottomHeight = Math.max( Math.max( m_leftBottom.y, m_centerBottom.y ), m_rightBottom.y );
    m_centerWidth = Math.max( Math.max( m_centerTop.x, m_centerBottom.x ), m_centerCenter.x );
    m_centerHeight = Math.max( Math.max( m_leftCenter.y, m_rightCenter.y ), m_centerCenter.y );

    return new Point( m_leftWidth + m_centerWidth + m_rightWidth, m_topHeight + m_centerHeight + m_bottomHeight );

  }

  public Image createImage( )
  {
    return createImage( new Rectangle( 0, 0, m_size.x, m_size.y ) );
  }

  public Image createImage( final Rectangle boundsRect )
  {
    if( boundsRect.width == 0 || boundsRect.height == 0 )
      return null;
    if( m_size == null )
      m_size = calcSize();
    final int centerWidth = boundsRect.width - m_size.x + m_centerWidth;
    final int centerHeight = boundsRect.height - m_size.y + m_centerHeight;

    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, boundsRect.width, boundsRect.height );
    final GC gcw = new GC( image );
    try
    {
      paintTitle( gcw, m_titleTypes.get( ALIGNMENT.LEFT.name() + ALIGNMENT.TOP.name() ), m_leftTop, 0, 0, m_leftWidth, m_topHeight );
      paintTitle( gcw, m_titleTypes.get( ALIGNMENT.CENTER.name() + ALIGNMENT.TOP.name() ), m_centerTop, m_leftWidth, 0, centerWidth, m_topHeight );
      paintTitle( gcw, m_titleTypes.get( ALIGNMENT.RIGHT.name() + ALIGNMENT.TOP.name() ), m_rightTop, m_leftWidth + centerWidth, 0, m_rightWidth, m_topHeight );
      paintTitle( gcw, m_titleTypes.get( ALIGNMENT.LEFT.name() + ALIGNMENT.CENTER.name() ), m_leftCenter, 0, m_topHeight, m_leftWidth, centerHeight );
      paintTitle( gcw, m_titleTypes.get( ALIGNMENT.CENTER.name() + ALIGNMENT.CENTER.name() ), m_centerCenter, m_leftWidth, m_topHeight, centerWidth, centerHeight );
      paintTitle( gcw, m_titleTypes.get( ALIGNMENT.RIGHT.name() + ALIGNMENT.CENTER.name() ), m_rightCenter, m_leftWidth + centerWidth, m_topHeight, m_rightWidth, centerHeight );
      paintTitle( gcw, m_titleTypes.get( ALIGNMENT.LEFT.name() + ALIGNMENT.BOTTOM.name() ), m_leftBottom, 0, m_topHeight + centerHeight, m_leftWidth, m_bottomHeight );
      paintTitle( gcw, m_titleTypes.get( ALIGNMENT.CENTER.name() + ALIGNMENT.BOTTOM.name() ), m_centerBottom, m_leftWidth, m_topHeight + centerHeight, centerWidth, m_bottomHeight );
      paintTitle( gcw, m_titleTypes.get( ALIGNMENT.RIGHT.name() + ALIGNMENT.BOTTOM.name() ), m_rightBottom, m_leftWidth + centerWidth, m_topHeight + centerHeight, m_rightWidth, m_bottomHeight );
    }
    finally
    {
      gcw.dispose();
    }
    return image;
  }

  private void paintTitle( final GC gcw, final List<IChartLabelRenderer> renderers, final Point size, final int left, final int top, final int width, final int height )
  {
    if( renderers == null )
      return;

    int centerLinePos = (height - size.y) / 2;
    for( final IChartLabelRenderer renderer : renderers )
    {
      renderer.paint( gcw, new Rectangle( left, top + centerLinePos, width, size.y ) );
      centerLinePos += renderer.getSize().y;
    }
  }
}
