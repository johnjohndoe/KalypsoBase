package de.openali.odysseus.chart.framework.util.img;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
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
@Deprecated
/** 
 * use ChartImageFactory instead
 */
public class AxisImageFactory
{

  public static ImageData createAxisImageFromChart( final ChartComposite chart, final String axisId, final Device dev, final int width, final int height )
  {
    // chart.setPlotSize( width, height );
    ChartImageFactory.setAxesHeight( chart.getChartModel().getMapperRegistry().getAxes(), ChartImageFactory.calculatePlotSize( chart.getChartModel().getMapperRegistry(), width, height ) );
    chart.layout();
    chart.update();

    // TODO: Idealerweise sollte hier kein Chart erzeugt werden, sondern nur der AxisCanvas
    final IMapperRegistry mapperRegistry = chart.getChartModel().getMapperRegistry();
    final IAxis axis = mapperRegistry.getAxis( axisId );
    final AxisCanvas axisCanvas = chart.getAxisCanvas( axis );// (AxisCanvas) mapperRegistry.getComponent( axis );
    return createAxisImage( axisCanvas, dev );
  }

  public static ImageData createAxisImageFromShell( final Shell shell, final ChartComposite chart, final String axisId, final Device dev, final int width, final int height )
  {
    final IMapperRegistry mapperRegistry = chart.getChartModel().getMapperRegistry();
    final IAxis axis = mapperRegistry.getAxis( axisId );
    final AxisCanvas axisCanvas = chart.getAxisCanvas( axis );
    final IAxisRenderer axisRenderer = axis.getRenderer();

    final int axisWidth = axisRenderer == null ? 1 : axisRenderer.getAxisWidth( axis );
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

    if( axisRenderer == null )
    {
      System.out.println( "no Axis Renderer found for " + axis.getId() + " -> size set to 1" );
    }
    System.out.println( shell.getBounds() );
    System.out.println( axisCanvas.getBounds() );
    return createAxisImage( axisCanvas, dev );
  }

  public static ImageData createAxisImage( final AxisCanvas axisCanvas, final Device dev )
  {
    final Rectangle bounds = axisCanvas.getBounds();
// final int axisWidth = axisCanvas.getBounds().width;
// final int axisHeight = axisCanvas.getBounds().height;

    final Image img = new Image( dev, bounds.width, bounds.height );

    final GC gcw = new GC( img );
// Image tmpImg = new Image( dev, axisWidth, axisHeight );
// final GC tmpGc = new GC( tmpImg );
//
// tmpGc.setBackground( dev.getSystemColor( SWT.COLOR_YELLOW ) );
// tmpGc.fillRectangle( 0, 0, axisWidth, axisHeight );
//
// // img mit wei�em Hintergrund versehen
// gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
// gcw.fillRectangle( 0, 0, axisWidth, axisHeight );

    // Axis zeichnen
// tmpImg = axisCanvas.paintBuffered( tmpGc, img.getBounds(), null );
    final Image axisImg = axisCanvas.createBufferImage( bounds );
    gcw.drawImage( axisImg, 0, 0 );
    axisImg.dispose();
    // gcw.drawImage( tmpImg, 0, 0 );

// tmpGc.dispose();
// tmpImg.dispose();

    final ImageData id = img.getImageData();
    img.dispose();
    gcw.dispose();
    return id;
  }
}
