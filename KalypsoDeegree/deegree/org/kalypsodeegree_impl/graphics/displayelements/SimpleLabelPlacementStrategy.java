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
package org.kalypsodeegree_impl.graphics.displayelements;

import java.util.List;

import org.kalypsodeegree.graphics.displayelements.Label;
import org.kalypsodeegree_impl.model.sort.JSISpatialIndex;
import org.kalypsodeegree_impl.model.sort.SpatialIndexExt;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Simple placement strategy that just hides labels that are covered by other labels (first come first).
 *
 * @author Gernot Belger
 */
public class SimpleLabelPlacementStrategy implements ILabelPlacementStrategy
{
  // FIXME: use rtree
  private final SpatialIndexExt m_index = new JSISpatialIndex();

  public SimpleLabelPlacementStrategy( final Envelope screenRect )
  {
  }

  @Override
  public void add( final Label[] labels )
  {
    for( final Label label : labels )
      add( label );
  }

  @Override
  public void add( final Label label )
  {
    final Envelope extent = LabelUtils.toEnvelope( label );

    if( !overlaps( extent, label ) )
      m_index.insert( extent, label );
  }

  private boolean overlaps( final Envelope extent, final Label label )
  {
    final List<Label> overlappingLabels = m_index.query( extent );
    return overlaps( overlappingLabels, label );
  }

  private boolean overlaps( final List<Label> overlappingLabels, final Label label )
  {
    for( final Label other : overlappingLabels )
    {
      if( other.intersects( label ) )
        return true;
    }

    return false;
  }

  @Override
  public Label[] getLabels( final Envelope searchExtent )
  {
    final List<Label> query = m_index.query( searchExtent );
    return query.toArray( new Label[query.size()] );
  }
}