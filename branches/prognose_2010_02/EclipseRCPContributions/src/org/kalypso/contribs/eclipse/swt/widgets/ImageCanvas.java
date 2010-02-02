/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.kalypso.contribs.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * A <code>Canvas</code> showing a single centered SWT <code>Image</code>. If the <code>Image</code> is larger than the
 * <code>Canvas<code>,
 * <code>Scrollbars</code> will appear.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>RESIZE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 */
public class ImageCanvas extends Canvas
{
  private Image fImage;

  /*
   * Create a new ImageCanvas with the given SWT stylebits.<br> Either use SWT.H_SCROLL and SWT.V_SCROLL or SWT.RESIZE.
   */
  public ImageCanvas( final Composite parent, final int style )
  {
    super( parent, style );

    final ScrollBar sbHorz = getHorizontalBar();
    if( sbHorz != null )
    {
      sbHorz.setIncrement( 20 );
      sbHorz.addListener( SWT.Selection, new Listener()
      {
        public void handleEvent( final Event e )
        {
          repaint();
        }
      } );
    }

    final ScrollBar sbVert = getVerticalBar();
    if( sbVert != null )
    {
      sbVert.setIncrement( 20 );
      sbVert.addListener( SWT.Selection, new Listener()
      {
        public void handleEvent( final Event e )
        {
          repaint();
        }
      } );
    }

    addListener( SWT.Resize, new Listener()
    {
      public void handleEvent( final Event e )
      {
        handleResize();
      }
    } );

    addListener( SWT.Paint, new Listener()
    {
      public void handleEvent( final Event event )
      {
        paint( event.gc );
      }
    } );
  }

  public Image getImage( )
  {
    return fImage;
  }

  /*
   * Set the SWT Image to use as the ImageCanvas contents.
   */
  public void setImage( final Image img )
  {
    fImage = img;

    if( !isDisposed() )
    {
      final ScrollBar horizontalBar = getHorizontalBar();
      if( horizontalBar != null )
        horizontalBar.setSelection( 0 );

      final ScrollBar verticalBar = getVerticalBar();
      if( verticalBar != null )
        verticalBar.setSelection( 0 );

      updateScrollbars();
      getParent().layout();
      redraw();
    }
  }

  public void repaint( )
  {
    if( !isDisposed() )
    {
      final GC gc = new GC( this );
      paint( gc );
      gc.dispose();
    }
  }

  void paint( final GC gc )
  {
    if( fImage != null )
    {
      final Rectangle bounds = fImage.getBounds();
      final Rectangle clientArea = getClientArea();

      final int style = getStyle();
      if( (style & SWT.RESIZE) != 0 )
      {
        /* Fit into canvas */
        final Point adjustedDest = adjustDest( bounds, clientArea );

        gc.drawImage( fImage, 0, 0, bounds.width, bounds.height, clientArea.x, clientArea.y, adjustedDest.x, adjustedDest.y );
      }
      else
      {
        /* Draw according to scroll */
        int x;
        if( bounds.width < clientArea.width )
          x = (clientArea.width - bounds.width) / 2;
        else
          x = -getHorizontalBar().getSelection();

        int y;
        if( bounds.height < clientArea.height )
          y = (clientArea.height - bounds.height) / 2;
        else
          y = -getVerticalBar().getSelection();

        gc.drawImage( fImage, x, y );
      }
    }
  }

  private Point adjustDest( final Rectangle imageBounds, final Rectangle clientArea )
  {
    /* First adjust width and preserve ratio */
    int width = imageBounds.width;
    int height = imageBounds.height;
    if( imageBounds.width > clientArea.width )
    {
      width = clientArea.width;
      height = (int) (height * ((double) clientArea.width / (double) imageBounds.width));
    }

    /* then adjust height and preserve ratio */
    if( height > clientArea.height )
    {
      width = (int) (width * ((double) clientArea.height / (double) height));
      height = clientArea.height;
    }

    return new Point( width, height );
  }

  /**
   * @private
   */
  void handleResize( )
  {
    final int style = getStyle();
    if( (style & SWT.H_SCROLL) != 0 || (style & SWT.V_SCROLL) != 0 )
      updateScrollbars();
// else
// resizeImage();
  }

  /**
   * @private
   */
  void updateScrollbars( )
  {
    final Rectangle bounds = fImage != null ? fImage.getBounds() : new Rectangle( 0, 0, 0, 0 );
    final Point size = getSize();
    final Rectangle clientArea = getClientArea();

    final ScrollBar horizontal = getHorizontalBar();
    if( horizontal != null )
    {
      if( bounds.width <= clientArea.width )
      {
        horizontal.setVisible( false );
        horizontal.setSelection( 0 );
      }
      else
      {
        horizontal.setPageIncrement( clientArea.width - horizontal.getIncrement() );
        final int max = bounds.width + (size.x - clientArea.width);
        horizontal.setMaximum( max );
        horizontal.setThumb( size.x > max ? max : size.x );
        horizontal.setVisible( true );
      }
    }

    final ScrollBar vertical = getVerticalBar();
    if( vertical != null )
    {
      if( bounds.height <= clientArea.height )
      {
        vertical.setVisible( false );
        vertical.setSelection( 0 );
      }
      else
      {
        vertical.setPageIncrement( clientArea.height - vertical.getIncrement() );
        final int max = bounds.height + (size.y - clientArea.height);
        vertical.setMaximum( max );
        vertical.setThumb( size.y > max ? max : size.y );
        vertical.setVisible( true );
      }
    }
  }
}
