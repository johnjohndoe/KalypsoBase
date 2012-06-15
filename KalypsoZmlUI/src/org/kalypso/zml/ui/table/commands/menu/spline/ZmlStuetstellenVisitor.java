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
package org.kalypso.zml.ui.table.commands.menu.spline;

import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.ZmlValues;
import org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor;

/**
 * @author Dirk Kuch
 */
public class ZmlStuetstellenVisitor implements IZmlModelColumnVisitor
{
  Set<IZmlModelValueCell> m_references = new LinkedHashSet<IZmlModelValueCell>();

  private final DateRange m_dateRange;

  public ZmlStuetstellenVisitor( final IZmlModelValueCell s1, final IZmlModelValueCell s2 ) throws SensorException
  {
    m_dateRange = new DateRange( s1.getIndexValue(), s2.getIndexValue() );
  }

  /**
   * @see org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor#visit(org.kalypso.zml.core.table.model.references.IZmlValueReference)
   */
  @Override
  public void visit( final IZmlModelValueCell reference ) throws SensorException
  {
    if( !m_dateRange.containsLazyInclusive( reference.getIndexValue() ) )
      return;

    if( ZmlValues.isStuetzstelle( reference ) )
      m_references.add( reference );
  }

  public IZmlModelValueCell[] getStuetzstellen( )
  {
    return m_references.toArray( new IZmlModelValueCell[] {} );
  }
}
