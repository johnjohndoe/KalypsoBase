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
package de.openali.odysseus.chart.framework.model.layer.manager.visitors;

import java.util.LinkedHashSet;
import java.util.Set;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor2;

/**
 * @author Dirk Kuch
 */
public class EditableChartLayerVisitor implements IChartLayerVisitor2
{
  private final Set<IEditableChartLayer> m_layers = new LinkedHashSet<IEditableChartLayer>();

  @Override
  public boolean getVisitDirection( )
  {
    // anticyclic to the paint direction, so top layers are edited first.
    return true;
  }

  @Override
  public boolean visit( final IChartLayer layer )
  {
    if( layer instanceof IEditableChartLayer )
    {
      final IEditableChartLayer editable = (IEditableChartLayer) layer;
      if( !editable.isLocked() && editable.isVisible() )
        m_layers.add( editable );
    }

    return true;
  }

  public IEditableChartLayer[] getLayers( )
  {
    return m_layers.toArray( new IEditableChartLayer[] {} );
  }
}