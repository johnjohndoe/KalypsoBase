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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileChange;
import org.kalypso.model.wspm.core.profil.IProfileTransaction;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;

public final class PointPropertyEdit implements IProfileChange
{
  protected final IRecord[] m_points;

  protected final int m_index;

  protected final Object[] m_newValues;

  protected final IProfile m_profile;

  public PointPropertyEdit( final IProfile profile, final IRecord p, final IComponent property, final Object newValue )
  {
    this( profile, new IRecord[] { p }, property, new Object[] { newValue } );
  }

  public PointPropertyEdit( final IProfile profile, final IRecord[] p, final IComponent property, final Object newValue )
  {
    this( profile, p, property, fillValues( p, newValue ) );
  }

  public PointPropertyEdit( final IProfile profile, final IRecord[] points, final IComponent property, final Object[] newValues )
  {
    this( profile, points, points[0].getOwner().indexOfComponent( property ), newValues );
  }

  public PointPropertyEdit( final IProfile profile, final IRecord point, final int component, final Object newValues )
  {
    this( profile, new IRecord[] { point }, component, new Object[] { newValues } );
  }

  public PointPropertyEdit( final IProfile profile, final IRecord[] points, final int component, final Object[] newValues )
  {
    m_profile = profile;
    m_points = points;
    m_newValues = newValues;
    m_index = component;
  }

  private static Object[] fillValues( final IRecord[] p, final Object newValue )
  {
    final Object[] newValues = new Object[p.length];
    Arrays.fill( newValues, newValue );
    return newValues;
  }

  protected Object[] m_oldValues;

  @Override
  public IProfileChange doChange( )
  {
    if( m_points.length < 1 || m_index < 0 )
      return new PointPropertyEdit( m_profile, m_points, m_index, m_newValues );

    m_profile.doTransaction( new IProfileTransaction()
    {

      @Override
      public IStatus execute( final IProfile profile )
      {
        m_oldValues = new Object[m_points.length];

        for( int i = 0; i < m_points.length; i++ )
        {
          final IRecord point = m_points[i];
          m_oldValues[i] = point.getValue( m_index );
          point.setValue( m_index, i < m_newValues.length ? m_newValues[i] : Double.NaN );
        }

        return Status.OK_STATUS;
      }
    } );

    return new PointPropertyEdit( m_profile, m_points, m_index, m_oldValues );
  }
}