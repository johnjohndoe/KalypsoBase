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
package org.kalypso.swtchart.chart.axis.component;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.ChartUtilities;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;

/**
 * @author schlienger
 */
public class AxisComponent<T> extends Canvas implements PaintListener, IAxisComponent<T>
{
  private final IAxis<T> m_axis;

  private final Color m_white;

  public AxisComponent( final IAxis<T> axis, final Composite parent, final int style )
  {
    super( parent, style );
    m_white = new Color( null, 255, 255, 255 );
    setBackground( m_white );
    m_axis = axis;
    addPaintListener( this );
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
   * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean) TODO: im Moment gibt die Funktion noch einen
   *      Standardwert zurück - die wirklichen Ausmaße sollen aber berechnet werden
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

    final Rectangle bounds = getClientArea();
    final Rectangle b = new Rectangle( 0, 0, bounds.width, bounds.height );
    IAxisRenderer renderer = m_axis.getRenderer();
    if( renderer != null )
      renderer.paint( gc, dev, m_axis, b );
  }

  public int normalizedToScreen( double normValue )
  {
    final int range = getRange();
    if( ChartUtilities.isInverseScreenCoords( m_axis ) )
      normValue = 1 - normValue;
    final int screenValue = (int) (range * normValue);
    return screenValue;
  }

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

  private int getRange( )
  {
    final Rectangle bounds = getBounds();
    final int range;
    if( m_axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      range = bounds.width; // horizontal
    else
      range = bounds.height; // vertical
    return range;
  }

}
