/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.swtchart.chart.legend;

import java.util.ArrayList;
import java.util.List;

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
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.layer.IChartLayer;

/**
 * The legend can be rendered in the chart at following positions:
 * <ul>
 * <li>right</li>
 * <li>left</li>
 * </ul>
 * 
 * @author schlienger
 */
public class Legend extends Canvas implements PaintListener
{
  private String m_title;

  private ArrayList<ILegendItem> m_items;

  private int m_iconWidth = 20;

  private int m_iconHeight = 20;

  private int m_inset = 5;

  private Image m_bufferImg;

  public Legend( Composite parent, int style )
  {
    super( parent, style );
    addPaintListener( this );
    m_items = new ArrayList<ILegendItem>();
    setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
  }

  public void setTitle( final String title )
  {
    m_title = title;
  }

  public void addLayer( IChartLayer l )
  {
    DefaultLegendItem li = new DefaultLegendItem( l, m_iconWidth, m_iconHeight, m_inset );
    addLegendItem( li );
  }

  public void addLayers( IChartLayer[] layers )
  {
    for( IChartLayer l : layers )
    {
      addLayer( l );
    }
  }

  public void addLayers( List<IChartLayer> layers )
  {
    for( IChartLayer l : layers )
    {
      addLayer( l );
    }
  }

  public void addLegendItem( ILegendItem l )
  {
    m_items.add( l );
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  /*
   * public void paintControl( PaintEvent e ) { GC gc=e.gc; gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
   * gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK)); //Abstand zum oberen Rand der Komponente int top=0;
   * for( ILegendItem li : m_items ) { Point size=li.computeSize(0,0); Image liImg=new Image(e.display, size.x, size.y);
   * li.paintImage(liImg); gc.drawImage(liImg, 0, top); liImg.dispose(); top+=size.y; } gc.dispose(); }
   */

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent) Zeichnet nacheinander
   *      alle Layer; falls eine MouseDrag-Aktion ausgef¸hrt wird, werden die Werte nicht neu berechnet, sondern ein
   *      gepuffertes Img angezeigt
   */
  public void paintControl( final PaintEvent e )
  {
    final GCWrapper gcw = new GCWrapper( e.gc );
    final Rectangle screenArea = getClientArea();
    m_bufferImg = paintLegend( e.display, gcw, screenArea, m_bufferImg );
    gcw.dispose();

  }

  /**
   * ‹Bernommen und angepasst von de.belger.swtchart zeichnet zuerst ein Image vom Chart, das dann in dem GC vom Plot
   * gezeichnet (doubleBuffering) und zur¸ckgegeben wird; es wird also nur erzeugt, wenn explizit redraw() aufgerufen
   * wird Die Funktion ist public, da sie auch f¸r die Generierungen von Grafiken f¸r die R¸ckgabe ¸ber den Webservice
   * verwendet wird.
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

    // muss so sein, wenn mann den layerClip immer setzt, kommts beim dragRect zeichnen zu
    // selstsamen effekten
    gcw.drawImage( usedBufferImage, 0, 0 );
    return usedBufferImage;
  }

  /**
   * Zeichnet die Legende in eine Image
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

      for( ILegendItem li : m_items )
      {
        Point size = li.computeSize( 0, 0 );
        Image liImg = new Image( dev, size.x, size.y );
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
    for( ILegendItem li : m_items )
    {
      Point lisize = li.computeSize( 0, 0 );
      if( lisize.x > width )
        width = lisize.x;
      height += lisize.y;
    }
    int tolerance = 5;
    return new Point( width, height + tolerance );
  }

}
