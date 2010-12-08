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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;

/**
 * @author Dirk Kuch
 */
public class DefaultLegendStrategy implements ILegendPaintStrategy
{

  private Point m_size;

  private int m_numRows;

  private Point calculateSize( final Point... points )
  {
    int x = 0;
    int y = 0;

    for( final Point point : points )
    {
      x += point.x;
      y = Math.max( point.y, y );
    }

    return new Point( x, y );
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.img.ILegendStrategy#createImage(de.openali.odysseus.chart.framework.util.img.LegendImageCreator)
   */
  @Override
  public Image createImage( final ChartLegendPainter creator )
  {
    final IChartLayer[] layers = creator.getLayers();
    final Point size = getSize( creator );
    final int rowHeight = m_numRows < 2 ? size.y : size.y / m_numRows;
    if( size.x <= 0 || size.y <= 0 )
      return null;

    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image canvas = new Image( dev, size.x, size.y );
    final GC gc = new GC( canvas );

    final ITextStyle style = creator.getTextStyle();
    style.apply( gc );

    try
    {
      int x = 0;
      int y = 0;

      for( final IChartLayer layer : layers )
      {
        // final ILegendEntry entry = getLegendEntry( layer );
        if( layer.getLegendEntries() == null )
          continue;
        for( final ILegendEntry entry : layer.getLegendEntries() )
        {
          if( entry == null )
            continue;

          final ImageData imageData = createLegendItem( creator, entry, rowHeight );
          if( x + imageData.width > creator.getMaximumWidth() )
          {
            x = 0;
            y += imageData.height;
          }

          final Image image = new Image( dev, imageData );
          gc.drawImage( image, x, y );

          x += imageData.width;

          image.dispose();
        }
      }
      return canvas;
    }
    finally
    {
      gc.dispose();
    }

  }

  private ImageData createLegendItem( final ChartLegendPainter creator, final ILegendEntry entry, final int rowHeight )
  {
    final Point size = getItemSize( creator, entry );

    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image canvas = new Image( dev, size.x, size.y );
    final GC gc = new GC( canvas );

    final Point iconSize = entry.computeSize( creator.getIconSize() );
    final ImageData iconImageData = entry.getSymbol( iconSize );
    final Image iconImage = new Image( dev, iconImageData );
    try
    {
      gc.drawImage( iconImage, 0, (rowHeight - iconSize.y) / 2 );

      final ITextStyle style = creator.getTextStyle();
      style.apply( gc );
      final Point textSize = gc.textExtent( entry.getDescription() );

      final Point anchor = getTextAnchor( creator, iconSize.x, rowHeight, textSize );
      final String description = entry.getDescription();
      gc.drawText( description == null ? "" : description, anchor.x, anchor.y, SWT.TRANSPARENT );

      final ImageData imageData = canvas.getImageData();
      return imageData;
    }
    finally
    {
      iconImage.dispose();
      canvas.dispose();
      gc.dispose();
    }
  }

  private Point getItemSize( final ChartLegendPainter creator, final ILegendEntry entry )
  {
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, 1, 1 );
    final GC gc = new GC( image );

    final ITextStyle style = creator.getTextStyle();
    style.apply( gc );

    try
    {
      final Point iconSize = entry.computeSize( creator.getIconSize() );
      final Point spacer = creator.getSpacer();
      final Point titleSize = gc.textExtent( entry.getDescription() == null ? "" : entry.getDescription(), SWT.DRAW_DELIMITER | SWT.DRAW_TAB );

      // TODO subtract spacer2 from last line element?
      final Point itemSpacer = creator.getItemSpacer();

      final Point size = calculateSize( iconSize, spacer, titleSize, itemSpacer );

      return size;
    }
    finally
    {
      image.dispose();
      gc.dispose();
    }
  }


  /**
   * @see de.openali.odysseus.chart.framework.util.img.ILegendStrategy#getSize(de.openali.odysseus.chart.framework.util.img.LegendImageCreator)
   */
  @Override
  public Point getSize( final ChartLegendPainter creator )
  {
    if( m_size != null )
      return m_size;

    final IChartLayer[] layers = creator.getLayers();

    int heigth = 0;
    int row = 0;

    int maxRowWidth = 0;
    int maxRowHeight = 0;

    m_numRows = 0;

    for( final IChartLayer layer : layers )
    {
      // final ILegendEntry entry = getLegendEntry( layer );
      if( layer.getLegendEntries() == null )
        continue;
      for( final ILegendEntry entry : layer.getLegendEntries() )
      {
        if( entry == null )
          continue;

        final Point size = getItemSize( creator, entry );

        if( row + size.x > creator.getMaximumWidth() )
        {
          maxRowWidth = Math.max( maxRowWidth, size.x );
          maxRowHeight = size.y;
          
          heigth += size.y;
          row = size.x;
          
          m_numRows += 1;
        }
        else
        {
          row += size.x;

          if( size.y > maxRowHeight )
            heigth += size.y - maxRowHeight;
          
          maxRowWidth = Math.max( maxRowWidth, row );
          maxRowHeight = Math.max( maxRowHeight, size.y );
          
          if( m_numRows == 0 )
            m_numRows = 1;
        }
      }
    }
    m_size = new Point( maxRowWidth, heigth );

    return m_size;
  }

  private Point getTextAnchor( final ChartLegendPainter creator, final int iconWidth, final int rowHeight, final Point textSize )
  {
    final Point spacer = creator.getSpacer();

    final int x = iconWidth + spacer.x;
    final int y = (rowHeight - textSize.y) / 2;

    return new Point( x, y );
  }

}
