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
package org.kalypso.contribs.eclipse.swt.canvas;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Dirk Kuch
 */
public class ImageCanvas2 extends Canvas
{
  // TODO implement redraw event for hover images

  protected final Set<IContentArea> m_contents = new LinkedHashSet<IContentArea>();

  private final PaintListener m_paintListener;

  private final MouseMoveListener m_mouseMoveListener;

  public ImageCanvas2( final Composite parent, final int style )
  {
    super( parent, style );

    final Canvas myCanvas = this;

    /** paint listener */
    m_paintListener = new PaintListener()
    {
      @Override
      public void paintControl( final PaintEvent e )
      {
        if( myCanvas.isDisposed() )
          return;

        for( final IContentArea area : m_contents )
        {
          area.draw( e );
        }
      }
    };
    this.addPaintListener( m_paintListener );

    /* handle mouse move events */
    final Cursor cursorHand = new Cursor( this.getDisplay(), SWT.CURSOR_HAND );
    final Cursor cursorDefault = new Cursor( this.getDisplay(), SWT.CURSOR_ARROW );

    m_mouseMoveListener = new MouseMoveListener()
    {
      @Override
      public void mouseMove( final MouseEvent e )
      {
        if( myCanvas.isDisposed() )
          return;

        boolean changed = false;

        Cursor cursor = cursorDefault;
        String tooltip = null;

        for( final IContentArea area : m_contents )
        {
          if( area.hasMouseListener() && area.isEnabled() )
          {
            final Rectangle boundingBox = area.getBoundingBox();
            if( boundingBox.contains( e.x, e.y ) )
            {
              final boolean hoverStateChanged = area.hover( true );
              if( hoverStateChanged )
              {
                changed = true;
              }

              cursor = cursorHand;
              tooltip = area.getTooltip();
            }
            else
            {
              final boolean hoverStateChanged = area.hover( false );
              if( hoverStateChanged )
              {
                changed = true;
              }
            }
          }
        }

        if( changed )
        {
          redraw();
        }

        myCanvas.setCursor( cursor );
        myCanvas.setToolTipText( tooltip );
      }
    };

    this.addMouseMoveListener( m_mouseMoveListener );

    /* mouse listener */
    this.addMouseListener( new MouseAdapter()
    {
      /**
       * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
       */
      @Override
      public void mouseUp( final MouseEvent e )
      {
        for( final IContentArea area : m_contents )
        {
          if( area.hasMouseListener() )
          {
            if( myCanvas.isDisposed() )
              return;

            final Rectangle boundingBox = area.getBoundingBox();
            if( boundingBox.contains( e.x, e.y ) )
            {
              area.mouseUp( e );
            }
          }
        }
      }

      /**
       * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
       */
      @Override
      public void mouseDoubleClick( final MouseEvent e )
      {
        for( final IContentArea area : m_contents )
        {
          if( area.hasMouseListener() )
          {
            final Rectangle boundingBox = area.getBoundingBox();
            if( boundingBox.contains( e.x, e.y ) )
            {
              area.mouseDoubleClick( e );
            }
          }
        }
      }

      /**
       * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
       */
      @Override
      public void mouseDown( final MouseEvent e )
      {
        for( final IContentArea area : m_contents )
        {
          if( area.hasMouseListener() )
          {
            final Rectangle boundingBox = area.getBoundingBox();
            if( boundingBox.contains( e.x, e.y ) )
            {
              area.mouseDown( e );
            }
          }
        }
      }
    } );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    this.removePaintListener( m_paintListener );
    this.removeMouseMoveListener( m_mouseMoveListener );

    super.dispose();
  }

  public void addContentArea( final IContentArea area )
  {
    m_contents.add( area );
  }

}
