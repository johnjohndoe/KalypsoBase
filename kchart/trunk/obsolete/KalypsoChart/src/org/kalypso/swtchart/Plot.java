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
package org.kalypso.swtchart;

import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.axis.IAxis;
import org.kalypso.swtchart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.axis.component.AxisComponent;
import org.kalypso.swtchart.axis.registry.AxisRegistry;
import org.kalypso.swtchart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.layer.IChartLayer;

/**
 * @author schlienger
 */
public class Plot extends Canvas implements PaintListener, MouseListener, MouseMoveListener, Listener
{
  private final Chart m_chart;
  
  private Point m_mouseDragStart;
  private Point m_mouseDragEnd;
  private boolean m_mousePressed=false;;
  private Rectangle m_mouseDragRect;

  private Image m_bufferedPlot=null;
  private Image m_bufferImg=null;

  public Plot( Chart chart, Composite parent, int style )
  {
    super( parent, style );
    m_chart = chart;

    addPaintListener( this );
    addMouseListener(this);
    addMouseMoveListener(this);
    addListener(SWT.RESIZE, this);
    addListener(SWT.Resize, this);
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    removePaintListener( this );
    if (m_bufferedPlot!=null)
    {
      m_bufferedPlot.dispose();
      m_bufferedPlot=null;
    }
    if (m_bufferImg!=null)
    {
      m_bufferedPlot.dispose();
      m_bufferImg=null;
    }
    super.dispose();
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   * 
   * Zeichnet nacheinander alle Layer; falls eine MouseDrag-Aktion ausgeführt wird, werden die Werte nicht 
   * neu berechnet, sondern ein gepuffertes Img angezeigt 
   */
  public void paintControl( final PaintEvent e )
  {
    final GCWrapper gcw = new GCWrapper( e.gc );
    
    //Antialiasing setzen
    gcw.m_gc.setAntialias(SWT.ON);
    
    /*
    //das PufferBild erzeugen - aber nur, wenn nicht gerade eine MausGeste gemacht wird
    if (m_mouseDragRect==null && !m_mousePressed)
    {
      paintBuffered(gcw, e.display);
    }
    if (m_bufferedPlot!=null)
    {
      gcw.drawImage(m_bufferedPlot, 0,0);
      
    }
    */

    final Rectangle screenArea = getClientArea();
    
    
    m_bufferImg = paintChart( e.display, gcw, screenArea, m_bufferImg );
    
    
    //Wenn ein DragRectangle da ist, dann muss nur das gezeichnet werden
    if (m_mouseDragRect!=null && m_mousePressed)
    {
      gcw.setForeground(new Color(e.display,0,0,0));
      gcw.setLineStyle(SWT.LINE_DASH);
      gcw.setLineWidth(1);
      gcw.drawRectangle(m_mouseDragRect);
    }
   gcw.dispose();
    
  }
  
  /**
   * ÜBernommen und angepasst von de.belger.swtchart
   * zeichnet zuerst ein Image vom Chart, das dann in dem GC vom Plot gezeichnet (doubleBuffering) und 
   * zurückgegeben wird; es wird also nur erzeugt, wenn explizit redraw() aufgerufen wird 
   */
  private Image paintChart( Device dev, GCWrapper gcw, Rectangle screen, Image bufferImage )
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
          ChartUtilities.resetGC(buffGc.m_gc, dev);
          buffGc.setAlpha(170);
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
    gcw.drawImage( usedBufferImage, 0,0);
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

  private void paintBuffered( GCWrapper gcw, Device dev )
  {
    /*
     * Dummerweise braucht man ein Display, um das PufferImage zu erzeugen - und das geht nur innerhalb
     * des paintcontrol-Zweigs.
     * Das führt aber dazu, dass man stets das alte Image disposen und neu erzeugen muss - das kostet
     * ziemlich rechenleistung
     */
    

    Rectangle sizeRect=getBounds();
    /*
     * >TODO: was hat gernot im SWTChart zum DoubleBuff.. gemacht
     */
    if (m_bufferedPlot!=null)
      m_bufferedPlot.dispose();
    m_bufferedPlot=new Image(dev, sizeRect);
   
    
    GC imgGc=new GC(m_bufferedPlot);
    GCWrapper imgGcw=new GCWrapper(imgGc);
    
    
    final List<IChartLayer> layers = m_chart.getLayers();
    for( final IChartLayer layer : layers )
    {
      ChartUtilities.resetGC(imgGcw.m_gc, dev);
      layer.paint( imgGcw, dev );
    }

    imgGc.dispose();
    imgGcw.dispose();
      
  }
  
  
  
  

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDoubleClick( MouseEvent e )
  {
      
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDown( final MouseEvent e )
  {
    m_mouseDragStart=new Point (e.x, e.y);
    m_mouseDragRect=new Rectangle(e.x, e.y,0,0);
    m_mousePressed=true;
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( MouseEvent e )
  {
    m_mouseDragEnd=new Point (e.x, e.y);
    
    //Wenn nach rechts unten gezogen wurde, wird reingezoomt
    if ( 
          (m_mouseDragStart.x<m_mouseDragEnd.x)
          &&
          (m_mouseDragStart.y<m_mouseDragEnd.y)
      )
    {
      zoomIn(m_mouseDragStart, m_mouseDragEnd);
    }
    //Wenn nach links oben gezogen wurde, wird rausgezoomt
    else if ( 
        (m_mouseDragStart.x>m_mouseDragEnd.x)
        &&
        (m_mouseDragStart.y>m_mouseDragEnd.y)
    )
    {
      
      zoom(2);
    }
    
    m_mousePressed=false;
    m_mouseDragRect=null;
  }
  
  private void zoomIn(Point start, Point end)
  {
    IAxisRegistry ar=m_chart.getAxisRegistry();
    for (IAxis axis:ar.getAxes())
    {
      if (axis.getPosition().getOrientation()==ORIENTATION.HORIZONTAL)
      {
        if (axis.getDirection()==DIRECTION.POSITIVE)
        {
          axis.setFrom(axis.screenToLogical(Math.min(start.x, end.x)));
          axis.setTo(axis.screenToLogical(Math.max(start.x, end.x)));
        }
        else if (axis.getDirection()==DIRECTION.NEGATIVE)
        {
          axis.setFrom(axis.screenToLogical(Math.max(start.x, end.x)));
          axis.setTo(axis.screenToLogical(Math.min(start.x, end.x)));
        }
      }
      else if (axis.getPosition().getOrientation()==ORIENTATION.VERTICAL)
      {
        if (axis.getDirection()==DIRECTION.NEGATIVE)
        {
          axis.setFrom(axis.screenToLogical(Math.min(start.y, end.y)));
          axis.setTo(axis.screenToLogical(Math.max(start.y, end.y)));
        }
        else if (axis.getDirection()==DIRECTION.POSITIVE)
        {
          axis.setFrom(axis.screenToLogical(Math.max(start.y, end.y)));
          axis.setTo(axis.screenToLogical(Math.min(start.y, end.y)));
        }
        
      }
      /*
       * zum Neuzeichnen der Achse muss nach die IAxisComponent nach AxisComponent gecastet werden -
       * sonst gibts keinen Zurgriff auf die Paint-Sachen
       */
      AxisComponent ac=(AxisComponent) ar.getComponent(axis);
      ac.redraw();
    }
   // m_chart.redraw();
    repaint();
  }

  
  /**
   * Gibt die Größe des neuen Ausschnitts im Verhältnis zum alten Ausschnitt an
   */
  private void zoom(double zoomFactor)
  {
    IAxisRegistry ar=m_chart.getAxisRegistry();
    IAxis[] axes=ar.getAxes();
    Rectangle bounds=getBounds();
    if (getDisplay()!=null && getDisplay().getActiveShell()!=null)
      bounds=getDisplay().getActiveShell().getBounds();
    for (IAxis axis:axes)
    {
      if (axis.getPosition().getOrientation()==ORIENTATION.HORIZONTAL)
      {
        if (axis.getDirection()==DIRECTION.POSITIVE)
        {
          axis.setFrom(axis.screenToLogical((int) (0-0.5*( zoomFactor*bounds.width- bounds.width ))));
          axis.setTo(axis.screenToLogical((int) (bounds.width+0.5*( zoomFactor*bounds.width- bounds.width ))));
        }
        else if (axis.getDirection()==DIRECTION.NEGATIVE)
        {
            axis.setTo(axis.screenToLogical((int) (0-0.5*( zoomFactor*bounds.width- bounds.width ))));
            axis.setFrom(axis.screenToLogical((int) (bounds.width+0.5*( zoomFactor*bounds.width- bounds.width ))));
        }
      }
      if (axis.getPosition().getOrientation()==ORIENTATION.VERTICAL)
      {
        if (axis.getDirection()==DIRECTION.NEGATIVE)
        {
          axis.setFrom(axis.screenToLogical((int) (0-0.5*( zoomFactor*bounds.width- bounds.width ))));
          axis.setTo(axis.screenToLogical((int) (bounds.width+0.5*( zoomFactor*bounds.width- bounds.width ))));
        }
        else if (axis.getDirection()==DIRECTION.POSITIVE)
        {
          axis.setTo(axis.screenToLogical((int) (0-0.5*( zoomFactor*bounds.width- bounds.width ))));
          axis.setFrom(axis.screenToLogical((int) (bounds.width+0.5*( zoomFactor*bounds.width- bounds.width ))));
        }
      }
      /*
       * zum Neuzeichnen der Achse muss nach die IAxisComponent nach AxisComponent gecastet werden -
       * sonst gibts keinen Zurgriff auf die Paint-Sachen
       */
      AxisComponent ac=(AxisComponent) ar.getComponent(axis);
      ac.redraw();
    }
    repaint();
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( MouseEvent e )
  {
      if (m_mousePressed==true)
      {
        
        //redraw kann nicht mit negativen Breiten und Höhen umgehen, daher muss der Startpunkt umgerechnet werden;
        int x0=m_mouseDragStart.x;
        int y0=m_mouseDragStart.y;
        int x1=e.x+m_mouseDragRect.width;
        int y1= e.y+m_mouseDragRect.height;
        redraw(Math.min(x0,x1)-1, Math.min(y0, y1)-1 ,Math.abs(x1-x0)+2 ,Math.abs(y1-y0)+2, true);
        //redraw();
        m_mouseDragRect.width=e.x-m_mouseDragStart.x;
        m_mouseDragRect.height=e.y-m_mouseDragStart.y;
        
      }
  }

  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent( Event event )
  {
    repaint();
    
  }

 
  
}
