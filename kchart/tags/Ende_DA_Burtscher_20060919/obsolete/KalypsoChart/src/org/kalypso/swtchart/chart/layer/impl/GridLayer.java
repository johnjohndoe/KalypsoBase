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
package org.kalypso.swtchart.chart.layer.impl;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.IDataRange;
import org.kalypso.swtchart.chart.legend.ILegendItem;
import org.kalypso.swtchart.chart.styles.ILayerStyle;

/**
 * @author schlienger
 */
public class GridLayer implements IChartLayer
{
  private final IAxis m_domAxis;

  private final IAxis m_valAxis;

  private String m_name;

  private String m_description;

  public GridLayer( IAxis domAxis, IAxis valAxis )
  {
    m_domAxis = domAxis;
    m_valAxis = valAxis;

  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getName()
   */
  public String getName( )
  {
    return "GridLayer";
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getDescription()
   */
  public String getDescription( )
  {
    return "Keine Beschreibung";
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getDomainAxis()
   */
  public IAxis getDomainAxis( )
  {
    return m_domAxis;
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getValueAxis()
   */
  public IAxis getValueAxis( )
  {
    return m_valAxis;
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getLegendItem()
   */
  public ILegendItem getLegendItem( )
  {
    return null;
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#isActive()
   */
  public boolean isActive( )
  {
    return true;
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#isVisible()
   */
  public boolean isVisible( )
  {
    return true;
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getStyle()
   */
  public ILayerStyle getStyle( )
  {
    return null;
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#setStyle(org.kalypso.swtchart.styles.ILayerStyle)
   */
  public void setStyle( ILayerStyle style )
  {
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gc, Device dev )
  {
    gc.setForeground( dev.getSystemColor( SWT.COLOR_GRAY ) );
    gc.setLineWidth( 1 );
    gc.m_gc.setLineStyle( SWT.LINE_DASHDOT );

    Collection<Object> ticks = m_domAxis.getRenderer().calcTicks( gc, dev, m_domAxis );
    // Der Typ ist auf Object gesetzt, weil man nicht genau weiss, was calcTicks zurückliefert
    // Der Typ der calcTicks-Collection ist aber der gleiche, der auch für logicalToScreen verwendet wird
    for( Object n : ticks )
    {
      if( m_domAxis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      {
        int x1 = m_domAxis.logicalToScreen( n );
        int y1 = m_valAxis.logicalToScreen( m_valAxis.getFrom() );
        int x2 = m_domAxis.logicalToScreen( n );
        int y2 = m_valAxis.logicalToScreen( m_valAxis.getTo() );
        gc.drawLine( x1, y1, x2, y2 );
      }
      else
      {
        int y1 = m_domAxis.logicalToScreen( n );
        int x1 = m_valAxis.logicalToScreen( m_valAxis.getFrom() );
        int y2 = m_domAxis.logicalToScreen( n );
        int x2 = m_valAxis.logicalToScreen( m_valAxis.getTo() );
        gc.drawLine( x1, y1, x2, y2 );
      }
    }

    ticks = m_valAxis.getRenderer().calcTicks( gc, dev, m_valAxis );
    for( Object n : ticks )
    {
      if( m_domAxis.getPosition().getOrientation() != ORIENTATION.HORIZONTAL )
      {
        int x1 = m_valAxis.logicalToScreen( n );
        int y1 = m_domAxis.logicalToScreen( m_domAxis.getFrom() );
        int x2 = m_valAxis.logicalToScreen( n );
        int y2 = m_domAxis.logicalToScreen( m_domAxis.getTo() );
        gc.drawLine( x1, y1, x2, y2 );
      }
      else
      {
        int y1 = m_valAxis.logicalToScreen( n );
        int x1 = m_domAxis.logicalToScreen( m_domAxis.getFrom() );
        int y2 = m_valAxis.logicalToScreen( n );
        int x2 = m_domAxis.logicalToScreen( m_domAxis.getTo() );
        gc.drawLine( x1, y1, x2, y2 );
      }
    }

  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getDomainRange()
   */
  public IDataRange getDomainRange( )
  {
    return null;
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getValueRange()
   */
  public IDataRange getValueRange( )
  {
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( Image img, int width, int height )
  {
    // TODO Auto-generated method stub

  }

  public void setName( String name )
  {
    m_name = name;
  }

  public void setDescription( String description )
  {
    m_description = description;
  }
}
