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
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * @author kimwerner
 */
public class ChartPainter
{

  final IChartModel m_model;

  final Point m_size;

  final Insets m_plotInsets;

  final ChartTitlePainter m_titlePainter;

  final ChartLegendPainter m_legendPainter;

  final ChartPlotPainter m_plotPainter;

  public ChartPainter( final IChartModel model, final Point size )
  {
    super();
    m_model = model;
    m_size = size;
    m_legendPainter = new ChartLegendPainter( model, size.x );
    m_titlePainter = new ChartTitlePainter( model, size.x );
    m_plotInsets = getPlotInsets();
    m_plotPainter = new ChartPlotPainter( model, new Point( size.x - m_plotInsets.left - m_plotInsets.right, size.y - m_plotInsets.bottom - m_plotInsets.top ) );
  }

  private Image createAxesImage( final IAxis[] axes, final int width, final int height, final boolean horizontal )
  {
    final Device dev = PlatformUI.getWorkbench().getDisplay();
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
        if( horizontal )
          offsetY += axisImage.getBounds().y;
        else
          offsetX += axisImage.getBounds().x;
        try
        {
          gc.drawImage( axisImage, offsetX, offsetY );
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

  public final Image createImage( )
  {
    final Device dev = PlatformUI.getWorkbench().getDisplay();

    final Image image = new Image( dev, m_size.x, m_size.y );
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
      gc.drawImage( titleImage, 0, 0 );
      gc.drawImage( topImage, m_plotInsets.left, m_titlePainter.getSize().y );
      gc.drawImage( bottomImage, m_plotInsets.left, m_plotInsets.bottom );
      gc.drawImage( leftImage, 0, m_plotInsets.top );
      gc.drawImage( rightImage, m_plotInsets.left + m_plotPainter.getSize().x, m_plotInsets.top );

    }
    finally
    {
      titleImage.dispose();
      legendImage.dispose();
      plotImage.dispose();
      leftImage.dispose();
      rightImage.dispose();
      topImage.dispose();
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

  public Insets getPlotInsets( )
  {

    final int top = m_titlePainter.getSize().y + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.TOP ) );
    final int left = getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.LEFT ) );
    final int bottom = m_legendPainter.getSize().y + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.BOTTOM ) );
    final int right = getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.RIGHT ) );

    return new Insets( top, left, bottom, right );
  }
}
