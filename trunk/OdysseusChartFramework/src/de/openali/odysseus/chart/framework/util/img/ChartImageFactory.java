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

    // Image tmpImg = new Image( dev, width, height );
    // GC tmpGc = new GC( tmpImg );

    // tmpGc.setBackground( dev.getSystemColor( SWT.COLOR_YELLOW ) );
    // tmpGc.fillRectangle( 0, 0, tmpImg.getBounds().width, tmpImg.getBounds().height );

    // img mit weiï¿½em Hintergrund versehen
    gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    gcw.fillRectangle( 0, 0, width, height );

    // Plot zeichnen
    final PlotCanvas plotCanvas = chart.getPlot();
    Image tmpImg = plotCanvas.createImage( chart.getChartModel().getLayerManager().getLayers(), img.getBounds() );
    gcw.drawImage( tmpImg, 0, 0, plotCanvas.getBounds().width, plotCanvas.getBounds().height, plotCanvas.getBounds().x, plotCanvas.getBounds().y, plotCanvas.getBounds().width, plotCanvas.getBounds().height );
    tmpImg.dispose();

    final IMapperRegistry ar = chart.getChartModel().getMapperRegistry();
// final Map<IAxis, IAxisComponent> components = ar.getAxesToComponentsMap();
// final Set<Entry<IAxis, IAxisComponent>> acs = components.entrySet();
// for( final Entry<IAxis, IAxisComponent> ac : acs )
// {
    for( final IAxis axis : ar.getAxes() )
    {
      final IAxisRenderer rend = axis.getRenderer();
      if( rend == null )
        continue;
      // final IAxis axis = ac.getKey();
      // Casten, damit man auf die Component-Eigenschaften zugreifen kann
      final AxisCanvas comp = chart.getAxisCanvas( axis );// (AxisCanvas) ac.getValue();
// final IAxis axis = ac.getKey();
// // Casten, damit man auf die Component-Eigenschaften zugreifen kann
// final AxisCanvas comp = (AxisCanvas) ac.getValue();

      // Wenn man den GC nicht neu erzeugt, werden die AChsen nicht
      // gezeichnet, sondern nochmal das Chart
      // Zusätzlich muss das Image disposed werden, weil sonst noch
      // Teile des Plot in die AxisSpaces gezeichnet werden

      tmpImg = new Image( dev, width, height );
      GC tmpGc = new GC( tmpImg );
      // den Renderer in den TmpGC-Zeichnen lassen
      rend.paint( tmpGc, axis, comp.getBounds() );
      /*
       * ...und ins Endbild kopieren; dabei muss berï¿½cksichtigt werden, dass die Components in 2 Ebenen Tiefe liegen -
       * zur Position muss noch die der Eltern addiert werden
       */
      gcw.drawImage( tmpImg, comp.getBounds().x, comp.getBounds().y, comp.getBounds().width, comp.getBounds().height, comp.getParent().getBounds().x + comp.getBounds().x, comp.getParent().getBounds().y
          + comp.getBounds().y, comp.getBounds().width, comp.getBounds().height );

      tmpGc.dispose();
      tmpImg.dispose();
    }
    gcw.dispose();
    final ImageData id = img.getImageData();

    img.dispose();

    return id;

  }
}
