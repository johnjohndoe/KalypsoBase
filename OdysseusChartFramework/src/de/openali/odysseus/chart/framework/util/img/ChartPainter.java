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
import java.util.HashMap;
import java.util.Map;

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
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.util.img.legend.ChartLegendCanvas;
import de.openali.odysseus.chart.framework.util.img.legend.config.DefaultChartLegendConfig;

/**
 * @author kimwerner
 */
public class ChartPainter
{
  private final IChartModel m_model;

  public static final String CHART_INSETS = "CHART_INSETS"; //$NON-NLS-1$

  public static final String PLOT_INSETS = "PLOT_INSETS"; //$NON-NLS-1$

  private final ChartLegendCanvas m_legendPainter;

  private final Image m_image;

  private final ChartTitlePainter m_titlePainter;

  private final Rectangle m_paintRect;

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

  public ChartPainter( final IChartModel model, final Image image )
  {
    m_model = model;
    m_image = image;

    final Rectangle imageSize = image.getBounds();

    m_paintRect = RectangleUtils.inflateRect( imageSize, getChartInsets() );

    m_titlePainter = new ChartTitlePainter( m_model.getSettings().getTitles() );
    m_legendPainter = new ChartLegendCanvas( m_model, new DefaultChartLegendConfig( m_paintRect ) );
  }

  public final IStatus paintImage( final IProgressMonitor monitor )
  {
    final Insets plotFrameInsets = getPlotFrameInsets();

    final Insets plotInsets = getPlotInsets();
    setAxesHeight( plotInsets, m_paintRect );

    if( monitor.isCanceled() )
      return Status.CANCEL_STATUS;

    Image legendImage = null;

    final GC gc = new GC( m_image );
    try
    {
      gc.setAntialias( SWT.ON );
      gc.setTextAntialias( SWT.ON );
      gc.setAdvanced( true );

      final Point titleSize = m_titlePainter.getSize( m_paintRect.width );
      m_titlePainter.paint( gc, new Rectangle( m_paintRect.x, m_paintRect.y, m_paintRect.width, titleSize.y ) );

      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      // paint left Axes

      // FIXME: axis position are highly dubious: plotFrameInsets are already a part of the plotInsets, why subtract
      // now?

      paintAxes( POSITION.LEFT, gc, plotInsets.left - plotFrameInsets.left, plotInsets.top - plotFrameInsets.top, plotInsets.top, m_paintRect.height - plotInsets.top - plotInsets.bottom, 90, false );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      // paint right Axes
      paintAxes( POSITION.RIGHT, gc, m_paintRect.width - plotInsets.right + plotFrameInsets.right, plotInsets.top, plotInsets.top, m_paintRect.height - plotInsets.top, 90, true );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      // paint top Axes
      paintAxes( POSITION.TOP, gc, plotInsets.left, plotInsets.top, plotInsets.left, m_paintRect.width, 0, true );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      // paint bottom Axes
      paintAxes( POSITION.BOTTOM, gc, plotInsets.left, m_paintRect.height - plotInsets.bottom + plotFrameInsets.bottom, plotInsets.left, m_paintRect.width - plotInsets.left, 0, false );
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      // paint plot
      legendImage = m_legendPainter.createImage();
      if( legendImage != null )
        gc.drawImage( legendImage, m_paintRect.x, m_paintRect.y + m_paintRect.height - m_legendPainter.getSize().height );

      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      final Rectangle plotRect = RectangleUtils.createInnerRectangle( m_paintRect.width, m_paintRect.height, plotInsets );
      // Layer könnten sonst in die Achsen zeichnen
      paintFrameRect( gc, RectangleUtils.bufferRect( plotRect, plotFrameInsets ) );
      gc.setClipping( plotRect );

      final ChartPlotPainter plotPainter = new ChartPlotPainter( m_model, new Point( m_paintRect.width, m_paintRect.height ) );
      plotPainter.paint( gc, plotInsets, monitor );

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

  private ChartPlotFrame getPlotFrame( )
  {
    return m_model.getSettings().getPlotFrame();
  }

  private void paintFrameRect( final GC gc, final Rectangle rect )
  {
    getPlotFrame().paint( gc, rect );

  }

  private int getAxesWidth( final IAxis[] axes )
  {
    int width = 0; /* pixel */

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

  private ILineStyle getStyleFromAxes( final IAxis[] axes )
  {
    for( final IAxis axis : axes )
    {
      final IAxisRenderer renderer = axis.getRenderer();
      if( renderer != null && axis.isVisible() )
      {
        return renderer.getLineStyle();
      }
    }
    return null;
  }

  private final Insets getChartInsets( )
  {
    final Insets insets = m_model.getSettings().getInsets( CHART_INSETS );
    if( insets == null )
    {
      return new Insets( 3, 3, 3, 3 );
    }
    return insets;
  }

  private final Insets getPlotFrameInsets( )
  {
    final Insets insets = m_model.getSettings().getInsets( PLOT_INSETS );
    if( insets == null )
    {
      return new Insets( 0, 0, 0, 0 );
    }
    return insets;
  }

  public final Insets getPlotInsets( )
  {
    final Insets plotFrameInsets = getPlotFrameInsets();

    final Map<POSITION, Integer> insets = new HashMap<POSITION, Integer>();
    insets.put( POSITION.TOP, plotFrameInsets.top + m_titlePainter.getSize( m_paintRect.width ).y + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.TOP ) ) );
    insets.put( POSITION.BOTTOM, plotFrameInsets.bottom + m_legendPainter.getSize().height + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.BOTTOM ) ) );
    insets.put( POSITION.LEFT, plotFrameInsets.left + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.LEFT ) ) );
    insets.put( POSITION.RIGHT, plotFrameInsets.right + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.RIGHT ) ) );

    final ChartPlotFrame plotFrame = getPlotFrame();
    for( final POSITION position : POSITION.values() )
    {
      final ILineStyle style = getStyleFromAxes( m_model.getMapperRegistry().getAxesAt( position ) );
      if( style == null )
      {
        if( plotFrame.getFrameEdge( position ).getLineStyle().isVisible() )
        {
          plotFrame.getFrameEdge( position ).getLineStyle().setWidth( 1 );
          insets.put( position, insets.get( position ) + 1 );
        }
      }
      else
      {
        plotFrame.getFrameEdge( position ).setLineStyle( style );
      }
    }

    return new Insets( insets.get( POSITION.TOP ), insets.get( POSITION.LEFT ), insets.get( POSITION.BOTTOM ), insets.get( POSITION.RIGHT ) );
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

  private void setAxesHeight( final Insets plotInsets, final Rectangle size )
  {
    // FIXME: setting the screen height during repaint is very bad practice, because it triggers another repaint

    final int axisWidth = size.width - plotInsets.left - plotInsets.right;
    final int axisHeight = size.height - plotInsets.top - plotInsets.bottom;

    for( final IAxis axis : m_model.getMapperRegistry().getAxes() )
    {
      final ORIENTATION orientation = axis.getPosition().getOrientation();
      switch( orientation )
      {
        case HORIZONTAL:
          axis.setScreenHeight( axisWidth );
          break;

        case VERTICAL:
          axis.setScreenHeight( axisHeight );
          break;
      }
    }
  }
}