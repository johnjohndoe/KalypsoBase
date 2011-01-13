package de.openali.odysseus.chart.framework.view.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.util.img.ChartTooltipPainter;

/**
 * @author kimwerner
 */
public class GenericLayerRenderer
{
  private EditInfo m_editInfo;

  private EditInfo m_tooltipInfo;

  private final Map<IChartLayer, Point> m_layerPanOffsets = new HashMap<IChartLayer, Point>();

  private Rectangle m_dragArea = null;

  private final ChartTooltipPainter m_tooltipPainter = new ChartTooltipPainter();

  public Image createLayerImage( final GC gc, final IChartLayer layer )
  {
    final ImageData id = new ImageData( gc.getClipping().width, gc.getClipping().height, 32, new PaletteData( 0xFF, 0xFF00, 0xFF0000 ) );

    id.transparentPixel = 0xfffffe;

    final Image image = new Image( gc.getDevice(), id );
    final GC tmpGC = new GC( image );

    // Hintergrund explizit malen - der wird später transparent gezeichnet
    final Color transparentColor = OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), new RGB( 0xfe, 0xff, 0xff ) );
    tmpGC.setBackground( transparentColor );
    tmpGC.fillRectangle( gc.getClipping() );

    try
    {
      ChartUtilities.resetGC( tmpGC );
      final long start = System.currentTimeMillis();
      layer.paint( tmpGC );
      final long end = System.currentTimeMillis();
      Logger.logInfo( Logger.TOPIC_LOG_PLOT, "Time to paint layer (" + layer.getTitle() + "): " + (end - start) + " ms" );
    }
    catch( final Exception e )
    {
      // catch all in order to protect from bad code in layer implementations
      e.printStackTrace();
    }
    finally
    {
      tmpGC.dispose();
    }

    return image;

  }

  /**
   * @param bufferLayers
   *          if set to true, each layer is buffered on an individual image; set this to true if you plan to offer
   *          panning of single layers in the chart front end
   */

  public EditInfo getTooltipInfo( )
  {
    return m_editInfo;
  }

  public void paintDragArea( final GC gcw )
  {
    // Wenn ein DragRectangle da ist, dann muss nur das gezeichnet werden
    if( m_dragArea != null )
    {
      gcw.setLineWidth( 1 );
      gcw.setForeground( gcw.getDevice().getSystemColor( SWT.COLOR_BLACK ) );

      gcw.setBackground( gcw.getDevice().getSystemColor( SWT.COLOR_BLUE ) );
      final Rectangle r = RectangleUtils.createNormalizedRectangle( m_dragArea );// new Rectangle( x, y, w, h ) );

      // TODO: SWT-Bug mit drawFocus (wird nicht immer gezeichnet),
      // irgendwann mal wieder �berpr�fen
      gcw.setAlpha( 50 );
      gcw.fillRectangle( r.x, r.y, r.width, r.height );
      gcw.setAlpha( 255 );
      gcw.setLineStyle( SWT.LINE_DASH );
      gcw.drawRectangle( r.x, r.y, r.width, r.height );
    }
  }

  public void paintEditInfo( final GC gcw )
  {

    if( m_editInfo != null )
    {
      ChartUtilities.resetGC( gcw );
      // draw hover shape
      if( m_editInfo.m_hoverFigure != null )
        m_editInfo.m_hoverFigure.paint( gcw );
      // draw edit shape
      if( m_editInfo.m_editFigure != null )
        m_editInfo.m_editFigure.paint( gcw );
    }
    if( m_tooltipInfo != null )
    {
      ChartUtilities.resetGC( gcw );
      // draw tooltip
// final Rectangle screen = gcw.getClipping();

      // String tooltiptext = m_editInfo.m_text;
      final Point mousePos = m_tooltipInfo.m_pos;
      if( (m_tooltipInfo.m_text != null) && (mousePos != null) )
      {
        // tooltiptext = tooltiptext.replace( '\r', ' ' );
        m_tooltipPainter.setTooltip( m_tooltipInfo.m_text.replace( '\r', ' ' ) );
        m_tooltipPainter.paint( gcw, mousePos );

// final int TOOLINSET = 3;
//
// final Font oldFont = gcw.getFont();
// final Font bannerFont = JFaceResources.getTextFont();
// gcw.setFont( bannerFont );
// gcw.setBackground( PlatformUI.getWorkbench().getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
// gcw.setForeground( PlatformUI.getWorkbench().getDisplay().getSystemColor( SWT.COLOR_INFO_FOREGROUND ) );
// final Point toolsize = gcw.textExtent( tooltiptext );
//
// /*
// * Positionieren der Tooltip-Box: der ideale Platz liegt rechts unter dem Mauszeiger. Wenn rechts nicht genï¿½gend
// * Platz ist, dann wird er nach links verschoben. Der Startpunkt soll dabei immer im sichtbaren Bereich liegen.
// */
// int toolx = mousePos.x + 3 + TOOLINSET;
// if( toolx + toolsize.x > screen.width )
// {
// toolx = screen.width - 5 - toolsize.x;
// if( toolx < 5 )
// toolx = 5;
// }
//
// int tooly = mousePos.y + 3 + TOOLINSET + 20;
// if( (tooly + toolsize.y > screen.height) && ((mousePos.y - 3 - TOOLINSET - toolsize.y - 20) > 0) )
// tooly = mousePos.y - 3 - TOOLINSET - toolsize.y - 20;
//
// gcw.setLineWidth( 1 );
// final Rectangle toolrect = new Rectangle( toolx - TOOLINSET, tooly - TOOLINSET, toolsize.x + TOOLINSET * 2,
// toolsize.y + TOOLINSET * 2 );
// gcw.fillRectangle( toolrect );
// gcw.drawRectangle( toolrect );
//
// gcw.drawText( tooltiptext, toolx, tooly, true );
//
// gcw.setFont( oldFont );
      }
    }
  }

  public void paintPlot( final Map<IChartLayer, Image> layerImageMap, final GC gc, final IChartLayer[] layers )
  {
    if( layers == null )
      return;

    for( final IChartLayer layer : layers )
    {
      if( layer.isVisible() )
      {
        if( !layerImageMap.containsKey( layer ) )
          layerImageMap.put( layer, createLayerImage( gc, layer ) );

        final Image image = layerImageMap.get( layer );
        final Point point = m_layerPanOffsets.get( layer );
        if( point != null )
          gc.drawImage( image, -point.x, -point.y );
        else
          gc.drawImage( image, 0, 0 );
      }
    }
  }

  public void setDragArea( final Rectangle dragArea )
  {
    m_dragArea = dragArea;
  }

  /**
   * sets an offset to which the paint buffer is moved in case of a pan action;
   * 
   * @param layers
   *          if null, all layers are moved; else, only mentioned layers are moved
   * @param offset
   *          positive value moves buffer to right / down, negative value to left / up
   */
  public void setPanOffset( final IChartLayer[] layers, final Point offset )
  {
    for( final IChartLayer iChartLayer : layers )
      m_layerPanOffsets.put( iChartLayer, offset );
  }

  public void setTooltipInfo( final EditInfo hoverInfo )
  {
    m_tooltipInfo = hoverInfo;
  }

  public void setEditInfo( final EditInfo hoverInfo )
  {
    m_editInfo = hoverInfo;
  }

}
