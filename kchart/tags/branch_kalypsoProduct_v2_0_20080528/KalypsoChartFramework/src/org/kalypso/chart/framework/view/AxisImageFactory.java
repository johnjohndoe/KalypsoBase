package org.kalypso.chart.framework.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author burtscher Creates an Image from a chart widget. The objects has to be disposed when it's no longer needed
 */
public class AxisImageFactory
{

  public static ImageData createAxisImageFromChart( ChartComposite chart, String axisId, Device dev, int width, int height )
  {
    chart.setPlotSize( width, height );
    chart.layout();
    chart.update();

    // TODO: Idealerweise sollte hier kein Chart erzeugt werden, sondern nur der AxisCanvas
    final IMapperRegistry mapperRegistry = chart.getModel().getMapperRegistry();
    final IAxis axis = mapperRegistry.getAxis( axisId );
    final AxisCanvas axisCanvas = (AxisCanvas) mapperRegistry.getComponent( axis );
    return createAxisImage( axisCanvas, dev );
  }

  public static ImageData createAxisImageFromShell( Shell shell, IMapperRegistry mapperRegistry, String axisId, Device dev, int width, int height )
  {
    final IAxis axis = mapperRegistry.getAxis( axisId );
    final AxisCanvas axisCanvas = (AxisCanvas) mapperRegistry.getComponent( axis );
    final IAxisRenderer axisRenderer = mapperRegistry.getRenderer( axis );
    final int axisWidth = axisRenderer.getAxisWidth( axis );
    int canvasHeight = 0;
    int canvasWidth = 0;

    if( axis.getPosition().getOrientation() == ORIENTATION.VERTICAL )
    {
      canvasHeight = height;
      canvasWidth = axisWidth;
    }
    else
    {
      canvasHeight = axisWidth;
      canvasWidth = width;
    }

    axisCanvas.setSize( canvasWidth, canvasHeight );

    System.out.println( shell.getBounds() );
    System.out.println( axisCanvas.getBounds() );
    return createAxisImage( axisCanvas, dev );
  }

  public static ImageData createAxisImage( AxisCanvas axisCanvas, Device dev )
  {
    final int axisWidth = axisCanvas.getBounds().width;
    final int axisHeight = axisCanvas.getBounds().height;

    final Image img = new Image( dev, axisWidth, axisHeight );

    final GCWrapper gcw = new GCWrapper( new GC( img ) );
    Image tmpImg = new Image( dev, axisWidth, axisHeight );
    final GC tmpGc = new GC( tmpImg );
    final GCWrapper tmpGcw = new GCWrapper( tmpGc );

    tmpGcw.setBackground( dev.getSystemColor( SWT.COLOR_YELLOW ) );
    tmpGcw.fillRectangle( 0, 0, axisWidth, axisHeight );

    // img mit weiﬂem Hintergrund versehen
    gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    gcw.fillRectangle( 0, 0, axisWidth, axisHeight );

    // Axis zeichnen
    tmpImg = axisCanvas.paintBuffered( tmpGcw, img.getBounds(), null );
    gcw.drawImage( tmpImg, 0, 0 );
    gcw.drawImage( tmpImg, 0, 0 );

    gcw.dispose();
    tmpGc.dispose();
    tmpGcw.dispose();
    tmpImg.dispose();

    final ImageData id = img.getImageData();

    img.dispose();
    return id;
  }
}
