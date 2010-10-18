package de.openali.odysseus.chart.framework.util.img;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
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

  public static ImageData createChartImage( final IChartModel model, final int width, final int height )
  {
    final Point plotTopLeft = new Point( 0, 0 );
    for( final IAxis axis : model.getMapperRegistry().getAxesAt( POSITION.TOP ) )
    {
      if( axis.isVisible() )
      {
        final IAxisRenderer renderer = axis.getRenderer();
        plotTopLeft.y = plotTopLeft.y + renderer.getAxisWidth( axis );
      }
    }
    for( final IAxis axis : model.getMapperRegistry().getAxesAt( POSITION.LEFT ) )
    {
      if( axis.isVisible() )
      {
        final IAxisRenderer renderer = axis.getRenderer();
        plotTopLeft.x = plotTopLeft.x + renderer.getAxisWidth( axis );
      }
    }
    final Point plotWidthHight = new Point( width-plotTopLeft.x, height-plotTopLeft.y );
    for( final IAxis axis : model.getMapperRegistry().getAxesAt( POSITION.BOTTOM ) )
    {
      if( axis.isVisible() )
      {
        final IAxisRenderer renderer = axis.getRenderer();
        plotWidthHight.y = plotWidthHight.y - renderer.getAxisWidth( axis );
      }
    }
    for( final IAxis axis : model.getMapperRegistry().getAxesAt( POSITION.RIGHT ) )
    {
      if( axis.isVisible() )
      {
        final IAxisRenderer renderer = axis.getRenderer();
        plotWidthHight.x = plotWidthHight.x - renderer.getAxisWidth( axis );
      }
    }

    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image bufferImage = new Image( dev, width, height );

    final GC buffGc = new GC( bufferImage );

    try
    {
      int h = 0;
      for( final IAxis axis : model.getMapperRegistry().getAxesAt( POSITION.TOP ) )
      {

        if( axis.isVisible() )
        {
          ChartUtilities.resetGC( buffGc );
          final IAxisRenderer renderer = axis.getRenderer();
          final int y = renderer.getAxisWidth( axis );
          renderer.paint( buffGc, axis, new Rectangle( plotTopLeft.x, h, plotWidthHight.x, y ) );
          h = h + y;
        }
      }
      int w = 0;
      for( final IAxis axis : model.getMapperRegistry().getAxesAt( POSITION.LEFT ) )
      {

        if( axis.isVisible() )
        {
          ChartUtilities.resetGC( buffGc );
          final IAxisRenderer renderer = axis.getRenderer();
          final int x = renderer.getAxisWidth( axis );
          renderer.paint( buffGc, axis, new Rectangle( 0, plotTopLeft.y, x, plotWidthHight.y ) );
          w = w + x;
        }
      }
      int b = 0;
      for( final IAxis axis : model.getMapperRegistry().getAxesAt( POSITION.BOTTOM ) )
      {

        if( axis.isVisible() )
        {
          ChartUtilities.resetGC( buffGc );
          final IAxisRenderer renderer = axis.getRenderer();
          final int x = renderer.getAxisWidth( axis );
          renderer.paint( buffGc, axis, new Rectangle( plotTopLeft.x, plotTopLeft.y + plotWidthHight.y + b, plotWidthHight.x, x ) );
          b = b + x;
        }
      }
      int r = 0;
      for( final IAxis axis : model.getMapperRegistry().getAxesAt( POSITION.RIGHT ) )
      {

        if( axis.isVisible() )
        {
          ChartUtilities.resetGC( buffGc );
          final IAxisRenderer renderer = axis.getRenderer();
          final int x = renderer.getAxisWidth( axis );
          renderer.paint( buffGc, axis, new Rectangle( plotTopLeft.x + plotWidthHight.x + r, plotTopLeft.y, x, plotWidthHight.y ) );
          r = r + x;
        }
      }
     buffGc.setClipping( new Rectangle( plotTopLeft.x, plotTopLeft.y, plotWidthHight.x+plotTopLeft.x, plotWidthHight.y+plotTopLeft.y ) );
      for( final IChartLayer layer : model.getLayerManager().getLayers() )
      {
        if( layer.isVisible() )
        {
          ChartUtilities.resetGC( buffGc );
          layer.paint( buffGc );
        }
      }
    }
    finally
    {
      buffGc.dispose();
    }
    return bufferImage.getImageData();
  }

//
// // img mit weiï¿½em Hintergrund versehen
// gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
// gcw.fillRectangle( 0, 0, width, height );
//
// // Plot zeichnen
// final PlotCanvas plotCanvas = new PlotCanvas();
// Image tmpImg = plotCanvas.createImage( chart.getChartModel().getLayerManager().getLayers(), img.getBounds() );
// gcw.drawImage( tmpImg, 0, 0, plotCanvas.getBounds().width, plotCanvas.getBounds().height, plotCanvas.getBounds().x,
// plotCanvas.getBounds().y, plotCanvas.getBounds().width, plotCanvas.getBounds().height );
// tmpImg.dispose();
//
// final IMapperRegistry ar = chart.getChartModel().getMapperRegistry();
// // final Map<IAxis, IAxisComponent> components = ar.getAxesToComponentsMap();
// // final Set<Entry<IAxis, IAxisComponent>> acs = components.entrySet();
// // for( final Entry<IAxis, IAxisComponent> ac : acs )
// // {
// for( final IAxis axis : ar.getAxes() )
// {
// final IAxisRenderer rend = axis.getRenderer();
// if( rend == null )
// continue;
// // final IAxis axis = ac.getKey();
// // Casten, damit man auf die Component-Eigenschaften zugreifen kann
// final AxisCanvas comp = chart.getAxisCanvas( axis );// (AxisCanvas) ac.getValue();
// // final IAxis axis = ac.getKey();
// // // Casten, damit man auf die Component-Eigenschaften zugreifen kann
// // final AxisCanvas comp = (AxisCanvas) ac.getValue();
//
// // Wenn man den GC nicht neu erzeugt, werden die AChsen nicht
// // gezeichnet, sondern nochmal das Chart
// // Zusätzlich muss das Image disposed werden, weil sonst noch
// // Teile des Plot in die AxisSpaces gezeichnet werden
//
// tmpImg = new Image( dev, width, height );
// final GC tmpGc = new GC( tmpImg );
// // den Renderer in den TmpGC-Zeichnen lassen
// rend.paint( tmpGc, axis, comp.getBounds() );
// /*
// * ...und ins Endbild kopieren; dabei muss berï¿½cksichtigt werden, dass die Components in 2 Ebenen Tiefe liegen -
// * zur Position muss noch die der Eltern addiert werden
// */
// gcw.drawImage( tmpImg, comp.getBounds().x, comp.getBounds().y, comp.getBounds().width, comp.getBounds().height,
// comp.getParent().getBounds().x + comp.getBounds().x, comp.getParent().getBounds().y
// + comp.getBounds().y, comp.getBounds().width, comp.getBounds().height );
//
// tmpGc.dispose();
// tmpImg.dispose();
// }
// gcw.dispose();
// final ImageData id = img.getImageData();
//
// img.dispose();
//
// return id;
//
// }

  /**
   * @param transparency
   *          RGB to which the transparency-color should be set; transparency will not be used if RGB is null
   */
  public static ImageData createChartImage( final ChartComposite chart, final Device dev, final int width, final int height )
  {
    chart.setSize( new Point( width, height ) );
    // chart.redraw();
    chart.invalidatePlotCanvas( null );

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
      final GC tmpGc = new GC( tmpImg );
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
