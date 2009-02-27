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
package org.kalypso.swtchart.chart.layer;

import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.styles.ILayerStyle;

/**
 * @author alibu
 */
public abstract class AbstractChartLayer implements IChartLayer
{
  protected  boolean m_isVisible = true;

  protected  ILayerStyle m_style;

  protected  String m_name;

  protected String m_description;

  protected  boolean m_isActive;

  protected  IAxis m_valueAxis;

  protected  IAxis m_domainAxis;

  // TODO; add everything to constructor
  public AbstractChartLayer( final String name, final String description, final IAxis domainAxis, final IAxis valueAxis )
  {
    m_name = name;
    m_description = description;
    m_domainAxis = domainAxis;
    m_valueAxis = valueAxis;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDescription()
   */
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainAxis()
   */
  public IAxis getDomainAxis( )
  {
    return m_domainAxis;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getName()
   */
  public String getName( )
  {
    return m_name;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getStyle()
   */
  public ILayerStyle getStyle( )
  {
    return m_style;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getValueAxis()
   */
  public IAxis getValueAxis( )
  {
    return m_valueAxis;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getVisibility()
   */
  public boolean getVisibility( )
  {
    return m_isVisible;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isActive()
   */
  public boolean isActive( )
  {
    return m_isActive;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setDescription(java.lang.String)
   */
  public void setDescription( String description )
  {
    m_description = description;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setName(java.lang.String)
   */
  public void setName( String name )
  {
    m_name = name;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setStyle(org.kalypso.swtchart.chart.styles.ILayerStyle)
   */
  public void setStyle( ILayerStyle style )
  {
    m_style = style;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setVisibility(boolean)
   */
  public void setVisibility( boolean isVisible )
  {
    m_isVisible = isVisible;

  }

}
