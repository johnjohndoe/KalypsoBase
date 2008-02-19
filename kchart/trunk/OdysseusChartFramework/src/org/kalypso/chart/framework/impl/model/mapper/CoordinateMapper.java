/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.chart.framework.impl.model.mapper;

import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.ICoordinateMapper;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;

/**
 * @author alibu
 * 
 */
public class CoordinateMapper implements ICoordinateMapper
{

  private final IAxis m_domainAxis;

  private final IAxis m_targetAxis;

  private final ORIENTATION m_ori;

  public CoordinateMapper( IAxis domain, IAxis target )
  {
    m_domainAxis = domain;
    m_targetAxis = target;
    m_ori = m_domainAxis.getPosition().getOrientation();
  }

  public Point numericToScreen( Number domVal, Number targetVal )
  {
    final int domScreen = m_domainAxis.numericToScreen( domVal );
    final int valScreen = m_targetAxis.numericToScreen( targetVal );
    Point unswitched = new Point( domScreen, valScreen );
    // Koordinaten switchen
    return new Point( m_ori.getX( unswitched ), m_ori.getY( unswitched ) );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.ICoordinateMapper#getDomainAxis()
   */
  public IAxis getDomainAxis( )
  {
    return m_domainAxis;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.ICoordinateMapper#getTargetAxis()
   */
  public IAxis getTargetAxis( )
  {
    return m_targetAxis;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.ICoordinateMapper#logicalToScreen(java.lang.Number, java.lang.Number)
   */
  public Point logicalToScreen( Number domainValue, Number targetValue )
  {
    // TODO Auto-generated method stub
    return null;
  }
}
