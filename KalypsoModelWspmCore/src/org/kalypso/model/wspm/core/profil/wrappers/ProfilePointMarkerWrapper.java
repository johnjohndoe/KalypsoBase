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
package org.kalypso.model.wspm.core.profil.wrappers;

import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;

/**
 * @author Dirk Kuch
 */
public class ProfilePointMarkerWrapper extends ProfilePointWrapper implements IProfilPointMarker
{

  private final IProfilPointMarker m_marker;

  public ProfilePointMarkerWrapper( final IProfilPointMarker marker )
  {
    super( marker.getPoint() );

    m_marker = marker;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfilPointMarker#getId()
   */
  @Override
  public IComponent getId( )
  {
    return m_marker.getId();
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfilPointMarker#getPoint()
   */
  @Override
  public IRecord getPoint( )
  {
    return m_marker.getPoint();
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfilPointMarker#setPoint(org.kalypso.observation.result.IRecord)
   */
  @Override
  public IRecord setPoint( final IRecord newPosition )
  {
    return m_marker.setPoint( newPosition );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfilPointMarker#setValue(java.lang.Object)
   */
  @Override
  public void setValue( final Object newValue )
  {
    m_marker.setValue( newValue );

  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfilPointMarker#getValue()
   */
  @Override
  public Object getValue( )
  {
    return m_marker.getValue();
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfilPointMarker#getIntepretedValue()
   */
  @Override
  public Object getIntepretedValue( )
  {
    return m_marker.getIntepretedValue();
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfilPointMarker#setInterpretedValue(java.lang.Object)
   */
  @Override
  public void setInterpretedValue( final Object value )
  {
    m_marker.setInterpretedValue( value );
  }

}
