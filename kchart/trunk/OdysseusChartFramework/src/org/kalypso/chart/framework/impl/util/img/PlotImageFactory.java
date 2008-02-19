package org.kalypso.chart.framework.impl.util.img;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.kalypso.chart.framework.impl.view.ChartComposite;
import org.kalypso.chart.framework.impl.view.PlotCanvas;

/**
 * @author burtscher Creates an Image from a chart widget. The objects has to be disposed when it's no longer needed
 */
public class PlotImageFactory
{

  public enum IMAGE_TYPE
  {
    PNG,
    JPG,
    GIF
  }

  /**
   * @param transparency
   *            RGB to which the transparency-color should be set; transparency will not be used if RGB is null
   */
  @SuppressWarnings("unchecked")
  public static ImageData createPlotImage( ChartComposite chart, Device dev, int width, int height )
  {
    chart.setPlotSize( width, height );
    chart.layout();
    chart.update();

    final Image img = new Image( dev, width, height );

    final GC gcw = new GC( img  );
    Image tmpImg = new Image( dev, width, height );
    final GC tmpGc = new GC( tmpImg );

    // img mit wei�em Hintergrund versehen
    gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    gcw.fillRectangle( 0, 0, width, height );

    // Plot zeichnen
    final PlotCanvas plotCanvas = chart.getPlot();
    tmpImg = plotCanvas.paintBuffered( tmpGc, img.getBounds(), null );
    gcw.drawImage( tmpImg, 0, 0, plotCanvas.getBounds().width, plotCanvas.getBounds().height, plotCanvas.getBounds().x, plotCanvas.getBounds().y, plotCanvas.getBounds().width, plotCanvas.getBounds().height );
    gcw.drawImage( tmpImg, 0, 0 );

    gcw.dispose();
    tmpGc.dispose();
    tmpImg.dispose();

    final ImageData id = img.getImageData();

    img.dispose();

    return id;

  }
}
