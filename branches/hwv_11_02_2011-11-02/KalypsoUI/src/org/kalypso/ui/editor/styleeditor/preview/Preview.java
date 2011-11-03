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
package org.kalypso.ui.editor.styleeditor.preview;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.swt.awt.ImageConverter;
import org.kalypso.i18n.Messages;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;

/**
 * @author Gernot Belger
 */
public abstract class Preview<DATA> extends Canvas
{
  private final Point m_size;

  private final IStyleInput<DATA> m_input;

  private final GM_Object m_geom;

  private boolean m_showDemoText = true;

  public Preview( final Composite parent, final Point size, final IStyleInput<DATA> input )
  {
    super( parent, SWT.NONE );

    m_size = size;
    m_input = input;
    m_geom = createGeometry();

    addPaintListener( new PaintListener()
    {
      @Override
      public void paintControl( final PaintEvent e )
      {
        handlePaint( e );
      }
    } );
  }

  private GM_Object createGeometry( )
  {
    try
    {
      return doCreateGeometry();
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
   */
  @Override
  public Point computeSize( final int wHint, final int hHint, final boolean changed )
  {
    final Point size = super.computeSize( wHint, hHint, changed );
    final int x = m_size.x == SWT.DEFAULT ? size.x : m_size.x;
    final int y = m_size.y == SWT.DEFAULT ? size.y : m_size.y;
    return new Point( x, y );
  }

  protected void setShowDemoText( final boolean showText )
  {
    m_showDemoText = showText;
  }

  protected final DATA getInputData( )
  {
    return m_input.getData();
  }

  protected void handlePaint( final PaintEvent e )
  {
    final Rectangle clientArea = getClientArea();
    final int width = clientArea.width;
    final int height = clientArea.height;

    final GC gc = e.gc;

    paintDemoText( width, height, e.gc );

    final Image image = createImage( width, height );
    if( image != null )
    {
      gc.drawImage( image, 0, 0 );
      image.dispose();
    }
  }

  private void paintDemoText( final int width, final int height, final GC gc )
  {
    if( !m_showDemoText )
      return;

    final String title = Messages.getString( "org.kalypso.ui.editor.sldEditor.FillEditorComposite.20" ); //$NON-NLS-1$

    final Font oldFont = gc.getFont();
    final FontData fd = new FontData( "SansSerif", (int) Math.ceil( height / 2 ), SWT.BOLD );
    final Font font = new Font( getDisplay(), fd );
    gc.setFont( font );

    final Point extent = gc.stringExtent( title );

    /* Center text */
    final int x = (int) Math.ceil( width / 2.0 - extent.x / 2.0 );
    final int y = (int) Math.ceil( (height / 2.0 - extent.y / 2.0) );
    gc.drawString( title, x, y );

    gc.setFont( oldFont );

    font.dispose();
  }

  private Image createImage( final int width, final int height )
  {
    if( width == 0 || height == 0 )
      return null;

    final BufferedImage bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );

    final Graphics2D g2D = bufferedImage.createGraphics();
    g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    g2D.setPaintMode();
    g2D.setClip( 0, 0, width, height );

    doPaintData( g2D, width, height, m_geom );

    final ImageData convertToSWT = ImageConverter.convertToSWT( bufferedImage );
    return new Image( getDisplay(), convertToSWT );
  }

  /**
   * Call, if style has changed.
   */
  public void updateControl( )
  {
    redraw();
  }

  protected abstract void doPaintData( Graphics2D g2d, int width, int height, GM_Object geom );

  protected abstract GM_Object doCreateGeometry( ) throws GM_Exception;
}
