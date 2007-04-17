/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.logging.Logger;

/**
 * @author burtscher
 */
public class DefaultLegendItem implements ILegendItem
{
  private IChartLayer m_layer;

  private int m_inset = 5;

  private int m_iconHeight;

  private int m_iconWidth;

  private FontData m_fd = new FontData( "Arial", 10, SWT.NONE );

  public DefaultLegendItem( IChartLayer l, int iconWidth, int iconHeight, int inset )
  {
    m_inset = inset;
    m_iconHeight = iconHeight;
    m_iconWidth = iconWidth;
    m_layer = l;
  }

  /**
   * calculates the extent of text using certain fontdata TODO: should be extracted into a general helper Method
   */
  private Point getTextExtent( GCWrapper gc, Device dev, String label, FontData fd )
  {
    Font f = new Font( dev, fd );
    gc.setFont( f );
    Point point = gc.textExtent( label );
    gc.setFont( dev.getSystemFont() );
    f.dispose();
    return point;
  }

  /**
   * @see org.kalypso.swtchart.chart.legend.ILegendItem#getName()
   */
  public String getName( )
  {
    String lname = m_layer.getDescription();
    if( lname != null && ! lname.trim().equals( "" ))
      return lname;
    else return m_layer.getName();
  }

  /**
   * @see org.kalypso.swtchart.chart.legend.ILegendItem#getDescription()
   */
  public String getDescription( )
  {
    String desc = m_layer.getDescription();
    if( desc != null )
      return desc;
    return "no Description";
  }

  /**
   * @see org.kalypso.swtchart.chart.legend.ILegendItem#drawIcon(org.eclipse.swt.graphics.Image)
   */
  public void drawIcon( Image img )
  {
    m_layer.drawIcon( img, m_iconWidth, m_iconHeight );
  }

  /**
   * @see org.kalypso.swtchart.chart.legend.ILegendItem#calcSize(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper)
   */
  public Point computeSize( int whint, int hhint )
  {
    GC gc = new GC( Display.getDefault() );
    GCWrapper gcw = new GCWrapper( gc );

    Point nameWidth = getTextExtent( gcw, Display.getCurrent(), getName(), m_fd );

    int width = m_inset + m_iconWidth + m_inset + nameWidth.x + m_inset;
    int height = m_inset + m_iconHeight + m_inset;
    // Logger.trace("LegendTextWidth (computeSize): "+nameWidth+" LegendText: "+getName());

    gcw.dispose();
    gc.dispose();

    return new Point( width, height );
  }

  /**
   * @see org.kalypso.swtchart.chart.legend.ILegendItem#getSize()
   */
  public Point getSize( )
  {
    return computeSize( 0, 0 );
  }

  /**
   * @see org.kalypso.swtchart.chart.legend.ILegendItem#paintImage(org.eclipse.swt.graphics.Image)
   */
  public void paintImage( Image img )
  {
    GC gc = new GC( img );
    Point p0 = new Point( m_inset, m_inset );
    GCWrapper gcw = new GCWrapper( gc );

    String name=getName();
    
    Point nameBounds = getTextExtent( gcw, Display.getCurrent(), name, m_fd );

    gc.setBackground( Display.getDefault().getSystemColor( (int) (Math.random() * 20.0) ) );

    // Icon holen
    Image iconImage = new Image( Display.getDefault(), m_iconWidth, m_iconHeight );
    m_layer.drawIcon( iconImage, m_iconWidth, m_iconHeight );
    // Icon kopieren
    gc.drawImage( iconImage, 0, 0, m_iconWidth, m_iconHeight, p0.x, p0.y, m_iconWidth, m_iconHeight );
    // TestImage zerstören
    iconImage.dispose();

    gc.setForeground( Display.getDefault().getSystemColor( SWT.COLOR_GRAY ) );
    gc.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_WHITE ) );
    gc.drawRectangle( p0.x, p0.y, m_iconWidth, m_iconHeight );

    // Jetzt den Layernamen
    gc.setForeground( Display.getDefault().getSystemColor( SWT.COLOR_BLACK ) );

    // an Mitte von icon anpassen
    // int width=getName().length()*fm.getAverageCharWidth();
    int width = nameBounds.x;
    Logger.trace( "LegendTextWidth (paint) : " + width + " LegendText: " + getName() );

    int fontheight = nameBounds.y;
    int fontTop = (m_iconHeight - fontheight) / 2;

    int textX = p0.x + m_iconWidth + m_inset;
    int textY = p0.y + fontTop;
    // gc.drawString( getName(), textX , textY );

    drawText( gcw, gc.getDevice(), name, textX, textY, m_fd );

    gcw.dispose();
    gc.dispose();
  }

  /**
   * draws a given text at the position using special fontData
   */
  private void drawText( GCWrapper gc, Device dev, String text, int x, int y, FontData fd )
  {
    Font f = new Font( dev, fd );
    gc.m_gc.setTextAntialias( SWT.ON );
    gc.setFont( f );
    gc.drawText( text, x, y );
    gc.setFont( dev.getSystemFont() );
    f.dispose();
  }
}
