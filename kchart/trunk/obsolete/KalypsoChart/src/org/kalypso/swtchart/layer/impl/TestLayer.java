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
package org.kalypso.swtchart.layer.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.kalypso.commons.math.SampleData;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.axis.IAxis;
import org.kalypso.swtchart.layer.IChartLayer;
import org.kalypso.swtchart.layer.IDataRange;
import org.kalypso.swtchart.legend.ILegendItem;
import org.kalypso.swtchart.styles.ILayerStyle;

/**
 * @author schlienger
 */
public class TestLayer implements IChartLayer
{
  private final IAxis m_domAxis;

  private final IAxis m_valAxis;

  private final Double[][] m_doubles;

  public TestLayer( IAxis domAxis, IAxis valAxis )
  {
    m_domAxis = domAxis;
    m_valAxis = valAxis;

    m_doubles = SampleData.createRandomPoints( 20 );
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
   * @see org.kalypso.swtchart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper, org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gc, Device dev )
  {
    gc.setForeground( dev.getSystemColor( SWT.COLOR_DARK_MAGENTA ) );
    
    for( int i = 0, j = 1; j < m_doubles.length; i++, j++ )
    {
      int x1 = m_domAxis.logicalToScreen( m_doubles[i][0] );
      int y1 = m_valAxis.logicalToScreen( m_doubles[i][1] );

      int x2 = m_domAxis.logicalToScreen( m_doubles[j][0] );
      int y2 = m_valAxis.logicalToScreen( m_doubles[j][1] );

      gc.drawLine( x1, y1, x2, y2 );
    }
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getDomainRange()
   */
  public IDataRange getDomainRange( )
  {
    Double xMin = null;
    Double xMax = null;

    for( int i = 0; i < m_doubles.length; i++ )
    {
      double x = m_doubles[i][0];

      if( xMin == null || x < xMin )
        xMin = x;
      if( xMax == null || x > xMax )
        xMax = x;
    }

    return new DataRange<Number>( xMin, xMax );
  }

  /**
   * @see org.kalypso.swtchart.layer.IChartLayer#getValueRange()
   */
  public IDataRange getValueRange( )
  {
    Double yMin = null;
    Double yMax = null;

    for( int i = 0; i < m_doubles.length; i++ )
    {
      double y = m_doubles[i][1];

      if( yMin == null || y < yMin )
        yMin = y;
      if( yMax == null || y > yMax )
        yMax = y;
    }

    return new DataRange<Number>( yMin, yMax );
  }
}
