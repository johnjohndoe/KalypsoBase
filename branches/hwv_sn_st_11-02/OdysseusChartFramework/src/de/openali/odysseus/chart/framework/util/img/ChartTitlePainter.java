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

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;

/**
 * @author kimwerner
 */
public class ChartTitlePainter
{
  private final TitleTypeBean[] m_titleTypes;

  private Point m_size = null;

  final IChartLabelRenderer m_renderer = new GenericChartLabelRenderer();

  public ChartTitlePainter( final TitleTypeBean... titleTypes )
  {
    m_titleTypes = titleTypes;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.style.IChartLabelRenderer#paint(org.eclipse.swt.graphics.GC)
   */

  private Point calcSize( )
  {

    int x = 0;
    int y = 0;

    for( final TitleTypeBean titleType : m_titleTypes )
    {
      m_renderer.setTitleTypeBean( titleType );
      final Rectangle size = m_renderer.getSize();
      y += size.height;
      x = Math.max( x, size.width );
    }
    return new Point( x, y );
  }

  public Image createImage( )
  {
    return createImage( new Rectangle( 0, 0, m_size.x, m_size.y ) );
  }

  public Image createImage( final Rectangle boundsRect )
  {
    if( boundsRect.width == 0 || boundsRect.height == 0 )
      return null;

    final Device dev = PlatformUI.getWorkbench().getDisplay();
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

  public final Point getSize( )
  {
    if( m_size == null )
      m_size = calcSize();
    return m_size;
  }

  public void paint( final GC gc, final Rectangle boundsRect )
  {
    for( final TitleTypeBean titleType : m_titleTypes )
    {
      m_renderer.setTitleTypeBean( titleType );
      m_renderer.paint( gc, boundsRect );
    }

  }

}
