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
package org.kalypso.chart.ext.base.style;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.model.styles.IStyledElement;

/**
 * @author burtscher1
 * 
 */
public abstract class AbstractStyledElement implements IStyledElement
{
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  private List<Point> m_path;

  private final String m_id;

  public AbstractStyledElement( final String id )
  {
    m_id = id;
  }

  /**
   * @see org.kalypso.chart.framework.styles.IStyledElement#setPath(java.util.List) The path is used draw a styled point
   *      at each point of the path; the Point is regarded as the center position of the element
   */
  public void setPath( final List<Point> path )
  {
    m_path = path;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  public void setData( final String id, final Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  public Object getData( final String id )
  {
    return m_data.get( id );
  }

  /**
   * @see org.kalypso.chart.framework.model.styles.IStyledElement#getId()
   */
  public String getId( )
  {
    return m_id;
  }

  protected List<Point> getPath( )
  {
    return m_path;
  }
}
