package de.openali.odysseus.chart.framework.util.img;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.view.impl.AxisCanvas;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;
import de.openali.odysseus.chart.framework.view.impl.PlotCanvas;

/**
 * @author burtscher Creates an Image from a chart widget. The objects has to be disposed when it's no longer needed
 */
public class ChartImageFactory
{
  /**
   * @param transparency
   *          RGB to which the transparency-color should be set; transparency will not be used if RGB is null
   */
  public static ImageData createChartImage( final ChartComposite chart, final Device dev, final int width, final int height )
  {
    chart.setSize( new Point( width, height ) );
    chart.redraw();

    final Image img = new Image( dev, width, height );

    final GC gcw = new GC( img );

    gcw.drawLine( 0, 0, 100, 100 );

    // img mit weiï¿½em Hintergrund versehen
    gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    gcw.fillRectangle( 0, 0, width, height );

    // Plot zeichnen
    final PlotCanvas plotCanvas = chart.getPlot();
    final Image tmpImg = plotCanvas.createImage( chart.getChartModel().getLayerManager().getLayers(), img.getBounds() );
    gcw.drawImage( tmpImg, 0, 0, plotCanvas.getBounds().width, plotCanvas.getBounds().height, plotCanvas.getBounds().x, plotCanvas.getBounds().y, plotCanvas.getBounds().width, plotCanvas.getBounds().height );
    tmpImg.dispose();

    final IMapperRegistry ar = chart.getChartModel().getMapperRegistry();
    for( final IAxis axis : ar.getAxes() )
    {
      // Casten, damit man auf die Component-Eigenschaften zugreifen kann
      final AxisCanvas comp = chart.getAxisCanvas( axis );// (AxisCanvas) ac.getValue();
      final IAxisRenderer rend = ar.getRenderer( axis ); // axis.getRgetRenderer();

      final Image tempImg = new Image( dev, width, height );

      final GC tempGc = new GC( tempImg );
      // den Renderer in den TmpGC-Zeichnen lassen
      rend.paint( tempGc, axis, comp.getBounds() );

      /*
       * ...und ins Endbild kopieren; dabei muss berücksichtigt werden, dass die Components in 2 Ebenen Tiefe liegen -
       * zur Position muss noch die der Eltern addiert werden // TODO: paint directly in gcw (set an affine
       * transformation). Will probably be faster!
       */
      gcw.drawImage( tempImg, comp.getBounds().x, comp.getBounds().y, comp.getBounds().width, comp.getBounds().height, comp.getParent().getBounds().x + comp.getBounds().x, comp.getParent().getBounds().y
          + comp.getBounds().y, comp.getBounds().width, comp.getBounds().height );

      tempGc.dispose();
      tempImg.dispose();
    }

    gcw.dispose();

    final ImageData id = img.getImageData();

    img.dispose();

    return id;

  }
}
