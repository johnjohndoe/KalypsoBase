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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
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

  final ChartTitlePainter m_titlePainter;

  final ChartLegendCanvas m_legendPainter;

  private ChartPlotPainter m_plotPainter = null;

  private final Rectangle m_clientRect;

  // private final ChartPlotFrame m_plotFrame;

  public static String CHART_INSETS = "CHART_INSETS";

  public static String PLOT_INSETS = "PLOT_INSETS";

  public ChartPainter( final IChartModel model, final Rectangle size )
  {
    m_model = model;
    m_size = size;
    m_clientRect = RectangleUtils.inflateRect( m_size, getChartInsets() );
    m_legendPainter = new ChartLegendCanvas( m_model, new DefaultChartLegendConfig( m_clientRect ) );
    m_titlePainter = new ChartTitlePainter( model.getSettings().getTitles() );
  }

  public final Image createImage( )
  {
    return createImage( new Point( 0, 0 ) );
  }

  public final Image createImage( final Point panOffset )
  {
    if( m_clientRect.width < 1 || m_clientRect.height < 1 )
      return null;

    final Insets plotInsets = getPlotInsets();
    setAxesHeight( plotInsets, m_size );
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, m_size.width, m_size.height );
    final GC gc = new GC( image );
    final Image legendImage = m_legendPainter.createImage();
    try
    {
      gc.setAntialias( SWT.ON );
      gc.setTextAntialias( SWT.ON );
      gc.setAdvanced( true );

      m_titlePainter.paint( gc, new Rectangle( m_clientRect.x, m_clientRect.y, m_clientRect.width, m_titlePainter.getSize( m_clientRect.width ).y ) );

      // paint left Axes
      paintAxes( m_model.getMapperRegistry().getAxesAt( POSITION.LEFT ), gc, plotInsets.left - getPlotFrameInsets().left, plotInsets.top - getPlotFrameInsets().top, plotInsets.top, m_size.height
          - plotInsets.top - plotInsets.bottom, 90, false );
      // paint right Axes
      paintAxes( m_model.getMapperRegistry().getAxesAt( POSITION.RIGHT ), gc, m_size.width - plotInsets.right + getPlotFrameInsets().right, plotInsets.top, plotInsets.top, m_size.height
          - plotInsets.top, 90, true );
      // paint top Axes
      paintAxes( m_model.getMapperRegistry().getAxesAt( POSITION.TOP ), gc, plotInsets.left, plotInsets.top, plotInsets.left, m_clientRect.width, 0, true );
      // paint bottom Axes
      paintAxes( m_model.getMapperRegistry().getAxesAt( POSITION.BOTTOM ), gc, plotInsets.left, m_size.height - plotInsets.bottom + getPlotFrameInsets().bottom, plotInsets.left, m_size.width
          - plotInsets.left, 0, false );
      // paint plot

      if( legendImage != null )
        // gc.drawImage( legendImage, m_chartInsets.left, m_size.height - m_legendPainter.getSize().height -
// m_chartInsets.bottom );
        gc.drawImage( legendImage, m_clientRect.x, m_clientRect.y + m_clientRect.height - m_legendPainter.getSize().height );
      final Rectangle plotRect = RectangleUtils.createInnerRectangle( m_size.width, m_size.height, plotInsets );

      // Layer kˆnnten sonst in die Achsen zeichnen, 1Pixel aufweiten um f¸r die Layer Maximale Zeichenfl‰che zu
// schaffen
      paintFrameRect( gc, RectangleUtils.bufferRect( plotRect, getPlotFrameInsets() ) );
      gc.setClipping( plotRect );
      getPlotPainter().paint( gc, new Insets( plotInsets.top - panOffset.y, plotInsets.left - panOffset.x, plotInsets.bottom + panOffset.y, plotInsets.right + panOffset.x ) );

    }
    finally
    {
      if( legendImage != null )
        legendImage.dispose();
      gc.dispose();
    }

    return image;
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
    int width = 0/* pixel */;
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

  private final ILineStyle getStyleFromAxes( final IAxis[] axes )
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

  final Insets getChartInsets( )
  {
    final Insets insets = m_model.getSettings().getInsets( CHART_INSETS );
    if( insets == null )
    {
      return new Insets( 3, 3, 3, 3 );
    }
    return insets;
  }

  final Insets getPlotFrameInsets( )
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
    if( m_plotInsets == null )
    {
      final Map<POSITION, Integer> insets = new HashMap<POSITION, Integer>();
      insets.put( POSITION.TOP, getPlotFrameInsets().top + getChartInsets().top + m_titlePainter.getSize( m_clientRect.width ).y + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.TOP ) ) );
      insets.put( POSITION.BOTTOM, getPlotFrameInsets().bottom + getChartInsets().bottom + m_legendPainter.getSize().height + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.BOTTOM ) ) );
      insets.put( POSITION.LEFT, getPlotFrameInsets().left + getChartInsets().left + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.LEFT ) ) );
      insets.put( POSITION.RIGHT, getPlotFrameInsets().right + getChartInsets().right + getAxesWidth( m_model.getMapperRegistry().getAxesAt( POSITION.RIGHT ) ) );
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
      m_plotInsets = new Insets( insets.get( POSITION.TOP ), insets.get( POSITION.LEFT ), insets.get( POSITION.BOTTOM ), insets.get( POSITION.RIGHT ) );
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
