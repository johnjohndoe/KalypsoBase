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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.util.img.legend.ChartLegendCanvas;
import de.openali.odysseus.chart.framework.util.img.legend.config.DefaultChartLegendConfig;

/**
 * @author kimwerner
 */
public class ChartPainter
{

  final IChartModel m_model;

  final Rectangle m_size;

  private Insets m_plotInsets = null;

  final ChartTitlePainter2 m_titlePainter;

  final ChartLegendCanvas m_legendPainter;

  private ChartPlotPainter m_plotPainter = null;

  public ChartPainter( final IChartModel model, final Rectangle size )
  {
    m_model = model;
    m_size = size;

    m_legendPainter = new ChartLegendCanvas( m_model, new DefaultChartLegendConfig( m_size.width ) );
    m_titlePainter = new ChartTitlePainter2();
    m_titlePainter.addTitle( model.getSettings().getTitles() );
  }

  public final Image createImage( )
  {
    return createImage( new Point( 0, 0 ) );
  }

  public final Image createImage( final Point panOffset )
  {
    if( m_size.width == 0 || m_size.height == 0 )
      return null;

    setAxesHeight( getPlotInsets(), m_size );

    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, m_size.width, m_size.height );
    final GC gc = new GC( image );
    gc.setAntialias( SWT.OFF );
    gc.setAdvanced( true );

    final Transform transform = new Transform( dev );

    m_titlePainter.paint( gc, new Rectangle( 0, 0, m_size.width, m_titlePainter.getSize().y ) );
    final Image legendImage = m_legendPainter.createImage();
    final Insets plotInsets = getPlotInsets();

    // paint plot
    final Rectangle plotRect = new Rectangle( plotInsets.left, plotInsets.top, m_size.width - plotInsets.left - plotInsets.right, m_size.height - plotInsets.top - plotInsets.bottom );
    gc.setClipping( plotRect );
    transform.translate( plotInsets.left - panOffset.x, plotInsets.top - panOffset.y );
    gc.setTransform( transform );
    getPlotPainter().paint( gc );

    // paint left Axes
    resetTransform( gc, transform );
    gc.setClipping( new Rectangle( 0, m_titlePainter.getSize().y, plotInsets.left, m_size.height - m_titlePainter.getSize().y - m_legendPainter.getSize().y ) );

    final IMapperRegistry mapperRegistry = m_model.getMapperRegistry();

    paintAxes( mapperRegistry.getAxesAt( POSITION.LEFT ), gc, transform, new Point( 0, plotInsets.top ) );
    // paint right Axes
    resetTransform( gc, transform );
    gc.setClipping( new Rectangle( m_size.width - plotInsets.right, m_titlePainter.getSize().y, plotInsets.right, m_size.height - m_titlePainter.getSize().y - m_legendPainter.getSize().y ) );
    paintAxes( mapperRegistry.getAxesAt( POSITION.RIGHT ), gc, transform, new Point( m_size.width - plotInsets.right, plotInsets.top ) );
    // paint top Axes
    resetTransform( gc, transform );
    gc.setClipping( new Rectangle( 0, m_titlePainter.getSize().y, m_size.width, plotInsets.top - m_titlePainter.getSize().y ) );
    paintAxes( mapperRegistry.getAxesAt( POSITION.TOP ), gc, transform, new Point( plotInsets.left, 0 ) );
    // paint bottom Axes
    resetTransform( gc, transform );
    gc.setClipping( new Rectangle( 0, m_size.height - plotInsets.bottom, m_size.width, plotInsets.bottom - m_legendPainter.getSize().y ) );
    paintAxes( mapperRegistry.getAxesAt( POSITION.BOTTOM ), gc, transform, new Point( plotInsets.left, m_size.height - plotInsets.bottom ) );

    resetTransform( gc, transform );
    gc.setClipping( m_size );
    try
    {
      if( legendImage != null )
        gc.drawImage( legendImage, 0, m_size.height - m_legendPainter.getSize().y );
    }
    finally
    {
      if( legendImage != null )
        legendImage.dispose();

      transform.dispose();
      gc.dispose();
    }
    return image;
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
      final int top = m_titlePainter.getSize().y + axisTopWidth;
      final int bottom = m_legendPainter.getSize().y + axisBottomWidth;
      m_plotInsets = new Insets( top, axisLeftWidth, bottom, axisRightWidth );
    }
    return m_plotInsets;
  }

  private ChartPlotPainter getPlotPainter( )
  {
    if( m_plotPainter == null )
    {
      final Insets plotInsets = getPlotInsets();
      m_plotPainter = new ChartPlotPainter( m_model, new Point( m_size.width - plotInsets.left - plotInsets.right, m_size.height - plotInsets.bottom - plotInsets.top ) );
    }
    return m_plotPainter;
  }

  private void paintAxes( final IAxis[] axes, final GC gc, final Transform transform, final Point offset )
  {
    transform.translate( offset.x, offset.y );
    gc.setTransform( transform );
    for( final IAxis axis : axes )
    {
      if( !axis.isVisible() )
        continue;
      final ChartAxisPainter axisPainter = new ChartAxisPainter( axis );
      axisPainter.paintImage( gc );
      final int width = axis.getRenderer().getAxisWidth( axis );
      if( axis.getPosition().getOrientation() == ORIENTATION.VERTICAL )
        transform.translate( width, 0 );
      else
        transform.translate( 0, width );
      gc.setTransform( transform );
    }
  }

  private void resetTransform( final GC gc, final Transform transform )
  {
    gc.setTransform( null );
    gc.getTransform( transform );
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
