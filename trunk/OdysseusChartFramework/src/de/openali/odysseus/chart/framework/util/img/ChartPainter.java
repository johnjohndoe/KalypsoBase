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
package de.openali.odysseus.chart.framework.util.img;

import java.awt.Insets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.util.img.legend.ChartLegendCanvas;
import de.openali.odysseus.chart.framework.util.img.legend.config.DefaultChartLegendConfig;

/**
 * @author kimwerner
 */
public class ChartPainter
{
  Image m_image = null;

  final IChartModel m_model;

  final Rectangle m_size;

  private Insets m_plotInsets = null;

  final ChartTitlePainter m_titlePainter;

  final ChartLegendCanvas m_legendPainter;

  private ChartPlotPainter m_plotPainter = null;

  private final Insets m_chartInsets;

  private final Rectangle m_clientRect;

  public ChartPainter( final IChartModel model, final Rectangle size )
  {
    this( model, size, new Insets( 3, 3, 3, 3 ) );
  }

  public ChartPainter( final IChartModel model, final Rectangle size, final Insets chartInsets )
  {
    m_model = model;
    // model.getBehaviour().isHideUnusedAxes();
    m_size = size;
    m_chartInsets = chartInsets;
    m_clientRect = RectangleUtils.inflateRect( m_size, m_chartInsets );
    m_legendPainter = new ChartLegendCanvas( m_model, new DefaultChartLegendConfig( m_clientRect ) );
    m_titlePainter = new ChartTitlePainter( model.getSettings().getTitles() );
  }

  public Image init( )
  {
    final Device dev = ChartUtilities.getDisplay();
    m_image = new Image( dev, m_size.width, m_size.height );

    return m_image;
  }

  public final Image createImage( )
  {
    return createImage( new Point( 0, 0 ) );
  }

  public final Image createImage( final Point panOffset )
  {
    if( m_size.width == 0 || m_size.height == 0 )
      return null;

    final Insets plotInsets = getPlotInsets();
    setAxesHeight( plotInsets, m_size );

    if( m_image == null )
      init();

    final GC gc = new GC( m_image );
    final Image legendImage = m_legendPainter.createImage();
    try
    {
      gc.setAntialias( SWT.ON );
      gc.setTextAntialias( SWT.ON );
      gc.setAdvanced( true );
      m_titlePainter.paint( gc, new Rectangle( m_clientRect.x, m_clientRect.y, m_clientRect.width, m_titlePainter.getSize( m_clientRect.width ).y ) );

      // paint left Axes
      paintAxes( m_model.getMapperRegistry().getAxesAt( POSITION.LEFT ), gc, plotInsets.left, plotInsets.top, plotInsets.top, m_size.height, 90, false );
      // paint right Axes
      paintAxes( m_model.getMapperRegistry().getAxesAt( POSITION.RIGHT ), gc, m_size.width - plotInsets.right, plotInsets.top, plotInsets.top, m_size.height, 90, true );
      // paint top Axes
      paintAxes( m_model.getMapperRegistry().getAxesAt( POSITION.TOP ), gc, plotInsets.left, plotInsets.top, plotInsets.left, m_size.width, 0, true );
      // paint bottom Axes
      paintAxes( m_model.getMapperRegistry().getAxesAt( POSITION.BOTTOM ), gc, plotInsets.left, m_size.height - plotInsets.bottom, plotInsets.left, m_size.width, 0, false );
      // paint plot

      if( legendImage != null )
        gc.drawImage( legendImage, m_chartInsets.left, m_size.height - m_legendPainter.getSize().height - m_chartInsets.bottom );
      final Rectangle plotRect = RectangleUtils.createInnerRectangle( m_size.width, m_size.height, plotInsets );
      // Layer könnten sonst in die Achsen zeichnen
      gc.setClipping( plotRect );
      getPlotPainter().paint( gc, new Insets( plotInsets.top - panOffset.y, plotInsets.left - panOffset.x, plotInsets.bottom + panOffset.y, plotInsets.right + panOffset.x ) );
    }
    finally
    {
      if( legendImage != null )
        legendImage.dispose();
      gc.dispose();
    }

    return m_image;
  }

  private int getAxesWidth( final IAxis[] axes )
  {
    int width = 0;
    for( final IAxis axis : axes )
    {
      if( axis.isVisible() )
      {
        final IAxisRenderer renderer = axis.getRenderer();
        width += renderer.getAxisWidth( axis );
      }
    }
    return width;
  }

  public final ImageData getImageData( )
  {
    final Image image = createImage();
    try
    {
      final ImageData imageData = image.getImageData();
      return imageData;
    }
    finally
    {
      image.dispose();
    }
  }

  public final Insets getPlotInsets( )
  {
    if( m_plotInsets == null )
    {
      final int axisLeftWidth = getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.LEFT ) );
      final int axisRightWidth = getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.RIGHT ) );
      final int axisTopWidth = getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.TOP ) );
      final int axisBottomWidth = getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.BOTTOM ) );
      final int top = m_titlePainter.getSize( m_clientRect.width ).y + axisTopWidth;
      final int bottom = m_legendPainter.getSize().height + axisBottomWidth;
      m_plotInsets = new Insets( top + m_chartInsets.top, axisLeftWidth + m_chartInsets.left, bottom + m_chartInsets.bottom, axisRightWidth + m_chartInsets.right );
    }
    return m_plotInsets;
  }

  private ChartPlotPainter getPlotPainter( )
  {
    if( m_plotPainter == null )
    {
      m_plotPainter = new ChartPlotPainter( m_model, new Point( m_size.width, m_size.height ) );
    }
    return m_plotPainter;
  }

  private void paintAxes( final IAxis[] axes, final GC gc, final int anchorX, final int anchorY, final int startOffset, final int screenWidth, final int rotation, final boolean invertVertical )
  {
    final Transform oldTransform = new Transform( gc.getDevice() );
    final Transform newTransform = new Transform( gc.getDevice() );
    gc.getTransform( oldTransform );
    gc.getTransform( newTransform );
    newTransform.translate( anchorX, anchorY );
    newTransform.rotate( rotation );
    gc.setTransform( newTransform );
    int offset = 0;

    try
    {
      for( final IAxis axis : axes )
      {
        if( !axis.isVisible() )
          continue;
        if( invertVertical )
        {
          newTransform.scale( 1, -1 );
          gc.setTransform( newTransform );
        }
        int height = 0;
        try
        {
          final IAxisRenderer renderer = axis.getRenderer();
          height = renderer.getAxisWidth( axis );
          renderer.paint( gc, axis, new Rectangle( startOffset, offset, screenWidth, height ) );
        }
        finally
        {
          if( invertVertical )
          {
            newTransform.scale( 1, -1 );
            gc.setTransform( newTransform );
            offset += height;
          }
          else
            offset += height;
        }
      }
    }
    finally
    {
      gc.setTransform( oldTransform );
      newTransform.dispose();
      oldTransform.dispose();
    }
  }

  private void setAxesHeight( final Insets plotInsets, final Rectangle size )
  {
    for( final IAxis axis : m_model.getMapperRegistry().getAxes() )
    {
      if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        axis.setScreenHeight( size.width - plotInsets.left - plotInsets.right );
      else
        axis.setScreenHeight( size.height - plotInsets.top - plotInsets.bottom );
    }
  }

}
