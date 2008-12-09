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
package org.kalypso.contribs.eclipse.swt.canvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

/**
 * A canvas for drawing transparent images and text and the possibility to handle images as hyperlink
 * 
 * @author Dirk Kuch
 */
public class HyperCanvas extends Canvas
{
  protected List<Image> m_imagesUnderCursor = new ArrayList<Image>();

  protected final Map<Rectangle, Image> m_imageRectangle = new HashMap<Rectangle, Image>();

  protected final Map<Image, List<MouseListener>> m_mouseListeners = new HashMap<Image, List<MouseListener>>();

  protected final Map<Image, String> m_tooltips = new HashMap<Image, String>();

  protected final Map<Image, IHyperCanvasSizeHandler> m_imageMap = new HashMap<Image, IHyperCanvasSizeHandler>();

  protected final Map<Image, Image> m_hoverImages = new HashMap<Image, Image>();

  protected final List<HyperCanvasTextHandler> m_text = new ArrayList<HyperCanvasTextHandler>();

  private final MouseListener m_mouseListener;

  private final MouseMoveListener m_moveListener;

  public class HyperCanvasTextHandler
  {
    public final String m_label;

    public final Font m_font;

    public final Color m_color;

    public final IHyperCanvasSizeHandler m_handler;

    public HyperCanvasTextHandler( final String label, final Font font, final Color color, final IHyperCanvasSizeHandler handler )
    {
      m_label = label;
      m_font = font;
      m_color = color;
      m_handler = handler;
    }
  }

  public HyperCanvas( final Composite parent, final int style )
  {
    super( parent, style );

    this.addPaintListener( new PaintListener()
    {
      @Override
      public void paintControl( final PaintEvent e )
      {
        m_imageRectangle.clear();

        final Set<Entry<Image, IHyperCanvasSizeHandler>> set = m_imageMap.entrySet();
        for( final Entry<Image, IHyperCanvasSizeHandler> entry : set )
        {
          final IHyperCanvasSizeHandler handler = entry.getValue();
          final Image img = entry.getKey();

          if( m_imagesUnderCursor.contains( img ) )
          {

            final Image hoverImage = m_hoverImages.get( img );
            if( hoverImage != null )
            {
              e.gc.drawImage( hoverImage, handler.getX(), handler.getY() );
            }
            else
              e.gc.drawImage( img, handler.getX(), handler.getY() );
          }
          else
            e.gc.drawImage( img, handler.getX(), handler.getY() );

          final Rectangle imgBounds = img.getBounds();
          final Rectangle rectangle = new Rectangle( handler.getX(), handler.getY(), imgBounds.width, imgBounds.height );

          m_imageRectangle.put( rectangle, img );
        }

        for( final HyperCanvasTextHandler handler : m_text )
        {
          e.gc.setFont( handler.m_font );
          e.gc.setForeground( handler.m_color );
          e.gc.drawText( handler.m_label, handler.m_handler.getX(), handler.m_handler.getY(), true );
        }

      }
    } );

    m_mouseListener = new MouseListener()
    {
      @Override
      public void mouseDoubleClick( final MouseEvent e )
      {
        final List<Image> hits = determineImagesUnderCursor( e );
        for( final Image image : hits )
        {
          final List<MouseListener> list = m_mouseListeners.get( image );
          if( list != null )
          {
            for( final MouseListener listener : list )
            {
              listener.mouseDoubleClick( e );
            }
          }
        }
      }

      @Override
      public void mouseDown( final MouseEvent e )
      {
        final List<Image> hits = determineImagesUnderCursor( e );
        for( final Image image : hits )
        {
          final List<MouseListener> list = m_mouseListeners.get( image );
          if( list != null )
          {
            for( final MouseListener listener : list )
            {
              listener.mouseDown( e );
            }
          }
        }
      }

      @Override
      public void mouseUp( final MouseEvent e )
      {
        final List<Image> hits = determineImagesUnderCursor( e );
        for( final Image image : hits )
        {
          final List<MouseListener> list = m_mouseListeners.get( image );
          if( list != null )
          {
            for( final MouseListener listener : list )
            {
              listener.mouseUp( e );
            }
          }
        }
      }
    };

    this.addMouseListener( m_mouseListener );

    final Cursor cursorHand = new Cursor( this.getDisplay(), SWT.CURSOR_HAND );
    final Cursor cursorDefault = new Cursor( this.getDisplay(), SWT.CURSOR_ARROW );

    final Canvas myCanvas = this;

    m_moveListener = new MouseMoveListener()
    {

      @Override
      public void mouseMove( final MouseEvent e )
      {
        m_imagesUnderCursor = determineImagesUnderCursor( e );

        if( m_imagesUnderCursor.size() > 0 )
        {
          for( final Image image : m_imagesUnderCursor )
          {
            final List<MouseListener> list = m_mouseListeners.get( image );
            if( list != null && list.size() > 0 )
            {
              setCursor( cursorHand );
              final String tooltip = m_tooltips.get( image );

              if( tooltip != null )
              {
                if( !tooltip.equals( getToolTipText() ) )
                {
                  myCanvas.redraw();

                  new UIJob( "" )
                  {

                    @Override
                    public IStatus runInUIThread( final IProgressMonitor monitor )
                    {
                      setToolTipText( tooltip );
                      return Status.OK_STATUS;
                    }
                  }.schedule();

                }
              }

              return;
            }
          }

          setCursor( cursorDefault );
        }
        else
        {
          setCursor( cursorDefault );
          setToolTipText( null );
        }
      }

    };

    this.addMouseMoveListener( m_moveListener );
  }

  protected List<Image> determineImagesUnderCursor( final MouseEvent e )
  {
    final List<Image> hits = new ArrayList<Image>();

    final Set<Entry<Rectangle, Image>> entries = m_imageRectangle.entrySet();
    for( final Entry<Rectangle, Image> entry : entries )
    {
      final Rectangle rectangle = entry.getKey();
      if( rectangle.intersects( e.x, e.y, 1, 1 ) )
      {
        hits.add( entry.getValue() );
      }
    }

    return hits;
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    m_imageMap.clear();
    m_imageRectangle.clear();
    m_mouseListeners.clear();
    m_text.clear();
    m_tooltips.clear();

    this.removeMouseListener( m_mouseListener );
    this.removeMouseMoveListener( m_moveListener );
    super.dispose();
  }

  public void addImage( final Image image, final IHyperCanvasSizeHandler handler )
  {
    m_imageMap.put( image, handler );
  }

  public void addImage( final Image image, final Image hoverImage, final IHyperCanvasSizeHandler handler )
  {
    m_imageMap.put( image, handler );
    m_hoverImages.put( image, hoverImage );
  }

  public void addText( final String label, final Font font, final Color color, final IHyperCanvasSizeHandler handler )
  {
    m_text.add( new HyperCanvasTextHandler( label, font, color, handler ) );
  }

  /**
   * adds an mouse listener for image x
   */
  public void addMouseListener( final MouseListener listener, final Image image, final String tooltip )
  {
    List<MouseListener> list = m_mouseListeners.get( image );
    if( list == null )
    {
      list = new ArrayList<MouseListener>();
    }

    list.add( listener );

    m_mouseListeners.put( image, list );
    m_tooltips.put( image, tooltip );
  }

}
