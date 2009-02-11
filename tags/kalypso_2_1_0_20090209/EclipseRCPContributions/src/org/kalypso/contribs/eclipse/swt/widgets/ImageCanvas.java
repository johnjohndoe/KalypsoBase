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
 * A <code>Canvas</code> showing a single centered SWT <code>Image</code>.
 * If the <code>Image</code> is larger than the <code>Canvas<code>,
 * <code>Scrollbars</code> will appear.
 */
public class ImageCanvas extends Canvas
{

  private Image fImage;

  /*
   * Create a new ImageCanvas with the given SWT stylebits.
   * (SWT.H_SCROLL and SWT.V_SCROLL are automtically added).
   */
  public ImageCanvas(final Composite parent, final int style) {
    super(parent, style | SWT.H_SCROLL | SWT.V_SCROLL);

    ScrollBar sb= getHorizontalBar();
    sb.setIncrement(20);
    sb.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        repaint();
      }
    });

    sb= getVerticalBar();
    sb.setIncrement(20);
    sb.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event e) {
        repaint();
      }
    });

    addListener(SWT.Resize, new Listener() {
      public void handleEvent(final Event e) {
        updateScrollbars();
      }
    });

    addListener(SWT.Paint, new Listener() {
      public void handleEvent(final Event event) {
        paint(event.gc);
      }
    });
  }

  /*
   * Set the SWT Image to use as the ImageCanvas contents.
   */
  public void setImage(final Image img) {
    fImage= img;

    if (!isDisposed()) {
      getHorizontalBar().setSelection(0);
      getVerticalBar().setSelection(0);
      updateScrollbars();
      getParent().layout();
      redraw();
    }
  }

  public void repaint() {
    if (!isDisposed()) {
      final GC gc= new GC(this);
      paint(gc);
      gc.dispose();
    }
  }

  void paint(final GC gc) {
    if (fImage != null) {
      final Rectangle bounds= fImage.getBounds();
      final Rectangle clientArea= getClientArea();

      int x;
      if (bounds.width < clientArea.width)
        x= (clientArea.width - bounds.width) / 2;
      else
        x= -getHorizontalBar().getSelection();

      int y;
      if (bounds.height < clientArea.height)
        y= (clientArea.height - bounds.height) / 2;
      else
        y= -getVerticalBar().getSelection();

      gc.drawImage(fImage, x, y);
    }
  }

  /**
   * @private
   */
  void updateScrollbars() {
    final Rectangle bounds= fImage != null ? fImage.getBounds() : new Rectangle(0, 0, 0, 0);
    final Point size= getSize();
    final Rectangle clientArea= getClientArea();

    final ScrollBar horizontal= getHorizontalBar();
    if (bounds.width <= clientArea.width) {
      horizontal.setVisible(false);
      horizontal.setSelection(0);
    } else {
      horizontal.setPageIncrement(clientArea.width - horizontal.getIncrement());
      final int max= bounds.width + (size.x - clientArea.width);
      horizontal.setMaximum(max);
      horizontal.setThumb(size.x > max ? max : size.x);
      horizontal.setVisible(true);
    }

    final ScrollBar vertical= getVerticalBar();
    if (bounds.height <= clientArea.height) {
      vertical.setVisible(false);
      vertical.setSelection(0);
    } else {
      vertical.setPageIncrement(clientArea.height - vertical.getIncrement());
      final int max= bounds.height + (size.y - clientArea.height);
      vertical.setMaximum(max);
      vertical.setThumb(size.y > max ? max : size.y);
      vertical.setVisible(true);
    }
  }

}
