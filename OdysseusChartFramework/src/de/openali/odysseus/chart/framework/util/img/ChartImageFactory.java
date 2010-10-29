package de.openali.odysseus.chart.framework.util.img;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.LABEL_POSITION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author burtscher Creates an Image from a chart widget. The objects has to be disposed when it's no longer needed
 */
public final class ChartImageFactory
{
  private ChartImageFactory( )
  {
  }

  public enum IMAGE_TYPE
  {
    PNG,
    JPG,
    GIF
  }

  public static void paintEditInfo( final GC gcw, final EditInfo editInfo )
  {
    ChartUtilities.resetGC( gcw );
    if( editInfo == null )
      return;

    // draw hover shape
    if( editInfo.m_hoverFigure != null )
      editInfo.m_hoverFigure.paint( gcw );
    // draw edit shape
    if( editInfo.m_editFigure != null )
      editInfo.m_editFigure.paint( gcw );

    // draw tooltip
    ChartUtilities.resetGC( gcw );

    final Rectangle screen = gcw.getClipping();

    String tooltiptext = editInfo.m_text;
    final Point mousePos = editInfo.m_pos;
    if( (tooltiptext != null) && (mousePos != null) )
    {
      tooltiptext = tooltiptext.replace( '\r', ' ' );

      final int TOOLINSET = 3;

      final Font oldFont = gcw.getFont();

      final Font bannerFont = JFaceResources.getTextFont();
      gcw.setFont( bannerFont );

      gcw.setBackground( PlatformUI.getWorkbench().getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
      gcw.setForeground( PlatformUI.getWorkbench().getDisplay().getSystemColor( SWT.COLOR_INFO_FOREGROUND ) );
      final Point toolsize = gcw.textExtent( tooltiptext );

      /*
       * Positionieren der Tooltip-Box: der ideale Platz liegt rechts unter dem Mauszeiger. Wenn rechts nicht genï¿½gend
       * Platz ist, dann wird er nach links verschoben. Der Startpunkt soll dabei immer im sichtbaren Bereich liegen.
       */
      int toolx = mousePos.x + 3 + TOOLINSET;
      if( toolx + toolsize.x > screen.width )
      {
        toolx = screen.width - 5 - toolsize.x;
        if( toolx < 5 )
          toolx = 5;
      }

      int tooly = mousePos.y + 3 + TOOLINSET + 20;
      if( (tooly + toolsize.y > screen.height) && ((mousePos.y - 3 - TOOLINSET - toolsize.y - 20) > 0) )
        tooly = mousePos.y - 3 - TOOLINSET - toolsize.y - 20;

      gcw.setLineWidth( 1 );
      final Rectangle toolrect = new Rectangle( toolx - TOOLINSET, tooly - TOOLINSET, toolsize.x + TOOLINSET * 2, toolsize.y + TOOLINSET * 2 );
      gcw.fillRectangle( toolrect );
      gcw.drawRectangle( toolrect );

      gcw.drawText( tooltiptext, toolx, tooly, true );

      gcw.setFont( oldFont );
    }
  }

  public static Image createAxesImage( final IMapperRegistry mapperRegistry, final Rectangle boundsRect )
  {
    final Rectangle plotRect = calculatePlotSize( mapperRegistry, boundsRect.width, boundsRect.height );
    setAxesHeight( mapperRegistry.getAxes(), plotRect );
    return createAxesImage( null, mapperRegistry, boundsRect, plotRect );
  }

  public static Image createAxesImage( final IMapperRegistry mapperRegistry, final Rectangle boundsRect, final Rectangle plotRect )
  {
    return createAxesImage( null, mapperRegistry, boundsRect, plotRect );
  }

  public static void drawHorizontalAxis( final GC gc, final IAxis[] axes, final int width )
  {
    int h = 0;
    for( final IAxis axis : axes )
    {
      ChartUtilities.resetGC( gc );
      final IAxisRenderer renderer = axis.getRenderer();
      final int y = renderer.getAxisWidth( axis );
      renderer.paint( gc, axis, new Rectangle( 0, h, width, y ) );
      h = h + y;
    }
  }

  public static void drawVerticalAxis( final GC gc, final IAxis[] axes, final int height )
  {
    int h = 0;
    for( final IAxis axis : axes )
    {
      ChartUtilities.resetGC( gc );
      final IAxisRenderer renderer = axis.getRenderer();
      final int x = renderer.getAxisWidth( axis );
      renderer.paint( gc, axis, new Rectangle( h, 0, x, height ) );
      h = h + x;
    }
  }

  public static Image createAxesImage( final Map<POSITION, Image> imageMap, final IMapperRegistry mapperRegistry, final Rectangle boundsRect, final Rectangle plotRect )
  {
    if( plotRect.width == 0 || plotRect.height == 0 )
      return null;
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    int height = Math.max( 1, boundsRect.height - plotRect.y - plotRect.height );
    final Image bottomImage = new Image( dev, plotRect.width, height );
    final GC bottomGc = new GC( bottomImage );
    drawHorizontalAxis( bottomGc, mapperRegistry.getAxesAt( POSITION.BOTTOM ), plotRect.width );
    bottomGc.dispose();

    height = Math.max( 1, plotRect.y );
    final Image topImage = new Image( dev, plotRect.width, height );
    final GC topGc = new GC( topImage );
    drawHorizontalAxis( topGc, mapperRegistry.getAxesAt( POSITION.TOP ), plotRect.width );
    topGc.dispose();

    height = Math.max( 1, plotRect.x );
    final Image leftImage = new Image( dev, height, plotRect.height );
    final GC leftGc = new GC( leftImage );
    drawVerticalAxis( leftGc, mapperRegistry.getAxesAt( POSITION.LEFT ), plotRect.height );
    leftGc.dispose();

    height = Math.max( 1, boundsRect.width - plotRect.width - plotRect.x );
    final Image rightImage = new Image( dev, height, plotRect.height );
    final GC rightGc = new GC( rightImage );
    drawVerticalAxis( rightGc, mapperRegistry.getAxesAt( POSITION.RIGHT ), plotRect.height );
    rightGc.dispose();

    final Image image = new Image( dev, boundsRect.width, boundsRect.height );
    final GC tmpGc = new GC( image );
// drawHorizontalAxis( tmpGc, mapperRegistry.getAxesAt( POSITION.TOP ), plotRect.width );
// drawHorizontalAxis( tmpGc, mapperRegistry.getAxesAt( POSITION.BOTTOM ), plotRect.width );
// drawVerticalAxis( tmpGc, mapperRegistry.getAxesAt( POSITION.LEFT ), plotRect.height );
// tmpGc.drawImage( topImage, plotRect.x, 0 );
// topImage.dispose();
    tmpGc.drawImage( bottomImage, plotRect.x, plotRect.y + plotRect.height );
    bottomImage.dispose();
    tmpGc.drawImage( topImage, plotRect.x, 0 );
    topImage.dispose();
    tmpGc.drawImage( leftImage, 0, plotRect.y );
    leftImage.dispose();
    tmpGc.drawImage( rightImage, plotRect.x + plotRect.width, plotRect.y );
    rightImage.dispose();

// topGc.dispose();
    tmpGc.dispose();
    return image;

// else
// {
// final Image axisImage = imageMap.get( axis );
// if( imageMap.get( layer ) == null )
// imageMap.put( layer, createLayerImage( tmpGc, layer ) );
// tmpGc.drawImage( layerImage, 0, 0 );
// }

  }

  private static int getAxesWidth( final IAxis[] axes )
  {
    int w = 0;
    for( final IAxis axis : axes )
    {
      if( axis.isVisible() )
      {
        final IAxisRenderer renderer = axis.getRenderer();
        w = w + renderer.getAxisWidth( axis );
      }
    }
    return w;
  }

  public static void setAxesHeight( final IAxis[] axes, final Rectangle plotRect )
  {
    for( final IAxis axis : axes )
    {
      if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        axis.setScreenHeight( plotRect.width );
      else
        axis.setScreenHeight( plotRect.height );
    }
  }

  public static Rectangle calculatePlotSize( final IMapperRegistry mapperRegistry, final int width, final int height )
  {

    if( mapperRegistry == null )
      return new Rectangle( 0, 0, 0, 0 );

    final int top = getAxesWidth( mapperRegistry.getAxesAt( POSITION.TOP ) );
    final int left = getAxesWidth( mapperRegistry.getAxesAt( POSITION.LEFT ) );
    final int bottom = height - getAxesWidth( mapperRegistry.getAxesAt( POSITION.BOTTOM ) );
    final int right = width - getAxesWidth( mapperRegistry.getAxesAt( POSITION.RIGHT ) );

    return new Rectangle( left, top, right - left, bottom - top );
  }

  public static ImageData createChartImage( final IChartModel model, final Point size )
  {
    final Point titleSize = model.isHideTitle() ? new Point( 0, 0 ) : ChartImageFactory.calculateTitleSize( model.getTitle(), model.getTextStyle().toFontData() );

    final Rectangle plotRect = calculatePlotSize( model.getMapperRegistry(), size.x, size.y - titleSize.y );
    plotRect.y += titleSize.y;

    setAxesHeight( model.getMapperRegistry().getAxes(), plotRect );
    final Image axesImage = createAxesImage( model.getMapperRegistry(), new Rectangle( 0, 0, size.x, size.x - plotRect.y ) );
    final Image plotImage = createPlotImage( model.getLayerManager().getLayers(), plotRect );
    final Image titleImage = model.isHideTitle() ? null : ChartImageFactory.createTitleImage( model.getTitle(), model.getTextStyle().toFontData(), titleSize );

    final Device dev = PlatformUI.getWorkbench().getDisplay();

    final Image image = new Image( dev, size.x, size.y );

    final GC tmpGc = new GC( image );
    try
    {
      tmpGc.drawImage( axesImage, 0, 0 );
      if( titleImage != null )
        tmpGc.drawImage( titleImage, 0, 0 );

      tmpGc.drawImage( plotImage, plotRect.x, plotRect.y );
      return image.getImageData();
    }
    finally
    {
      tmpGc.dispose();
      image.dispose();
      axesImage.dispose();
      plotImage.dispose();
    }

  }

  public static Image createLayerImage( final GC gc, final IChartLayer layer )
  {
    // get width from Axes
    final ICoordinateMapper mapper = layer.getCoordinateMapper();
    if( mapper == null )
    {
      System.out.println( "no axismapper found for layer :" + layer.getTitle() );
      return new Image( gc.getDevice(), 1, 1 );
    }
    final int width = mapper == null ? 1 : mapper.getDomainAxis().getScreenHeight();
    final int height = mapper == null ? 1 : mapper.getTargetAxis().getScreenHeight();
    // prepare image
    final ImageData id = new ImageData( width, height, 32, new PaletteData( 0xFF, 0xFF00, 0xFF0000 ) );
    id.transparentPixel = 0xfffffe;
    final Image image = new Image( gc.getDevice(), id );
    final GC tmpGC = new GC( image );
    // Hintergrund explizit malen - der wird später transparent gezeichnet
    final Color transparentColor = OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), new RGB( 0xfe, 0xff, 0xff ) );
    tmpGC.setBackground( transparentColor );
    tmpGC.fillRectangle( new Rectangle( 0, 0, width, height ) );

    try
    {
      ChartUtilities.resetGC( tmpGC );
      layer.paint( tmpGC );
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

  public static Point calculateTitleSize( final String title, final FontData titleFont )
  {
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, 1, 1 );
    final GC tmpGc = new GC( image );
    final Font tmpFont = new Font( dev, titleFont == null ? dev.getFontList( null, true )[0] : titleFont );
    try
    {
      tmpGc.setFont( tmpFont );
      final Point size = tmpGc.textExtent( title, SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
      return size;
    }
    finally
    {
      image.dispose();
      tmpGc.dispose();
      tmpFont.dispose();
    }

  }

  public static Image createTitleImage( final String title, final FontData titleFont, final Point size )
  {
    return createTitleImage( title, titleFont, size, LABEL_POSITION.CENTERED );
  }

  public static Image createTitleImage( final String title, final FontData titleFont, final Point size, final LABEL_POSITION position )
  {
    final String[] lines = StringUtils.split( title, "\n" );
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, size.x, size.y );
    final GC tmpGc = new GC( image );
    final Font tmpFont = new Font( dev, titleFont == null ? dev.getFontList( null, true )[0] : titleFont );

    try
    {
      tmpGc.setFont( tmpFont );
      for( int i = 0; i < lines.length; i++ )
      {
        final Point lineSize = tmpGc.textExtent( lines[i] );
        tmpGc.drawText( lines[i], i * lineSize.y, (size.x - lineSize.x) / 2, SWT.DRAW_TAB );
      }
      return image;
    }
    finally
    {
      tmpFont.dispose();
      tmpGc.dispose();
    }

  }

  public static Image createPlotImage( final IChartLayer[] layers, final Rectangle plotSize )
  {
    return createPlotImage( null, layers, plotSize );
  }

  public static Image createPlotImage( final Map<IChartLayer, Image> imageMap, final IChartLayer[] layers, final Rectangle plotSize )
  {
    if( plotSize.width == 0 || plotSize.height == 0 )
      return null;
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, plotSize.width, plotSize.height );
    final GC tmpGc = new GC( image );
    try
    {
      for( final IChartLayer layer : layers )
      {
        if( imageMap == null )
        {
          layer.paint( tmpGc );
        }
        else
        {
          final Image layerImage = imageMap.get( layer );
          if( imageMap.get( layer ) == null )
            imageMap.put( layer, createLayerImage( tmpGc, layer ) );
          tmpGc.drawImage( layerImage, 0, 0 );
        }
      }
    }

    finally
    {
      tmpGc.dispose();
    }
    return image;
  }

}
