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
package org.kalypso.swtchart.chart;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.component.AxisComponent;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.mouse.DragMouseHandler;
import org.kalypso.swtchart.chart.mouse.IChartMouseHandler;

/**
 * @author schlienger
 * @author burtscher
 * 
 * widget in which the layers' content is paintet
 */
public class Plot extends Canvas implements PaintListener, Listener, MouseListener, MouseMoveListener
{
  private final Chart m_chart;

  private Image m_bufferImg = null;

  private IChartMouseHandler m_mouseHandler;

  public Plot( final Chart chart, final Composite parent, final int style )
  {
    super( parent, style );
    m_chart = chart;

    addPaintListener( this );

    // TODO: set mouse handler from outside
    m_mouseHandler = new DragMouseHandler( this );
    
    addMouseListener( this );
    addMouseMoveListener( this );

    addListener( SWT.Resize, this );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    removePaintListener( this );
    if( m_bufferImg != null )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }
    super.dispose();
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent) 
   * 
   * Draws all layers if a PaintEvent is thrown; only exeption: if the PaintEvent is thrown by a
   * MouseDrag-Action, a buffered plot image is used
   */
  public void paintControl( final PaintEvent e )
  {
    final GCWrapper gcw = new GCWrapper( e.gc );

    // Antialiasing setzen
    gcw.m_gc.setAntialias( SWT.ON );

    final Rectangle screenArea = getClientArea();

    m_bufferImg = paintChart( e.display, gcw, screenArea, m_bufferImg );

    m_mouseHandler.paint( gcw );

    gcw.dispose();
  }

  /**
   * 
   * double-buffered paint method; set to public in order to be used from ouside, e.g. from
   * ChartImageContainer
   */
  public Image paintChart( Device dev, GCWrapper gcw, Rectangle screen, Image bufferImage )
  {
    final Image usedBufferImage;
    if( bufferImage == null )
    {
      usedBufferImage = new Image( dev, screen.width, screen.height );

      final GCWrapper buffGc = new GCWrapper( new GC( usedBufferImage ) );

      try
      {
        final List<IChartLayer> layers = m_chart.getLayers();
        for( final IChartLayer layer : layers )
        {
          ChartUtilities.resetGC( buffGc.m_gc, dev );
          layer.paint( buffGc, dev );
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
    else
      usedBufferImage = bufferImage;

    // muss so sein, wenn mann den layerClip immer setzt, kommts beim dragRect zeichnen zu
    // selstsamen effekten
    gcw.drawImage( usedBufferImage, screen.x, screen.y, screen.width, screen.height, screen.x, screen.y, screen.width, screen.height );
    return usedBufferImage;
  }

  /** Forces a repaint of the complete chart. The buffered image is disposed. */
  public void repaint( )
  {
    if( m_bufferImg != null )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }

    redraw();
  }

  /**
   * maximises the content of the plot to the values inside a dragged rectangle
   */
  public void zoomIn( final Point start, final Point end )
  {
    final IAxisRegistry ar = m_chart.getAxisRegistry();
    final IAxis[] axes = ar.getAxes();
    for( IAxis axis : axes )
    {
      Object from = null;
      Object to = null;

      switch( axis.getPosition().getOrientation() )
      {
        case HORIZONTAL:
          switch( axis.getDirection() )
          {
            case POSITIVE:
              from = axis.screenToLogical( Math.min( start.x, end.x ) );
              to = axis.screenToLogical( Math.max( start.x, end.x ) );
              break;

            case NEGATIVE:
              from = axis.screenToLogical( Math.max( start.x, end.x ) );
              to = axis.screenToLogical( Math.min( start.x, end.x ) );
              break;
          }
          break;

        case VERTICAL:
          switch( axis.getDirection() )
          {
            case POSITIVE:
              from = axis.screenToLogical( Math.max( start.y, end.y ) );
              to = axis.screenToLogical( Math.min( start.y, end.y ) );
              break;

            case NEGATIVE:
              from = axis.screenToLogical( Math.min( start.y, end.y ) );
              to = axis.screenToLogical( Math.max( start.y, end.y ) );
              break;
          }
          break;
      }

      // TRICKY: always set from and to in one go, bceause 'screenToLogcal' depends on its values
      if( from != null && to != null )
      {
        axis.setFrom( from );
        axis.setTo( to );

        /*
         * zum Neuzeichnen der Achse muss nach die IAxisComponent nach AxisComponent gecastet werden - sonst gibts
         * keinen Zugriff auf die Paint-Sachen
         */
        AxisComponent ac = (AxisComponent) ar.getComponent( axis );
        ac.redraw();
      }
    }
    repaint();
  }


  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent( final Event event )
  {
    if( event.type == SWT.Resize )
      repaint();
  }

  /**
   * @return Image used to buffer plot graphics
   */
  public Image getBufferImg( )
  {
    return m_bufferImg;
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDoubleClick( final MouseEvent e )
  {
    if( m_mouseHandler != null )
      m_mouseHandler.mouseDoubleClick( e );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDown( final MouseEvent e )
  {
    if( m_mouseHandler != null )
      m_mouseHandler.mouseDown( e );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    if( m_mouseHandler != null )
      m_mouseHandler.mouseUp( e );
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {
    if( m_mouseHandler != null )
      m_mouseHandler.mouseMove( e );
  }

}
