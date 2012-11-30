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
import java.util.List;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author kimwerner
 */
public class ChartTitlePainter
{
  private final TitleTypeBean[] m_titleTypes;

  private Point m_size = null;

  private final IChartLabelRenderer m_renderer;

  /**
   * @param titleTypes
   *          titleTypeBean.textAnchorY will be ignored, always set to TOP
   */
  public ChartTitlePainter( final IChartLabelRenderer labelRenderer, final TitleTypeBean... titleTypes )
  {
    m_titleTypes = titleTypes;
    m_renderer = labelRenderer;
  }

  /**
   * @param titleTypes
   *          titleTypeBean.textAnchorY will be ignored, always set to TOP
   */
  public ChartTitlePainter( final TitleTypeBean... titleTypes )
  {
    this( new GenericChartLabelRenderer(), titleTypes );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.style.IChartLabelRenderer#paint(org.eclipse.swt.graphics.GC)
   */

// private int calcHeight( final ALIGNMENT line )
// {
// final Rectangle minRect = new Rectangle( 0, 0, m_width, 0 );
// final Rectangle left = paint( null, minRect, ALIGNMENT.LEFT, line );
// final Rectangle right = paint( null, minRect, ALIGNMENT.RIGHT, line );
// final Rectangle center = paint( null, minRect, ALIGNMENT.CENTER, line );
// final int height = Math.max( left.height, right.height );
// if( center.intersects( left ) || center.intersects( right ) )
// return height + center.height;
// return height;
// }

  private TitleTypeBean[] getTitlesAtPosition( final ALIGNMENT horizontal, final ALIGNMENT vertical )
  {
    final List<TitleTypeBean> titles = new ArrayList<>();
    for( final TitleTypeBean titleType : m_titleTypes )
    {
      if( titleType == null )
        continue;
      if( (vertical == null || titleType.getPositionVertical() == vertical) && (horizontal == null || titleType.getPositionHorizontal() == horizontal) )
      {
        titles.add( titleType );
      }
    }
    return titles.toArray( new TitleTypeBean[] {} );
  }

  public Image createImage( )
  {
    return createImage( new Rectangle( 0, 0, m_size.x, m_size.y ) );
  }

  public Image createImage( final Rectangle boundsRect )
  {
    if( boundsRect.width == 0 || boundsRect.height == 0 )
      return null;

    final Device dev = ChartUtilities.getDisplay();
    final Image image = new Image( dev, boundsRect.width, boundsRect.height );
    final GC gcw = new GC( image );
    try
    {
      paint( gcw, boundsRect );
    }
    finally
    {
      gcw.dispose();
    }
    return image;
  }

  public final Point getSize( final int width )
  {
    if( m_size == null )
    {
      final Rectangle minRect = new Rectangle( 0, 0, width, 0 );
      final Rectangle used = paint( null, minRect );
      m_size = new Point( used.width, used.height );
    }
    return m_size;
  }

  public Rectangle probe( final Rectangle boundsRect, final ALIGNMENT horizontal, final ALIGNMENT vertical )
  {
    return paint( null, boundsRect, horizontal, vertical );
  }

  private Rectangle paint( final GC gc, final Rectangle boundsRect, final ALIGNMENT horizontal, final ALIGNMENT vertical )
  {
    //TODO: hier f¸r horizontalen und vertikalen Textoffset feste Pixel nehmen, die einzelnen TitleTypes kˆnnen dann mit relativen offsets arbeiten.
    final int anchorX = boundsRect.x + (int) Math.ceil( boundsRect.width * horizontal.doubleValue() );
    int anchorY = boundsRect.y;// + (int) Math.ceil( boundsRect.height * vertical.doubleValue() );
    Rectangle usedRect = new Rectangle( anchorX, anchorY, 0, 0 );
    for( final TitleTypeBean titleType : getTitlesAtPosition( horizontal, vertical ) )
    {
      final ALIGNMENT oldAnchor = titleType.getTextAnchorY();
      titleType.setTextAnchorY( ALIGNMENT.TOP );
      m_renderer.setTitleTypeBean( titleType );
      final Rectangle textRect = m_renderer.getSize();
      textRect.x += anchorX;
      textRect.y = anchorY;
      m_renderer.paint( gc, new Point( anchorX, anchorY ) );
      titleType.setTextAnchorY( oldAnchor );
      anchorY += textRect.height;
      usedRect = usedRect.union( textRect );
    }
    return usedRect;
  }

  private final int getTopMost( final Rectangle rect, final Rectangle... rectangles )
  {
    for( final Rectangle r : rectangles )
    {
      if( rect.intersects( r ) )
        rect.y = Math.max( rect.y, r.y + r.height );
    }
    return rect.y;
  }

  public Rectangle paint( final GC gc, final Rectangle boundsRect )
  {
    // titleTypeBean.textAnchorY is ignored, always set to TOP

    final Rectangle topLeft = probe( boundsRect, ALIGNMENT.LEFT, ALIGNMENT.TOP );
    final Rectangle topCenter = probe( boundsRect, ALIGNMENT.CENTER, ALIGNMENT.TOP );
    final Rectangle topRight = probe( boundsRect, ALIGNMENT.RIGHT, ALIGNMENT.TOP );
    final Rectangle centerLeft = probe( boundsRect, ALIGNMENT.LEFT, ALIGNMENT.CENTER );
    final Rectangle centerCenter = probe( boundsRect, ALIGNMENT.CENTER, ALIGNMENT.CENTER );
    final Rectangle centerRight = probe( boundsRect, ALIGNMENT.RIGHT, ALIGNMENT.CENTER );
    final Rectangle bottomLeft = probe( boundsRect, ALIGNMENT.LEFT, ALIGNMENT.BOTTOM );
    final Rectangle bottomCenter = probe( boundsRect, ALIGNMENT.CENTER, ALIGNMENT.BOTTOM );
    final Rectangle bottomRight = probe( boundsRect, ALIGNMENT.RIGHT, ALIGNMENT.BOTTOM );

    // top-Line
    Rectangle usedRect = boundsRect.union( topLeft );
    topRight.y = getTopMost( topRight, topLeft );
    usedRect = usedRect.union( topRight );
    topCenter.y = getTopMost( topCenter, topLeft, topRight );
    usedRect = usedRect.union( topCenter );
    // center-Line
    centerLeft.y = getTopMost( centerLeft, topLeft, topRight, topCenter );
    usedRect = usedRect.union( centerLeft );
    centerRight.y = getTopMost( centerRight, topLeft, topRight, topCenter, centerLeft );
    usedRect = usedRect.union( centerRight );
    centerCenter.y = getTopMost( centerCenter, topLeft, topRight, topCenter, centerLeft, centerRight );
    usedRect = usedRect.union( centerCenter );

    // bottom-Line
    bottomRight.y = getTopMost( bottomRight, topLeft, topRight, topCenter, centerLeft, centerRight, centerCenter );
    usedRect = usedRect.union( bottomRight );
    bottomLeft.y = getTopMost( bottomLeft, topLeft, topRight, topCenter, centerLeft, centerRight, bottomRight, centerCenter );
    usedRect = usedRect.union( bottomLeft );
    bottomLeft.y = usedRect.y + usedRect.height - bottomLeft.height;
    bottomRight.y = usedRect.y + usedRect.height - bottomRight.height;
    if( bottomRight.x - bottomLeft.x + bottomLeft.width > bottomCenter.width )
      bottomCenter.y = usedRect.y + usedRect.height - bottomCenter.height;
    else
    {
      bottomCenter.y = Math.min( bottomLeft.y, bottomRight.y ) - bottomCenter.height;
      bottomLeft.y += bottomCenter.height;
      bottomRight.y += bottomCenter.height;
    }
    usedRect = usedRect.union( bottomLeft );

    // adjust Center-Line
    final int deltaLeft = bottomLeft.y - topLeft.y - topLeft.height - centerLeft.height;
    if( deltaLeft > 1 )
      centerLeft.y += deltaLeft / 2;
    final int deltaCenter = bottomCenter.y - topCenter.y - topCenter.height - centerCenter.height;
    if( deltaCenter > 1 )
      centerCenter.y += deltaCenter / 2;
    final int deltaRight = bottomRight.y - topRight.y - topRight.height - centerRight.height;
    if( deltaRight > 1 )
      centerRight.y += deltaRight / 2;

    // paint
    paint( gc, topLeft, ALIGNMENT.LEFT, ALIGNMENT.TOP );
    paint( gc, topCenter, ALIGNMENT.CENTER, ALIGNMENT.TOP );
    paint( gc, topRight, ALIGNMENT.RIGHT, ALIGNMENT.TOP );
    paint( gc, centerLeft, ALIGNMENT.LEFT, ALIGNMENT.CENTER );
    paint( gc, centerCenter, ALIGNMENT.CENTER, ALIGNMENT.CENTER );
    paint( gc, centerRight, ALIGNMENT.RIGHT, ALIGNMENT.CENTER );
    paint( gc, bottomLeft, ALIGNMENT.LEFT, ALIGNMENT.BOTTOM );
    paint( gc, bottomCenter, ALIGNMENT.CENTER, ALIGNMENT.BOTTOM );
    paint( gc, bottomRight, ALIGNMENT.RIGHT, ALIGNMENT.BOTTOM );

    return usedRect;
  }
}
