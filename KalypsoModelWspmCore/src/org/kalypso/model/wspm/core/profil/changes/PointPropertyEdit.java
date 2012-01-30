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
package org.kalypso.model.wspm.core.profil.changes;

import java.util.Arrays;

import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;

public final class PointPropertyEdit implements IProfilChange
{
  private final IRecord[] m_points;

  private final int m_index;

  private final Object[] m_newValues;

  public PointPropertyEdit( final IRecord p, final IComponent property, final Object newValue )
  {
    this( new IRecord[] { p }, property, new Object[] { newValue } );
  }

  public PointPropertyEdit( final IRecord[] p, final IComponent property, final Object newValue )
  {
    this( p, property, fillValues( p, newValue ) );
  }

  private static Object[] fillValues( final IRecord[] p, final Object newValue )
  {
    final Object[] newValues = new Object[p.length];
    Arrays.fill( newValues, newValue );
    return newValues;
  }

  public PointPropertyEdit( final IRecord[] points, final IComponent property, final Object[] newValues )
  {
    this( points, points[0].getOwner().indexOfComponent( property ), newValues );
  }

  public PointPropertyEdit( final IRecord point, final int component, final Object newValues )
  {
    this( new IRecord[] { point }, component, new Object[] { newValues } );
  }

  public PointPropertyEdit( final IRecord[] points, final int component, final Object[] newValues )
  {
    m_points = points;
    m_newValues = newValues;
    m_index = component;
  }

  @Override
  public void configureHint( final ProfilChangeHint hint )
  {
    hint.setPointValuesChanged();
  }

  @Override
  public IProfilChange doChange( )
  {
    if( m_points.length < 1 || m_index < 0 )
      return new PointPropertyEdit( m_points, m_index, m_newValues );

    final Object[] oldValues = new Object[m_points.length];

    for( int i = 0; i < m_points.length; i++ )
    {
      final IRecord point = m_points[i];
      oldValues[i] = point.getValue( m_index );
      point.setValue( m_index, i < m_newValues.length ? m_newValues[i] : Double.NaN, i < m_points.length - 1 );
    }

    return new PointPropertyEdit( m_points, m_index, oldValues );
  }
}