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
package de.openali.odysseus.chart.framework.view.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;
import de.openali.odysseus.chart.framework.util.img.ChartPainter;

/**
 * @author Dirk Kuch
 */
public class ChartPaintJob extends Job
{
  private final ChartImageComposite m_chart;

  final RGB m_backgroundRGB;

  private Image m_plotImage;

  private Image m_layerImage;

  private Rectangle m_clientArea = null;

  private ChartImageInfo m_plotInfo;

  private final UIJob m_redrawJob;

  private boolean m_doRedraw;

  private ImageData m_plotData;

  private ImageData m_layerData;

  public ChartPaintJob( final ChartImageComposite chart, final RGB backgroundRGB )
  {
    super( "Painting chart" ); //$NON-NLS-1$
    m_backgroundRGB = backgroundRGB;
    m_chart = chart;
    m_plotInfo = null;
    m_redrawJob = new ChartRedrawJob( this );
    m_redrawJob.setUser( false );
    m_redrawJob.setSystem( true );

    setSystem( true );
    setUser( false );
    setPriority( Job.LONG );
  }

  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    if( monitor.isCanceled() )
      return Status.OK_STATUS;

    if( m_chart.isDisposed() )
      return Status.OK_STATUS;

    /* start redrawing the chart regularly */
    setDoRedraw( true );
    final IStatus status = doPaint( m_clientArea, monitor );

    /* Stop redrawing */
    setDoRedraw( false );

    return status;
  }

  public ChartImageInfo getPlotInfo( )
  {
    return m_plotInfo;
  }

  private synchronized void setDoRedraw( final boolean doRedraw )
  {
    m_doRedraw = doRedraw;

    /* Even if stopping, redraw one last time */
    m_redrawJob.schedule( 250 );
  }

  synchronized boolean isDoRedraw( )
  {
    return m_doRedraw;
  }

  public synchronized void setClientArea( final Rectangle clientArea )
  {
    m_clientArea = clientArea;
  }

  private IStatus doPaint( final Rectangle bounds, final IProgressMonitor monitor )
  {
    if( m_chart.isDisposed() )
      return Status.OK_STATUS;

    if( monitor.isCanceled() )
      return Status.CANCEL_STATUS;
    if( bounds.width == 0 || bounds.height == 0 )
      return Status.OK_STATUS;
    final Display display = m_chart.getDisplay();
    // final Image plotImage = createPlotImage( bounds );
    // Hotfix: caching the image leads to a race condition and sometimes only a blank image is shown initially.
    final ImageData imageData = new ImageData( bounds.width, bounds.height, 24, new PaletteData( 0xFF, 0xFF00, 0xFF0000 ) );
    final Color colorBackground = new Color( display, m_backgroundRGB );
    imageData.transparentPixel = imageData.palette.getPixel( ChartPainter.COLOR_TRANSPARENT );

    final Image plotImage = new Image( display, imageData );

    final GC plotGC = new GC( plotImage );
    plotGC.setAntialias( SWT.ON );
    plotGC.setTextAntialias( SWT.ON );
    plotGC.setAdvanced( true );
    plotGC.setBackground( colorBackground );
    plotGC.fillRectangle( bounds );

    final Image layerImage = new Image( display, imageData );
    final GC layerGC = new GC( layerImage );
    layerGC.setAntialias( SWT.ON );
    layerGC.setTextAntialias( SWT.ON );
    layerGC.setAdvanced( true );
    layerGC.setBackground( colorBackground );
    layerGC.fillRectangle( bounds );

    synchronized( this )
    {
      m_plotImage = plotImage;
      m_layerImage = layerImage;
    }
    try
    {
      final IChartModel model = m_chart.getChartModel();
      if( model == null )
        return Status.OK_STATUS;

      final ChartPainter chartPainter = new ChartPainter( model, bounds );

      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      final IStatus status = chartPainter.paintImage( monitor, plotGC, layerGC);
      m_plotInfo = chartPainter.getInfoObject();
      return status;
    }
    finally
    {
      synchronized( this )
      {
        m_plotImage = null;
        m_plotData = plotImage.getImageData();
        plotGC.dispose();
        plotImage.dispose();

        m_layerImage = null;
        m_layerData = layerImage.getImageData();
        layerGC.dispose();
        layerImage.dispose();

        colorBackground.dispose();
      }
    }
  }

//  private synchronized Image createPlotImage( final Rectangle bounds )
//  {
//    Assert.isTrue( m_plotImage == null );
//
//    if( bounds.width > 0 && bounds.height > 0 )
//      m_plotImage = new Image( m_chart.getDisplay(), bounds.width, bounds.height );
//
//    return m_plotImage;
//  }

  public synchronized void dispose( )
  {
    cancel();

    m_doRedraw = false;

    m_plotData = null;
  }

  void redraw( )
  {
    if( m_chart.isDisposed() )
      return;

    synchronized( this )
    {
      if( m_plotImage != null )
        m_plotData = m_plotImage.getImageData();
      if( m_layerImage != null )
        m_layerData = m_layerImage.getImageData();
    }

    m_chart.redraw();
  }

  public synchronized ImageData getLayerImageData( )
  {
    return m_layerData;
  }

  public synchronized ImageData getPlotImageData( )
  {
    return m_plotData;
  }
}