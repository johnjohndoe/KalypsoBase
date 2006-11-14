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
package org.kalypso.swtchart.chart.axis.component;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.ChartUtilities;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;

/**
 * @author schlienger
 * @author burtscher
 * 
 * Implementation of IAxisComponent;
 * AxisComponent is a widget displaying the charts' axes; its used
 * to calculate screen coordinates for normalized values
 * 
 * 
 * 
 */
public class AxisComponent<T> extends Canvas implements PaintListener, IAxisComponent<T>
{
  /**
   * the corresponding axis
   */
  private final IAxis<T> m_axis;

  private final Color m_white;
  
/**
 * width (or height) of the margin
 */
  private int m_margin=0;
  
  /**
   * if set to true, an axis margin is calculated so that all labels are shown in full
   */
  private boolean m_useMargin=true;

  public AxisComponent( final IAxis<T> axis, final Composite parent, final int style )
  {
    super( parent, style );
    m_white = new Color( null, 255, 255, 255 );
    setBackground( m_white );
    m_axis = axis;
    addPaintListener( this );
    
    calcMargin();
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    removePaintListener( this );
    m_white.dispose();
    super.dispose();
  }
  
  /**
   * calculates a margin thats needed to show the full labels for the axis
   */
  private void calcMargin()
  {
    if (m_useMargin)
    {
      IAxisRenderer<T> ar = m_axis.getRenderer();
      GC gc=new GC(this);
      GCWrapper gcw=new GCWrapper(gc);
      if (ar!=null)
      {
        Point point =  ar.calcTickLabelSize( gcw, Display.getDefault(), m_axis );
        if (m_axis.getPosition().getOrientation()==ORIENTATION.HORIZONTAL)
        {
          m_margin=(int)(0.5*point.x);
        }
        else
        {
          m_margin=(int)(0.5*point.y);
        }
      }
      else
      {
        //Usring default value
        m_margin=30;
      }
      gc.dispose();
      gcw.dispose();
    }
  }

  /**
   * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean) 
   * 
   * @return Point p where p.x is the widgets width and p.y is its height;
   * as there is only 1 dimension in which the axis size needs to be fixed
   * - width for horizontal, height for vertical axes- the variable dimension
   * is set to 0
   */
  @Override
  public Point computeSize( int wHint, int hHint, boolean changed )
  {
    if( m_axis != null )
    {
      IAxisRenderer<T> renderer = m_axis.getRenderer();
      if( renderer != null )
      {
        int axisWidth = renderer.getAxisWidth( m_axis );
        if( m_axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
          return new Point( 0, axisWidth );
        else
          return new Point( axisWidth, 0 );
      }
    }
    return new Point( 0, 0 );
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl( final PaintEvent e )
  {
    paint( new GCWrapper( e.gc ), e.display );
  }

  private void paint( final GCWrapper gc, final Device dev )
  {
    calcMargin();
    final Rectangle bounds = getClientArea();
    int startx=0, starty=0, width=0, height =0;
    
    if (m_axis.getPosition().getOrientation()==ORIENTATION.HORIZONTAL)
    {
      startx=m_margin;
      width=-2*m_margin;
    }
    else
    {
      starty=m_margin;
      height=-2*m_margin;
    }
    final Rectangle b=new Rectangle( startx, starty, bounds.width+width, bounds.height+height );
    IAxisRenderer<T> renderer = m_axis.getRenderer();
    if( renderer != null )
      renderer.paint( gc, dev, m_axis, b );
  }

  /**
   * Uses the widgets' complete extension to alculates the screen value in correspondance to a normalized value
   * 
   * @see org.kalypso.swtchart.chart.axis.component.IAxisComponent#normalizedToScreen(double)
   */
  public int normalizedToScreen( double normValue )
  {
    final int range = getRange();
    if( ChartUtilities.isInverseScreenCoords( m_axis ) )
      normValue = 1 - normValue;
    final int screenValue = (int) (range * normValue)+m_margin;
    return screenValue;
  }

  /**
   * Uses the widgets' complete extension to alculates the normalized value in correspondance to a screen value
   * @see org.kalypso.swtchart.chart.axis.component.IAxisComponent#screenToNormalized(int)
   */
  public double screenToNormalized( int screenValue )
  {
    final int range = getRange();
    if( range == 0 )
      return 0;
    final double normValue = (double) screenValue / range;
    if( ChartUtilities.isInverseScreenCoords( m_axis ) )
      return 1 - normValue;
    
    return normValue;
  }

  /**
   * calculates the significant (= not variable) extension of the widget
   */
  private int getRange( )
  {
    final Rectangle bounds = getBounds();
    final int range;
    if( m_axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      range = bounds.width-2*m_margin; // horizontal
    else
      range = bounds.height-2*m_margin; // vertical
    return range;
  }

  /**
   * @see org.kalypso.swtchart.chart.axis.component.IAxisComponent#useMargin(boolean)
   */
  public void useMargin( boolean useIt )
  {
    m_useMargin=useIt;
  }

}
