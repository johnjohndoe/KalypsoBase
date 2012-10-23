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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
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
import de.openali.odysseus.chart.framework.model.impl.settings.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chart.framework.util.img.legend.ChartLegendCanvas;
import de.openali.odysseus.chart.framework.util.img.legend.config.DefaultChartLegendConfig;

/**
 * @author kimwerner
 */
public class ChartPainter
{
  private final IChartModel m_model;

  private ChartImageInfo m_infoObject;

  public static Image createChartImage( final IChartModel model, final Rectangle size, final IProgressMonitor monitor )
  {
    if( size.width == 0 || size.height == 0 )
      return null;

    final Device dev = ChartUtilities.getDisplay();
    final Image image = new Image( dev, size.width, size.height );

    final ChartPainter painter = new ChartPainter( model, image );
    painter.paintImage( monitor );

    return image;
  }

  public static ImageData createChartImageData( final IChartModel model, final Rectangle size, final IProgressMonitor monitor )
  {
    final Image image = createChartImage( model, size, monitor );
    try
    {
      return image.getImageData();
    }
    finally
    {
      image.dispose();
    }
  }

  private final ChartLegendCanvas m_legendPainter;

  private final Image m_image;

  private final ChartTitlePainter m_titlePainter;

  private final Rectangle m_paintRect;

  public ChartPainter( final IChartModel model, final Image image )
  {
    m_model = model;
    m_image = image;

    final Rectangle imageSize = image.getBounds();

    final IBasicChartSettings settings = m_model.getSettings();

    m_paintRect = RectangleUtils.inflateRect( imageSize, settings.getChartInsets() );

    m_titlePainter = new ChartTitlePainter( settings.getTitles() );
    m_legendPainter = new ChartLegendCanvas( m_model, new DefaultChartLegendConfig( m_paintRect ) );
  }

  private final Rectangle calculateAxisBottomRect( final Rectangle usableRect )
  {
    final int totalWidth = getAxisWidth( POSITION.BOTTOM );
    return new Rectangle( usableRect.x, usableRect.y + usableRect.height - totalWidth, usableRect.width, totalWidth );
  }

  private final Rectangle calculateAxisLeftRect( final Rectangle usableRect )
  {
    final int totalWidth = getAxisWidth( POSITION.LEFT );
    return new Rectangle( usableRect.x, usableRect.y, totalWidth, usableRect.height );
  }

  private final Rectangle calculateAxisRightRect( final Rectangle usableRect )
  {
    final int totalWidth = getAxisWidth( POSITION.RIGHT );
    return new Rectangle( usableRect.x + usableRect.width - totalWidth, usableRect.y, totalWidth, usableRect.height );
  }

  private final Rectangle calculateAxisTopRect( final Rectangle usableRect )
  {
    final int totalWidth = getAxisWidth( POSITION.TOP );
    return new Rectangle( usableRect.x, usableRect.y, usableRect.width, totalWidth );
  }

  private final Rectangle calculateLegendRect( final Rectangle chartRect, final Image legendImage )
  {
    final int legendHeight = legendImage == null ? 0 : legendImage.getBounds().height;
    return new Rectangle( chartRect.x, chartRect.y + chartRect.height - legendHeight, chartRect.width, legendHeight );
  }

  private final Rectangle calculateTitleRect( final Rectangle chartRect )
  {
    final Point titleSize = m_titlePainter.getSize( m_paintRect.width );
    return new Rectangle( chartRect.x, chartRect.y, titleSize.x, titleSize.y );
  }

  private final int getAxisWidth( final POSITION position )
  {
    final IAxis[] axes = m_model.getMapperRegistry().getAxesAt( position );
    int totalWidth = 0;
    for( final IAxis axis : axes )
    {
      final IAxisRenderer renderer = axis.getRenderer();
      if( renderer != null )
      {
        totalWidth += renderer.getAxisWidth( axis );
      }
    }
    return totalWidth;
  }

  public ChartImageInfo getInfoObject( )
  {
    return m_infoObject;
  }

  private void paintAxes( final POSITION position, final GC gc, final int anchorX, final int anchorY, final int startOffset, final int screenWidth, final int rotation, final boolean invertVertical )
  {
    final IAxis[] axes = m_model.getMapperRegistry().getAxesAt( position );

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

  private void paintFrameRect( final GC gc, final Rectangle rect )
  {
    final ChartPlotFrame plotFrame = m_model.getSettings().getPlotFrame();
    for( final POSITION position : POSITION.values() )
    {
      final ChartPlotFrameEdge edge = plotFrame.getFrameEdge( position );
      if( edge.getLineStyle() == null )
      {
        final ILineStyle lineStyle = StyleUtils.getDefaultLineStyle();
        lineStyle.setWidth( 1 );
        edge.setLineStyle( lineStyle );
        for( final IAxis axis : m_model.getMapperRegistry().getAxesAt( position ) )
        {
          if( axis.isVisible() )
          {
            edge.setLineStyle( axis.getRenderer().getLineStyle() );
            break;
          }
        }
      }
    }
    plotFrame.paint( gc, rect );
// final int old = gc.getLineWidth();
// gc.setLineWidth( 1 );
// gc.drawRectangle( rect );
// gc.setLineWidth( old );

  }

  public final IStatus paintImage( final IProgressMonitor monitor )
  {
    if( monitor.isCanceled() )
      return Status.CANCEL_STATUS;

    final Image legendImage = m_legendPainter.createImage();
    final ChartImageInfo infoObject = new ChartImageInfo();

    final GC gc = new GC( m_image );
    try
    {
      gc.setAntialias( SWT.ON );
      gc.setTextAntialias( SWT.ON );
      gc.setAdvanced( true );

      infoObject.setClientRect( m_paintRect );

      final Insets plotInsets = m_model.getSettings().getPlotInsets();

      final Rectangle titleRect = calculateTitleRect( m_paintRect );
      infoObject.setTitleRect( titleRect );
      final Rectangle legendRect = calculateLegendRect( m_paintRect, legendImage );
      infoObject.setLegendRect( legendRect );
      final Rectangle usableAxisRect = new Rectangle( m_paintRect.x, m_paintRect.y + titleRect.height, m_paintRect.width, m_paintRect.height - titleRect.height - legendRect.height );
      final Rectangle axisTopRect = calculateAxisTopRect( usableAxisRect );
      final Rectangle axisBottomRect = calculateAxisBottomRect( usableAxisRect );
      final Rectangle axisLeftRect = calculateAxisLeftRect( usableAxisRect );
      final Rectangle axisRightRect = calculateAxisRightRect( usableAxisRect );
      infoObject.setAxisBottomRect( axisBottomRect );
      infoObject.setAxisLeftRect( axisLeftRect );
      infoObject.setAxisRightRect( axisRightRect );
      infoObject.setAxisTopRect( axisTopRect );
      final Rectangle plotRect = new Rectangle( usableAxisRect.x + axisLeftRect.width, usableAxisRect.y + axisTopRect.height, usableAxisRect.width - axisLeftRect.width - axisRightRect.width, usableAxisRect.height
          - axisTopRect.height - axisBottomRect.height );
      infoObject.setPlotRect( plotRect );

      final Rectangle plotClientRect = RectangleUtils.inflateRect( plotRect, plotInsets );

      infoObject.setLayerRect( plotClientRect );
      m_infoObject = infoObject;

      // FIXME: plot insets do not really act like expected; check this...
      setAxesHeight( plotRect, plotRect );

      m_titlePainter.paint( gc, titleRect );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;
      paintFrameRect( gc, plotRect );
      paintAxes( POSITION.TOP, gc, plotRect.x, axisTopRect.y + axisTopRect.height, plotInsets.left, axisTopRect.width, 0, true );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;
      
      paintAxes( POSITION.LEFT, gc, plotRect.x, axisLeftRect.y, plotInsets.top, axisLeftRect.height - axisTopRect.height - axisBottomRect.height, 90, false );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;
      
      paintAxes( POSITION.RIGHT, gc, axisRightRect.x, axisRightRect.y, plotInsets.top, axisRightRect.height - axisTopRect.height - axisBottomRect.height, 90, true );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;
      
      paintAxes( POSITION.BOTTOM, gc, plotRect.x, axisBottomRect.y, plotInsets.left, axisBottomRect.width - axisLeftRect.width - axisRightRect.width, 0, false );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;
      
      if( legendImage != null )
        gc.drawImage( legendImage, legendRect.x, legendRect.y );

      // Layer könnten sonst in die Achsen zeichnen
      // REMARK: not the plotClientRect, the layer may still paint into the insets
      gc.setClipping( plotRect );

      final ChartPlotPainter plotPainter = new ChartPlotPainter( m_model, new Point( plotClientRect.width, plotClientRect.height ) );
      plotPainter.paint( gc, infoObject, monitor );

      return Status.OK_STATUS;
    }
    catch( final OperationCanceledException e )
    {
      return Status.CANCEL_STATUS;
    }
    finally
    {
      if( legendImage != null )
        legendImage.dispose();

      gc.dispose();
    }
  }

  private void setAxesHeight( final Rectangle plotRect, final Rectangle size )
  {
    final int axisWidth = size.width;// - plotInsets.left - plotInsets.right;
    final int axisHeight = size.height;// - plotInsets.top - plotInsets.bottom;

    for( final IAxis axis : m_model.getMapperRegistry().getAxes() )
    {
      final ORIENTATION orientation = axis.getPosition().getOrientation();
      switch( orientation )
      {
        case HORIZONTAL:
          axis.setScreenOffset( plotRect.x, axisWidth );
          break;

        case VERTICAL:
          axis.setScreenOffset( plotRect.y, axisHeight );
          break;
      }
    }
  }
}