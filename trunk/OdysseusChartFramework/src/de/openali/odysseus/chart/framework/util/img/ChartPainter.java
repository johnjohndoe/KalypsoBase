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

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.util.img.legend.ChartLegendCanvas;

/**
 * @author kimwerner
 */
public class ChartPainter
{

  final IChartModel m_model;

  final Rectangle m_size;

  final Insets m_plotInsets;

  final ChartTitlePainter m_titlePainter;

  final ChartLegendCanvas m_legendPainter;

  final ChartPlotPainter m_plotPainter;

  public ChartPainter( final IChartModel model, final Rectangle size )
  {
    m_model = model;
    m_size = size;

    m_titlePainter = new ChartTitlePainter( model, size );
    final int left = getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.LEFT ) );
    final int right = getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.RIGHT ) );

    m_legendPainter = new ChartLegendCanvas( model, new Rectangle( left, 0, size.width - left - right, size.height ) );

    final int top = m_titlePainter.getSize().y + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.TOP ) );
    final int bottom = m_legendPainter.getSize().y + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.BOTTOM ) );

    m_plotInsets = new Insets( top, left, bottom, right );
    setAxesHeight();
    m_plotPainter = new ChartPlotPainter( model, new Point( size.width - m_plotInsets.left - m_plotInsets.right, size.height - m_plotInsets.bottom - m_plotInsets.top ) );
  }

  public final Insets getPlotInsets( )
  {
    return m_plotInsets;
  }

  private void setAxesHeight( )
  {
    for( final IAxis axis : m_model.getMapperRegistry().getAxes() )
    {
      if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        axis.setScreenHeight( m_size.width - m_plotInsets.left - m_plotInsets.right );
      else
        axis.setScreenHeight( m_size.height - m_plotInsets.top - m_plotInsets.bottom );
    }
  }

  private Image createAxesImage( final IAxis[] axes, final int width, final int height, final boolean horizontal )
  {
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    if( width < 1 || height < 1 )
    {
      return null;
    }
    final Image image = new Image( dev, width, height );
    final GC gc = new GC( image );
    int offsetX = 0;
    int offsetY = 0;
    try
    {
      for( final IAxis axis : axes )
      {
        final ChartAxisPainter axisPainter = new ChartAxisPainter( axis );
        final Image axisImage = axisPainter.createImage();
        if( axisImage == null )
          continue;

        try
        {
          gc.drawImage( axisImage, offsetX, offsetY );
          if( horizontal )
            offsetY += axisImage.getBounds().height;
          else
            offsetX += axisImage.getBounds().width;
        }
        finally
        {
          axisImage.dispose();
        }
      }
    }
    finally
    {
      gc.dispose();
    }
    return image;

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

  public final Image createImage( )
  {
    return createImage( new Point( 0, 0 ) );
  }

  public final Image createImage( final Point panOffset )
  {
    if( m_size.width == 0 || m_size.height == 0 )
      return null;

    final Device dev = PlatformUI.getWorkbench().getDisplay();

    final Image image = new Image( dev, m_size.width, m_size.height );
    final Image titleImage = m_titlePainter.paint();
    final Image legendImage = m_legendPainter.createImage();
    final Image plotImage = m_plotPainter.createImage();
    final Image leftImage = createAxesImage( m_model.getMapperRegistry().getAxesAt( POSITION.LEFT ), m_plotInsets.left, m_plotPainter.getSize().y, false );
    final Image topImage = createAxesImage( m_model.getMapperRegistry().getAxesAt( POSITION.TOP ), m_plotPainter.getSize().x, m_plotInsets.top - m_titlePainter.getSize().y, true );
    final Image rightImage = createAxesImage( m_model.getMapperRegistry().getAxesAt( POSITION.RIGHT ), m_plotInsets.right, m_plotPainter.getSize().y, false );
    final Image bottomImage = createAxesImage( m_model.getMapperRegistry().getAxesAt( POSITION.BOTTOM ), m_plotPainter.getSize().x, m_plotInsets.bottom - m_legendPainter.getSize().y, true );
    final GC gc = new GC( image );
    try
    {
      if( plotImage != null )
        gc.drawImage( plotImage, m_plotInsets.left - panOffset.x, m_plotInsets.top - panOffset.y );
      if( titleImage != null )
        gc.drawImage( titleImage, 0, 0 );
      if( topImage != null )
        gc.drawImage( topImage, m_plotInsets.left, m_titlePainter.getSize().y );
      if( bottomImage != null )
        gc.drawImage( bottomImage, m_plotInsets.left, m_size.height - m_plotInsets.bottom );
      if( leftImage != null )
        gc.drawImage( leftImage, 0, m_plotInsets.top );
      if( rightImage != null )
        gc.drawImage( rightImage, m_size.width - m_plotInsets.right, m_plotInsets.top );

      if( legendImage != null )
        gc.drawImage( legendImage, m_plotInsets.left, m_size.height - m_legendPainter.getSize().y );
    }
    finally
    {
      if( titleImage != null )
        titleImage.dispose();
      if( legendImage != null )
        legendImage.dispose();
      if( plotImage != null )
        plotImage.dispose();
      if( leftImage != null )
        leftImage.dispose();
      if( rightImage != null )
        rightImage.dispose();
      if( topImage != null )
        topImage.dispose();
      if( bottomImage != null )
        bottomImage.dispose();

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

}
