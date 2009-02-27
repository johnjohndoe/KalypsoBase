package org.kalypso.chart.framework.trash;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author burtscher widget displaying a legend describing a charts contents; also a container for ILegendItems
 */
public class Legend extends Canvas implements PaintListener
{

  @SuppressWarnings("unused")
  private String m_title = "";

  private final ArrayList<ILegendItem_old> m_items;

  private final int m_iconWidth = 20;

  private final int m_iconHeight = 20;

  private final int m_inset = 5;

  private Image m_bufferImg;

  public Legend( Composite parent, int style )
  {
    super( parent, style );
    addPaintListener( this );
    m_items = new ArrayList<ILegendItem_old>();
    setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
  }

  /**
   * sets the legends title TODO: the title is not rendered into the legend image - this should be done as there's no
   * need for a title otherwise
   */
  public void setTitle( final String title )
  {
    m_title = title;
  }

  /**
   * creates an ILegendItem for the given ChartLayer and adds it to the list of ILegendItems
   */
  public void addLayer( IChartLayer l )
  {
    final DefaultLegendItem li = new DefaultLegendItem( l, m_iconWidth, m_iconHeight, m_inset );
    addLegendItem( li );
  }

  /**
   * creates ILegendItems for the given ChartLayers and adds them to the list of ILegendItems
   */
  public void addLayers( IChartLayer[] layers )
  {
    for( final IChartLayer l : layers )
    {
      if( l.isVisible() )
        addLayer( l );
    }
  }

  /**
   * adds an ILegendItem to the list of ILengendItems
   */
  public void addLegendItem( ILegendItem_old l )
  {
    m_items.add( l );
  }

  /**
   * paints the legend into the PaintEvents GC; uses DoubleBuffering
   */
  public void paintControl( PaintEvent e )
  {
    final GCWrapper gcw = new GCWrapper( e.gc );
    final Rectangle screenArea = getClientArea();
    m_bufferImg = paintLegend( e.display, gcw, screenArea, m_bufferImg );
    gcw.dispose();

  }

  /**
   * paints the legend into a buffer image and uses the given Rectangle to define the size; if the given image is null,
   * a new image will be created and returned TODO: rename to paintBufferedLegend or similar to separate from drawImage
   */
  public Image paintLegend( Device dev, GCWrapper gcw, Rectangle screen, Image bufferImage )
  {
    final Image usedBufferImage;
    if( bufferImage == null )
    {
      usedBufferImage = new Image( dev, screen.width, screen.height );
      drawImage( usedBufferImage, dev );
    }
    else
      usedBufferImage = bufferImage;

    gcw.drawImage( usedBufferImage, 0, 0 );
    return usedBufferImage;
  }

  /**
   * paints the legend into an Image
   */
  public void drawImage( Image img, Device dev )
  {
    final GCWrapper buffGc = new GCWrapper( new GC( img ) );
    buffGc.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    buffGc.setForeground( dev.getSystemColor( SWT.COLOR_BLACK ) );
    try
    {
      // Abstand zum oberen Rand der Komponente
      int top = 0;

      for( final ILegendItem_old li : m_items )
      {
        final Point size = li.computeSize( 0, 0 );
        final Image liImg = new Image( dev, size.x, size.y );
        li.paintImage( liImg );
        buffGc.drawImage( liImg, 0, top );
        liImg.dispose();
        top += size.y;
      }

    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    finally
    {
      buffGc.dispose();
    }
  }

  @Override
  public Point computeSize( int whint, int hhint )
  {
    int width = 0;
    int height = 0;
    for( final ILegendItem_old li : m_items )
    {
      final Point lisize = li.computeSize( 0, 0 );
      Logger.trace( li.getTitle() + " hat Gr��e " + lisize );
      if( lisize.x > width )
        width = lisize.x;
      height += lisize.y;
      Logger.trace( "Current Legend Size width: " + width + " height: " + height );
    }
    final int tolerance = 5;
    final Point size = new Point( width, height + tolerance );
    Logger.trace( "Legend Size: " + size );
    return size;
  }

}
