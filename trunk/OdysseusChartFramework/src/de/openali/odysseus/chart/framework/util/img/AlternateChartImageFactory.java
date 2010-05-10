package de.openali.odysseus.chart.framework.util.img;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.view.impl.AxisCanvas;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;
import de.openali.odysseus.chart.framework.view.impl.PlotCanvas;

/**
 * Creates an Image from a chart widget. The objects has to be disposed when it's no longer needed This alternate
 * version demonstrates that chart images can be created without cut tick values
 * 
 * @author burtscher
 */
public class AlternateChartImageFactory
{

  public enum IMAGE_TYPE
  {
    PNG,
    JPG,
    GIF
  }

  /**
   * @param transparency
   *          RGB to which the transparency-color should be set; transparency will not be used if RGB is null
   */
  public static ImageData createChartImage( ChartComposite chart, Device dev, int width, int height )
  {
    chart.setSize( new Point( width, height ) );
    chart.redraw();

    final Image img = new Image( dev, width, height );

    final GC gcw = new GC( img );

    gcw.drawLine( 0, 0, 100, 100 );

    Image tmpImg = new Image( dev, width, height );
    GC tmpGc = new GC( tmpImg );

    tmpGc.setBackground( dev.getSystemColor( SWT.COLOR_YELLOW ) );
    tmpGc.fillRectangle( 0, 0, tmpImg.getBounds().width, tmpImg.getBounds().height );

    // img mit weiﬂem Hintergrund versehen
    gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    gcw.fillRectangle( 0, 0, width, height );

    // Plot zeichnen
    final PlotCanvas plotCanvas = chart.getPlot();
    tmpImg = plotCanvas.paintBuffered( chart.getChartModel().getLayerManager().getLayers(), tmpGc, img.getBounds(), null );
    gcw.drawImage( tmpImg, 0, 0, plotCanvas.getBounds().width, plotCanvas.getBounds().height, plotCanvas.getBounds().x, plotCanvas.getBounds().y, plotCanvas.getBounds().width, plotCanvas.getBounds().height );
    tmpGc.fillRectangle( 0, 0, tmpImg.getBounds().width, tmpImg.getBounds().height );

    final IMapperRegistry ar = chart.getChartModel().getMapperRegistry();
// final Map<IAxis, IAxisComponent> components = ar.getAxesToComponentsMap();
// final Set<Entry<IAxis, IAxisComponent>> acs = components.entrySet();
// for( final Entry<IAxis, IAxisComponent> ac : acs )
    for( final IAxis axis : ar.getAxes() )
    {
      // final IAxis axis = ac.getKey();
      // Casten, damit man auf die Component-Eigenschaften zugreifen kann
      final AxisCanvas comp = chart.getAxisCanvas( axis );// (AxisCanvas) ac.getValue();
      final IAxisRenderer rend = ar.getRenderer( axis ); // axis.getRgetRenderer();

      tmpImg.dispose();
      Rectangle cBounds = comp.getBounds();

      int offset = 100;

      Rectangle extBounds = null;
      Image extImg = null;

      // Bounds so setzen, dass der offset mit eingerechnet wird
      if( axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
      {
        extBounds = new Rectangle( offset, 0, cBounds.width, cBounds.height );
        extImg = new Image( dev, cBounds.width + 2 * offset, cBounds.height );
      }
      else
      {
        extBounds = new Rectangle( 0, offset, cBounds.width, cBounds.height );
        extImg = new Image( dev, cBounds.width, cBounds.height + 2 * offset );
      }
      Color white = dev.getSystemColor( SWT.COLOR_WHITE );
      extImg.setBackground( white );
      GC imgGC = new GC( extImg );
      rend.paint( imgGC, axis, extBounds );
      imgGC.dispose();

      ImageData extId = extImg.getImageData();
      extId.transparentPixel = 0xffffff;

      extImg.dispose();
      Image newImg = new Image( dev, extId );

      int srcX = comp.getBounds().x;
      int srcY = comp.getBounds().y;
      int srcWidth = comp.getBounds().width + 2 * extBounds.x;
      int srcHeight = comp.getBounds().height + 2 * extBounds.y;
      int destX = comp.getParent().getBounds().x + comp.getBounds().x - extBounds.x;
      int destY = comp.getParent().getBounds().y + comp.getBounds().y - extBounds.y;
      int destWidth = comp.getBounds().width + 2 * extBounds.x;
      int destHeight = comp.getBounds().height + 2 * extBounds.y;

      gcw.drawImage( newImg, srcX, srcY, srcWidth, srcHeight, destX, destY, destWidth, destHeight );
    }
    gcw.dispose();
    tmpGc.dispose();
    tmpImg.dispose();

    final ImageData id = img.getImageData();

    img.dispose();

    return id;

  }
}
