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
package org.kalypso.model.wspm.core.profil.visitors;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecordVisitor;

/**
 * @author Dirk Kuch
 */
public class FindMinMaxVisitor implements IProfileRecordVisitor
{
  IProfileRecord m_min;

  IProfileRecord m_max;

  private final String m_property;

  public FindMinMaxVisitor( final String property )
  {
    m_property = property;
  }

  @Override
  public void visit( final IProfileRecord point, final int searchDirection )
  {
    final int property = point.indexOfComponent( m_property );
    if( Objects.isNull( property ) )
      return;

    final Object value = point.getValue( property );
    if( !(value instanceof Number) )
      return;

    final Number number = (Number) value;

    if( Objects.isNull( m_min ) )
      m_min = point;
    else
    {
      final Number minValue = (Number) m_min.getValue( property );

      if( number.doubleValue() < minValue.doubleValue() )
        m_min = point;
    }

    if( Objects.isNull( m_max ) )
      m_max = point;
    else
    {
      final Number maxValue = (Number) m_max.getValue( property );

      if( number.doubleValue() > maxValue.doubleValue() )
        m_max = point;
    }
  }

  public IProfileRecord getMinimum( )
  {
    return m_min;
  }

  public IProfileRecord getMaximum( )
  {
    return m_max;
  }

  @Override
  public boolean isWriter( )
  {
    return false;
  }

}
