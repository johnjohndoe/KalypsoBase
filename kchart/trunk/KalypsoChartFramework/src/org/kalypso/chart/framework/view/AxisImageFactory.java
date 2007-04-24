package org.kalypso.chart.framework.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
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

	public enum IMAGE_TYPE{
		PNG,
		JPG,
		GIF
	};
	
	
  /**
   *
   *
   *  @param transparency RGB to which the transparency-color should be set; transparency will not be used if RGB is null
   */
  @SuppressWarnings("unchecked")
public static ImageData createAxisImage( ChartComposite chart, String axisId, Device dev, int width, int height )
  {
    chart.setPlotSize( width, height );
    chart.layout();
    chart.update();
    
    
    //TODO: Idealerweise sollte hier kein Chart erzeugt werden, sondern nur der AxisCanvas
    IMapperRegistry mapperRegistry = chart.getModel().getMapperRegistry();
    IAxis axis = mapperRegistry.getAxis(axisId);
    AxisCanvas axisCanvas = (AxisCanvas) mapperRegistry.getComponent(axis);
    int axisWidth=axisCanvas.getBounds().width;
    int axisHeight=axisCanvas.getBounds().height;
    
    
    
    
    Image img = new Image( dev, axisWidth, axisHeight );

    GCWrapper gcw = new GCWrapper( new GC( img ) );
    Image tmpImg = new Image( dev, axisWidth, axisHeight );
    GC tmpGc = new GC( tmpImg );
    GCWrapper tmpGcw = new GCWrapper( tmpGc );
    
    
    tmpGcw.setBackground( dev.getSystemColor( SWT.COLOR_YELLOW ) );
    tmpGcw.fillRectangle( 0, 0, tmpImg.getBounds().width, tmpImg.getBounds().height );

    // img mit weiﬂem Hintergrund versehen
    gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE) );
    gcw.fillRectangle( 0, 0, width, height );

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
