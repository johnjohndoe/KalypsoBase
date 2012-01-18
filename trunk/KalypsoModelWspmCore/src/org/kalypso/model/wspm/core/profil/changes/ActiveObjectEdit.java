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

import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.observation.result.IComponent;

public class ActiveObjectEdit implements IProfilChange
{
  private final IComponent m_property;

  private final IProfileRecord m_point;

  private final IProfil m_profil;

  public ActiveObjectEdit( final IProfil profil, final IProfileRecord point, final IComponent property )
  {
    m_profil = profil;
    m_property = property;
    m_point = point;
  }

  @Override
  public void configureHint( final ProfilChangeHint hint )
  {
    hint.setActivePointChanged();
  }

  @Override
  public IProfilChange doChange( )
  {
    final IProfileRecord oldPoint = m_profil.getSelection().getActivePoint();
    final IComponent oldProperty = m_profil.getSelection().getActiveProperty();

    m_profil.getSelection().setActivePoint( m_point );
    m_profil.getSelection().setActivePointProperty( m_property );

    return new ActiveObjectEdit( m_profil, oldPoint, oldProperty == null ? null : oldProperty );
  }

  @Override
  public String toString( )
  {
    return m_property.toString();
  }
}
