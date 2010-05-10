package de.openali.odysseus.chart.framework.util.img;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Shell;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.view.impl.AxisCanvas;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

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
    final IMapperRegistry mapperRegistry = chart.getChartModel().getMapperRegistry();
    final IAxis axis = mapperRegistry.getAxis( axisId );
    final AxisCanvas axisCanvas = chart.getAxisCanvas( axis );// (AxisCanvas) mapperRegistry.getComponent( axis );
    return createAxisImage( axisCanvas, dev );
  }

  public static ImageData createAxisImageFromShell( Shell shell, ChartComposite chart, String axisId, Device dev, int width, int height )
  {
    final IMapperRegistry mapperRegistry = chart.getChartModel().getMapperRegistry();
    final IAxis axis = mapperRegistry.getAxis( axisId );
    final AxisCanvas axisCanvas = chart.getAxisCanvas( axis );
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

    final GC gcw = new GC( img );
    Image tmpImg = new Image( dev, axisWidth, axisHeight );
    final GC tmpGc = new GC( tmpImg );

    tmpGc.setBackground( dev.getSystemColor( SWT.COLOR_YELLOW ) );
    tmpGc.fillRectangle( 0, 0, axisWidth, axisHeight );

    // img mit wei�em Hintergrund versehen
    gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    gcw.fillRectangle( 0, 0, axisWidth, axisHeight );

    // Axis zeichnen
    tmpImg = axisCanvas.paintBuffered( tmpGc, img.getBounds(), null );
    gcw.drawImage( tmpImg, 0, 0 );
    gcw.drawImage( tmpImg, 0, 0 );

    gcw.dispose();
    tmpGc.dispose();
    tmpImg.dispose();

    final ImageData id = img.getImageData();

    img.dispose();
    return id;
  }
}
