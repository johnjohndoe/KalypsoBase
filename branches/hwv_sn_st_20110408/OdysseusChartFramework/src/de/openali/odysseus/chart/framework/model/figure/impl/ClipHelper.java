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
package de.openali.odysseus.chart.framework.model.figure.impl;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import de.openali.odysseus.chart.framework.util.resource.IPair;

/**
 * @author Gernot Belger
 */
public class ClipHelper
{
  private final Rectangle2D m_clipRect;

  public ClipHelper( final Rectangle2D clipRect )
  {
    m_clipRect = clipRect;
  }

  /**
   * Considers the given points as a LineString and intersects it with the current clip rectangle.<br/>
   * 
   * @param Possible
   *          multiple sub-line-strings of the original line string. The whole thing, if no clip was defined.
   */
  @SuppressWarnings("unchecked")
  public IPair<Number, Number>[][] clipAsLine( final IPair<Number, Number>[] points )
  {
    if( m_clipRect == null || points.length == 0 )
      return new IPair[][] { points };

    final Collection<IPair<Number, Number>> result = new ArrayList<IPair<Number, Number>>();

    for( final IPair<Number, Number> point : points )
    {
      final Point2D point2D = new Point2D.Double( point.getDomain().doubleValue(), point.getTarget().doubleValue() );
      if( m_clipRect.contains( point2D ) )
        result.add( point );
    }

    return new IPair[][] { result.toArray( new IPair[result.size()] ) };
  }
}