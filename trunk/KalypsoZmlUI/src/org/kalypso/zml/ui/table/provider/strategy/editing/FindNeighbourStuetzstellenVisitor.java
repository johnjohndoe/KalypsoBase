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
package org.kalypso.zml.ui.table.provider.strategy.editing;

import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.references.ZmlValues;
import org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor;
import org.kalypso.zml.ui.table.model.IZmlTableCell;

/**
 * @author Dirk Kuch
 */
public class FindNeighbourStuetzstellenVisitor implements IZmlModelColumnVisitor
{
  private final Integer m_modelIndex;

  private IZmlValueReference m_after;

  private IZmlValueReference m_before;

  public FindNeighbourStuetzstellenVisitor( final IZmlTableCell base )
  {
    final IZmlValueReference reference = base.getValueReference();
    m_modelIndex = reference.getModelIndex();
  }

  /**
   * @see org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor#visit(org.kalypso.zml.core.table.model.visitor.IZmlModelColumnValue)
   */
  @Override
  public boolean visit( final IZmlValueReference reference ) throws SensorException
  {
    if( !ZmlValues.isStuetzstelle( reference ) )
      return true;

    if( reference.getModelIndex() < m_modelIndex )
      m_before = reference;
    else if( reference.getModelIndex() > m_modelIndex && m_after == null )
      m_after = reference;

    return true;
  }

  public IZmlValueReference getBefore( )
  {
    return m_before;
  }

  public IZmlValueReference getAfter( )
  {
    return m_after;
  }

}
