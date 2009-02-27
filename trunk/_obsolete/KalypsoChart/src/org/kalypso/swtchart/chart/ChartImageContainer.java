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
package org.kalypso.swtchart.chart;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.component.AxisComponent;
import org.kalypso.swtchart.chart.axis.component.IAxisComponent;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;

/**
 * @author burtscher
 *
 * Creates an Image from a chart widget.
 *
 * The objects has to be disposed when it's no longer needed
 */
public class ChartImageContainer
{

  Image m_img = null;
  private Chart m_chart;



  /**
   * @param pre-loaded chart object
   * @param dev Device for which the image should be created
   * @param width width of the Image
   * @param height height of the Image
   */
  public ChartImageContainer( Chart chart, Device dev, int width, int height )
  {
    createChartImage( chart, dev, width, height );
  }

  /**
   * @return the Image containing the chart
   */
  public Image getImage( )
  {
    return m_img;
  }

  /*
   * disposes the disposable elements
   */
  public void dispose( )
  {
    if( m_img != null )
      m_img.dispose();
    m_img = null;
  }

  /**
   *
   *
   * TODO: set to private
   */
  public void createChartImage( Chart chart, Device dev, int width, int height )
  {
    chart.setSize( new Point( width, height ) );
    chart.layout();
    chart.update();

    m_img = new Image( dev, width, height );

    GCWrapper gcw = new GCWrapper( new GC( m_img ) );
    Image tmpImg = new Image( dev, width, height );
    GC tmpGc = new GC( tmpImg );
    GCWrapper tmpGcw = new GCWrapper( tmpGc );
    tmpGcw.setBackground( dev.getSystemColor( SWT.COLOR_YELLOW ) );
    tmpGcw.fillRectangle( 0, 0, tmpImg.getBounds().width, tmpImg.getBounds().height );

    // img mit weißem Hintergrund versehen
    gcw.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    gcw.fillRectangle( 0, 0, width, height );

    // Plot zeichnen
    Plot plot = chart.getPlot();
    tmpImg = plot.paintChart( dev, tmpGcw, m_img.getBounds(), null );
    gcw.drawImage( tmpImg, 0, 0, plot.getBounds().width, plot.getBounds().height, plot.getBounds().x, plot.getBounds().y, plot.getBounds().width, plot.getBounds().height );
    tmpGcw.fillRectangle( 0, 0, tmpImg.getBounds().width, tmpImg.getBounds().height );

    IAxisRegistry ar = chart.getAxisRegistry();
    Map<IAxis, IAxisComponent> components = ar.getComponents();
    Set<Entry<IAxis, IAxisComponent>> acs = components.entrySet();
    for( Entry<IAxis, IAxisComponent> ac : acs )
    {
      IAxis axis = ac.getKey();
      // Casten, damit man auf die Component-Eigenschaften zugreifen kann
      AxisComponent comp = (AxisComponent) ac.getValue();
      IAxisRenderer rend = axis.getRenderer();

      // Wenn man den GC nicht neu erzeugt, werden die AChsen nicht gezeichnet, sondern nochmal das Chart
      tmpGc.dispose();
      tmpGcw.dispose();
      tmpGc = new GC( tmpImg );
      tmpGcw = new GCWrapper( tmpGc );
      // den Renderer in den TmpGC-Zeichnen lassen
      rend.paint( tmpGcw, axis, comp.getBounds() );
      /*
       * ...und ins Endbild kopieren; dabei muss berücksichtigt werden, dass die Components in 2 Ebenen Tiefe liegen -
       * zur Position muss noch die der Eltern addiert werden
       */
      gcw.drawImage( tmpImg, comp.getBounds().x, comp.getBounds().y, comp.getBounds().width, comp.getBounds().height, comp.getParent().getBounds().x + comp.getBounds().x, comp.getParent().getBounds().y
          + comp.getBounds().y, comp.getBounds().width, comp.getBounds().height );
    }
  //  chart.dispose();
    gcw.dispose();
    tmpGc.dispose();
    tmpGcw.dispose();
    tmpImg.dispose();
  }

  private Chart getChart()
  {
    return m_chart;
  }
}
