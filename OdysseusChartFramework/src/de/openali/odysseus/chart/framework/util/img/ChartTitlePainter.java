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

import java.awt.Insets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author Dirk Kuch
 */
public class ChartTitlePainter
{
  private final IChartModel m_model;

  private final int m_width;

  private Point m_size;

  public ChartTitlePainter( final IChartModel model, final Rectangle size )
  {
    m_model = model;
    m_width = size.width;
  }

  public Point getSize( )
  {
    if( m_model.isHideTitle() )
      return new Point( 0, 0 );

    if( m_size != null )
      return m_size;

    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, 1, 1 );
    final GC gc = new GC( image, SWT.DRAW_DELIMITER | SWT.DRAW_TAB );

    int y = 0;

    try
    {
      final TitleTypeBean[] titles = m_model.getTitles();

      for( final TitleTypeBean title : titles )
      {
        final ITextStyle textStyle = title.getTextStyle();
        final FontData fontData = textStyle == null ? StyleUtils.getDefaultTextStyle().toFontData() : textStyle.toFontData();
        final Font font = new Font( dev, fontData );
        try
        {
          gc.setFont( font );

          final Point extent = gc.textExtent( title.getText(), SWT.DRAW_DELIMITER | SWT.DRAW_TAB );

          y += extent.y;
        }
        finally
        {
          font.dispose();
        }
      }
    }
    finally
    {
      image.dispose();
      gc.dispose();
    }

    m_size = new Point( m_width, y );

    return m_size;
  }

  public Image paint( )
  {
    if( m_model.isHideTitle() )
      return null;

    final Point size = getSize();
    if( size.x <= 0 || size.y <= 0 )
      return null;

    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, m_width, size.y );
    final GC gc = new GC( image, SWT.DRAW_DELIMITER | SWT.DRAW_TAB );

    try
    {
      int y = 0;

      for( final TitleTypeBean bean : m_model.getTitles() )
      {
        final ITextStyle textStyle = bean.getTextStyle();
        textStyle.apply( gc );

        final Point extent = gc.textExtent( bean.getText(), SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
        final Point anchor = getAnchor( bean, extent, y );

        gc.drawText( bean.getText(), anchor.x, anchor.y, SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
        y += extent.y;
      }
    }
    finally
    {
      gc.dispose();
    }
    return image;
  }

  /**
   * @param y
   *          pointer to y row position
   */
  private Point getAnchor( final TitleTypeBean title, final Point textExtent, final int y )
  {
    final Insets inset = title.getInsets();

    final ALIGNMENT alignment = title.getAlignmentHorizontal();
    if( ALIGNMENT.CENTER.equals( alignment ) )
    {
      final int x = Double.valueOf( m_width / 2.0 - textExtent.x / 2.0 ).intValue();

      return new Point( x, y );
    }
    else if( ALIGNMENT.LEFT.equals( alignment ) )
    {
      final int x = Double.valueOf( inset.left ).intValue();

      return new Point( x, y );
    }
    else if( ALIGNMENT.RIGHT.equals( alignment ) )
    {
      final int x = Double.valueOf( m_width - textExtent.x - inset.right ).intValue();

      return new Point( x, y );
    }

    return new Point( 0, y );
  }

}
