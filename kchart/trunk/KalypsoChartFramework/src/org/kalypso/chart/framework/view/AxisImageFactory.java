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
 * @author burtscher
 *
 * Creates an Image from a chart widget.
 *
 * The objects has to be disposed when it's no longer needed
 */
public class AxisImageFactory
{
 
  public static ImageData createAxisImageFromChart( ChartComposite chart, String axisId, Device dev, int width, int height )
  {
    chart.setPlotSize( width, height );
    chart.layout();
    chart.update();
    
    //TODO: Idealerweise sollte hier kein Chart erzeugt werden, sondern nur der AxisCanvas
    IMapperRegistry mapperRegistry = chart.getModel().getMapperRegistry();
    IAxis axis = mapperRegistry.getAxis(axisId);
    AxisCanvas axisCanvas = (AxisCanvas) mapperRegistry.getComponent(axis);
    return createAxisImage(axisCanvas, dev);
  }
  
  public static ImageData createAxisImageFromShell( Shell shell, IMapperRegistry mapperRegistry, String axisId, Device dev, int width, int height )
  {
	IAxis axis=mapperRegistry.getAxis(axisId);
    AxisCanvas axisCanvas = (AxisCanvas) mapperRegistry.getComponent(axis);
    IAxisRenderer axisRenderer=mapperRegistry.getRenderer(axis);
    int axisWidth = axisRenderer.getAxisWidth(axis);
    int canvasHeight=0;
    int canvasWidth=0;
    
    if (axis.getPosition().getOrientation()==ORIENTATION.VERTICAL)
    {
    	canvasHeight=height;
    	canvasWidth=axisWidth;
    }
    else
    {
    	canvasHeight=axisWidth;
    	canvasWidth=width;
    }
    
    axisCanvas.setSize(canvasWidth, canvasHeight);

    System.out.println(shell.getBounds());
    System.out.println(axisCanvas.getBounds());
    return createAxisImage(axisCanvas, dev);
  }
  
  
  public static ImageData createAxisImage(AxisCanvas axisCanvas, Device dev)
  { 
	    int axisWidth=axisCanvas.getBounds().width;
	    int axisHeight=axisCanvas.getBounds().height;
	    
	    Image img = new Image( dev, axisWidth, axisHeight );

	    GCWrapper gcw = new GCWrapper( new GC( img ) );
	    Image tmpImg = new Image( dev, axisWidth, axisHeight );
	    GC tmpGc = new GC( tmpImg );
	    GCWrapper tmpGcw = new GCWrapper( tmpGc );
	    
	    tmpGcw.setBackground( dev.getSystemColor( SWT.COLOR_YELLOW ) );
	    tmpGcw.fillRectangle( 0, 0, axisWidth, axisHeight );

	    // img mit weiﬂem Hintergrund versehen
	    gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE) );
	    gcw.fillRectangle( 0, 0, axisWidth, axisHeight );

	    // Axis zeichnen
	    tmpImg = axisCanvas.paintBuffered( tmpGcw, img.getBounds(), null );
	    gcw.drawImage( tmpImg, 0, 0);
	    gcw.drawImage(tmpImg, 0,0 );
	   
	    gcw.dispose();
	    tmpGc.dispose();
	    tmpGcw.dispose();
	    tmpImg.dispose();
	    
	    ImageData id=img.getImageData();
	    
	    img.dispose();
	    return id;
  }
}
