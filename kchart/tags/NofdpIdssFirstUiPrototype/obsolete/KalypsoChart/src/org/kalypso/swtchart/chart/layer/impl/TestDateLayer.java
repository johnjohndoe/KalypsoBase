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
package org.kalypso.swtchart.chart.layer.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.kalypso.commons.math.SampleData;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.IDataRange;
import org.kalypso.swtchart.chart.legend.ILegendItem;
import org.kalypso.swtchart.chart.styles.ILayerStyle;
import org.kalypso.swtchart.chart.styles.IStyledElement;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;
import org.kalypso.swtchart.chart.util.AliComparator;

/**
 * @author burtscher
 */
public class TestDateLayer implements IChartLayer
{
  private final IAxis m_domAxis;

  private final IAxis m_valAxis;

  private final TreeMap<Date, Double> m_values;

  private final ILayerStyle m_style = null;

//  private String m_name;

//  private String m_description;

  public TestDateLayer( IAxis domAxis, IAxis valAxis )
  {
    m_domAxis = domAxis;
    m_valAxis = valAxis;
    m_values = SampleData.createRandomDatePoints( 25 );
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getName()
   */
  public String getName( )
  {
    return "TestLayer";
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
    gc.setForeground( dev.getSystemColor( SWT.COLOR_BLACK ) );
    if( m_style != null )
    {
//      IStyledElement line = m_style.getElement( SE_TYPE.LINE, 0 );
//      IStyledElement point = m_style.getElement( SE_TYPE.POINT, 0 );
    }
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getDomainRange()
   */
  public IDataRange getDomainRange( )
  {
    Date xMin = null;
    Date xMax = null;

    Set<Date> keys = m_values.keySet();
    Iterator<Date> keyIt = keys.iterator();

    AliComparator dc = new AliComparator();

    while( keyIt.hasNext() )
    {
      Date x = keyIt.next();
      if( xMin == null || dc.compare( x, xMin ) < 0 )
        xMin = x;
      if( xMax == null || dc.compare( x, xMax ) > 0 )
        xMax = x;
    }

    return new DataRange<Date>( xMin, xMax );
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getValueRange()
   */
  public IDataRange getValueRange( )
  {
    Double yMin = null;
    Double yMax = null;

    Set<Date> keys = m_values.keySet();
    Iterator<Date> keyIt = keys.iterator();

    while( keyIt.hasNext() )
    {
      double y = m_values.get( keyIt.next() );
      if( yMin == null || y < yMin )
        yMin = y;
      if( yMax == null || y > yMax )
        yMax = y;
    }

    return new DataRange<Double>( yMin, yMax );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( Image img, int width, int height )
  {
    GC gc = new GC( img );
    GCWrapper gcw = new GCWrapper( gc );

    gcw.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_YELLOW ) );
    gcw.fillRectangle( 0, 0, width, height );

    IStyledElement line = m_style.getElement( SE_TYPE.LINE, 0 );
    IStyledElement point = m_style.getElement( SE_TYPE.POINT, 0 );

    if( line != null )
    {
      ArrayList<Point> points = new ArrayList<Point>();
      // Linie von Links nach rechts
      points.add( new Point( 0, height / 2 ) );
      points.add( new Point( width, height / 2 ) );
      line.setPath( points );
      line.paint( gcw, Display.getCurrent() );
    }

    if( point != null )
    {
      ArrayList<Point> points = new ArrayList<Point>();
      // Linie von Links nach rechts
      points.add( new Point( width / 2, height / 2 ) );
      point.setPath( points );
      point.paint( gcw, Display.getCurrent() );
    }

    gc.dispose();
    gcw.dispose();

  }

  public void setName( String name )
  {
//    m_name = name;
  }

  public void setDescription( String description )
  {
//    m_description = description;
  }
}
