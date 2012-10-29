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
package org.kalypso.model.wspm.core.profil.impl.marker;

import org.kalypso.model.wspm.core.profil.IProfilePointMarker;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.TupleResult;

/**
 * @author kimwerner
 */
public class PointMarker implements IProfilePointMarker
{
  private final IComponent m_type;

  IProfileRecord m_point = null;

  public PointMarker( final IComponent typ, final IProfileRecord point )
  {
    if( typ == null || point == null )
      throw new IllegalStateException();

    m_type = typ;
    m_point = point;

    /* type exists in result?!? */
    final TupleResult result = point.getOwner();
    if( !result.hasComponent( typ ) )
    {
      result.addComponent( typ );
    }
  }

  @Override
  public IComponent getComponent( )
  {
    return m_type;
  }

  @Override
  public IProfileRecord getPoint( )
  {
    return m_point;
  }

  @Override
  public Object getValue( )
  {
    final TupleResult owner = m_point.getOwner();
    final int index = owner.indexOfComponent( m_type );
    return m_point.getValue( index );
  }

  /* Interpreted ui values to obtain backward compability */
  @Override
  public Object getIntepretedValue( )
  {
    return getValue();
  }

  @Override
  public void setInterpretedValue( final Object value )
  {
    setValue( value );
  }

  @Override
  public IProfileRecord setPoint( final IProfileRecord newPosition )
  {
    final IProfileRecord oldPoint = m_point;
    if( newPosition != null && newPosition.getOwner() == m_point.getOwner() )
    {

      final TupleResult owner = m_point.getOwner();
      final int index = owner.indexOfComponent( m_type );
      /*
       * get old value of point, change point mapping and set old value to new point and null old point value
       */
      final Object old = m_point.getValue( index );
      m_point.setValue( index, m_type.getDefaultValue() );

      m_point = newPosition;
      m_point.setValue( index, old );
    }

    return oldPoint;
  }

  @Override
  public void setValue( final Object value )
  {
    final TupleResult owner = m_point.getOwner();
    final int index = owner.indexOfComponent( m_type );
    m_point.setValue( index, value );
  }
}
