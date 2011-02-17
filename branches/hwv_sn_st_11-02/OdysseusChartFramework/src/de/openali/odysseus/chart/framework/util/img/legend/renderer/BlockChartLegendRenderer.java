/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package de.openali.odysseus.chart.framework.util.img.legend.renderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.img.legend.IChartLegendCanvas;
import de.openali.odysseus.chart.framework.util.img.legend.config.IChartLegendConfig;
import de.openali.odysseus.chart.framework.util.img.legend.utils.LegendChartLayersVisitor;

/**
 * @author Dirk Kuch
 */
public class BlockChartLegendRenderer implements IChartLegendRenderer
{
  public static final String ID = "de.openali.odysseus.chart.legend.render.block"; //$NON-NLS-1$

  /**
   * @see de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer#getIdentifier()
   */
  @Override
  public String getIdentifier( )
  {
    return ID; //$NON-NLS-1$
  }

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
  public Image createImage( final IChartLegendCanvas canvas, final IChartLegendConfig config )
  {
    final IChartLayer[] layers = getLayers( canvas.getModel() );
    final Point canvasSize = calculateSize( layers, config );
    final Point itemSize = calculateItemSize( layers, config );

    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, canvasSize.x, canvasSize.y );
    final GC gc = new GC( image );

    final ITextStyle style = config.getTextStyle();
    style.apply( gc );

    try
    {
      int x = 0;
      int y = 0;

      for( final IChartLayer layer : layers )
      {
        if( layer.getLegendEntries() == null )
          continue;
        for( final ILegendEntry entry : layer.getLegendEntries() )
        {
          if( entry == null )
            continue;

          final ImageData imageData = createLegendItem( entry, config, itemSize );
          if( x + imageData.width > config.getMaximumWidth() )
          {
            x = 0;
            y += imageData.height;
          }

          final Image can = new Image( dev, imageData );
          gc.drawImage( can, x, y );

          x += imageData.width;

          can.dispose();
        }
      }
      return image;
    }
    finally
    {
      gc.dispose();
    }

  }

  protected IChartLayer[] getLayers( final IChartModel model )
  {
    final ILayerManager layerManager = model.getLayerManager();
    final LegendChartLayersVisitor visitor = new LegendChartLayersVisitor();
    layerManager.accept( visitor );

    return visitor.getLayers();
  }

  private ImageData createLegendItem( final ILegendEntry entry, final IChartLegendConfig config, final Point size )
  {
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image img = new Image( dev, size.x, size.y );
    final GC gc = new GC( img );

    final Point iconSize = entry.computeSize( config.getIconSize() );
    final ImageData iconImageData = entry.getSymbol( iconSize );
    final Image iconImage = new Image( dev, iconImageData );
    try
    {
      gc.drawImage( iconImage, 0, (size.y - iconSize.y) / 2 );

      final ITextStyle style = config.getTextStyle();
      style.apply( gc );

      final String description = entry.getDescription();

      final Point textSize;
      if( description == null )
        textSize = new Point( 1, 1 );
      else
        textSize = gc.textExtent( description );

      final Point anchor = getTextAnchor( config, iconSize.x, size.y, textSize );

      gc.drawText( description == null ? "" : description, anchor.x, anchor.y, SWT.TRANSPARENT );

      return img.getImageData();
    }
    finally
    {
      iconImage.dispose();
      img.dispose();
      gc.dispose();
    }
  }

  private Point getItemSize( final IChartLegendConfig config, final ILegendEntry entry )
  {
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, 1, 1 );
    final GC gc = new GC( image );

    final ITextStyle style = config.getTextStyle();
    style.apply( gc );

    try
    {
      final Point iconSize = entry.computeSize( config.getIconSize() );
      final Point spacer = config.getSpacer();
      final Point titleSize = gc.textExtent( entry.getDescription() == null ? "" : entry.getDescription(), SWT.DRAW_DELIMITER | SWT.DRAW_TAB );

      // TODO subtract spacer2 from last line element?
      final Point itemSpacer = config.getItemSpacer();

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
  public Point calculateSize( final IChartLegendCanvas canvas, final IChartLegendConfig config )
  {
    final IChartLayer[] layers = getLayers( canvas.getModel() );

    return calculateSize( layers, config );
  }

  private Point calculateSize( final IChartLayer[] layers, final IChartLegendConfig config )
  {
    final Point maxItemSize = calculateItemSize( layers, config );
    if( maxItemSize == null || maxItemSize.x == 0 || maxItemSize.y == 0 )
      return new Point( 1, 1 );

    final int legendEntries = calculateLegendEntries( layers );

    final int itemsPerRow = config.getMaximumWidth() / maxItemSize.x;

    double rows = Integer.valueOf( legendEntries ).doubleValue() / Integer.valueOf( itemsPerRow ).doubleValue();
    if( Double.valueOf( rows ).intValue() - rows != 0 )
      rows += 1;

    return new Point( config.getMaximumWidth(), Double.valueOf( rows ).intValue() * maxItemSize.y );
  }

  private int calculateLegendEntries( final IChartLayer[] layers )
  {
    int legendEntries = 0;
    for( final IChartLayer layer : layers )
    {
      legendEntries += layer.getLegendEntries().length;
    }

    return legendEntries;
  }

  private Point calculateItemSize( final IChartLayer[] layers, final IChartLegendConfig config )
  {
    Point maxItemSize = null;
    for( final IChartLayer layer : layers )
    {
      final Point layerSize = getMaxEntrySize( layer, config );
      maxItemSize = getMax( maxItemSize, layerSize );
    }

    return maxItemSize;
  }

  private Point getMax( final Point p1, final Point p2 )
  {
    if( p1 == null && p2 == null )
      return null;
    else if( p1 != null && p2 == null )
      return p1;
    else if( p1 == null && p2 != null )
      return p2;

    return new Point( Math.max( p1.x, p2.x ), Math.max( p1.y, p2.y ) );
  }

  private Point getMaxEntrySize( final IChartLayer layer, final IChartLegendConfig config )
  {
    Point maxItemSize = null;

    final ILegendEntry[] entries = layer.getLegendEntries();
    for( final ILegendEntry entry : entries )
    {
      if( entry == null )
        continue;

      final Point size = getItemSize( config, entry );
      maxItemSize = getMax( maxItemSize, size );
    }

    return maxItemSize;
  }

  private Point getTextAnchor( final IChartLegendConfig config, final int iconWidth, final int rowHeight, final Point textSize )
  {
    final Point spacer = config.getSpacer();

    final int x = iconWidth + spacer.x;
    final int y = (rowHeight - textSize.y) / 2;

    return new Point( x, y );
  }

}
