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
package org.kalypso.ogc.sensor;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;

/**
 * @author Dirk Kuch
 */
public class AxisSet
{
  private final IAxis m_valueAxis;

  private IAxis m_statusAxis;

  private IAxis m_datasourceAxis;

  private IAxis[] m_axes;

  public AxisSet( final IAxis[] axes, final IAxis valueAxis )
  {
    m_axes = axes;
    m_valueAxis = valueAxis;
  }

  public AxisSet( final IAxis valueAxis, final IAxis statusAxis, final IAxis datasourceAxis )
  {
    m_valueAxis = valueAxis;
    setStatusAxis( statusAxis );
    setDatasourceAxis( datasourceAxis );
  }

  public void setStatusAxis( final IAxis statusAxis )
  {
    m_statusAxis = statusAxis;
  }

  public IAxis getStatusAxis( )
  {
    if( Objects.isNull( m_statusAxis ) && ArrayUtils.isNotEmpty( m_axes ) )
    {
      m_statusAxis = AxisUtils.findStatusAxis( m_axes, getValueAxis() );
    }

    return m_statusAxis;
  }

  public void setDatasourceAxis( final IAxis datasourceAxis )
  {
    m_datasourceAxis = datasourceAxis;
  }

  public IAxis getDatasourceAxis( )
  {
    if( Objects.isNull( m_datasourceAxis ) && ArrayUtils.isNotEmpty( m_axes ) )
    {
      m_datasourceAxis = AxisUtils.findDataSourceAxis( m_axes, getValueAxis() );
    }

    return m_datasourceAxis;
  }

  public IAxis getValueAxis( )
  {
    return m_valueAxis;
  }

  public boolean hasAxis( final IAxis axis )
  {
    if( AxisUtils.isEqual( getValueAxis(), axis ) )
      return true;
    else if( AxisUtils.isEqual( getStatusAxis(), axis ) )
      return true;
    else if( AxisUtils.isEqual( getDatasourceAxis(), axis ) )
      return true;

    return false;
  }

}
