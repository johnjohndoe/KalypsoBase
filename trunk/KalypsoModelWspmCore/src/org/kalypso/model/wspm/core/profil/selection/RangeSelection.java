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
package org.kalypso.model.wspm.core.profil.selection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.changes.ActiveObjectEdit;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileWrapper;
import org.kalypso.observation.result.IComponent;

/**
 * @author Dirk Kuch
 */
public class RangeSelection implements IRangeSelection
{
  private final ProfileWrapper m_profile;

  private IProfileRecord m_activePoint;

  private IComponent m_activePointProperty;

  public RangeSelection( final IProfil profile )
  {
    m_profile = new ProfileWrapper( profile );
  }

  /**
   * @return the active point.
   */
  @Override
  public IProfileRecord getActivePoint( )
  {
    final IProfileRecord[] points = m_profile.getPoints();
    if( ArrayUtils.isEmpty( points ) )
      return null;

    return (IProfileRecord) Objects.firstNonNull( m_activePoint, points[0] );
  }

  @Override
  public IComponent getActiveProperty( )
  {
    return m_activePointProperty;
  }

  @Override
  public void setActivePoint( final IProfileRecord point )
  {
    if( m_activePoint == point )
      return;

    m_activePoint = point;
    final ProfilChangeHint hint = new ProfilChangeHint();
    hint.setActivePointChanged();

    final IProfil profile = m_profile.getProfile();
    profile.fireProfilChanged( hint, new IProfilChange[] { new ActiveObjectEdit( profile, point, m_activePointProperty ) } );
  }

  @Override
  public void setActivePointProperty( final IComponent pointProperty )
  {
    m_activePointProperty = pointProperty;
    final ProfilChangeHint hint = new ProfilChangeHint();
    hint.setActivePropertyChanged( true );

    final IProfil profile = m_profile.getProfile();
    profile.fireProfilChanged( hint, new IProfilChange[] { new ActiveObjectEdit( profile, m_activePoint, m_activePointProperty ) } );
  }

  @Override
  public Range<Double> getRange( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setRange( final Double p0, final Double pn )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public IProfileRecord[] toPoints( )
  {
    // TODO Auto-generated method stub
    return null;
  }

}
