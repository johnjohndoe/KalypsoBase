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
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.visitors.FindMinMaxVisitor;
import org.kalypso.model.wspm.core.profil.visitors.ProfileVisitors;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.observation.result.IComponent;

/**
 * @author Dirk Kuch
 */
public class RangeSelection implements IRangeSelection
{
  private IComponent m_activePointProperty;

  private Range<Double> m_selection;

  private final IProfil m_profile;

  public RangeSelection( final IProfil profile )
  {
    m_profile = profile;
  }

  @Override
  public IComponent getActiveProperty( )
  {
    return m_activePointProperty;
  }

  @Override
  public void setActivePointProperty( final IComponent pointProperty )
  {
    m_activePointProperty = pointProperty;
    m_profile.fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.ACTIVE_PROPERTY_CHANGED ) );
  }

  @Override
  public Range<Double> getRange( )
  {
    return m_selection;
  }

  @Override
  public void setRange( final Range<Double> selection )
  {
    if( Objects.equal( m_selection, selection ) )
      return;

    m_selection = selection;
    final ProfilChangeHint hint = new ProfilChangeHint( ProfilChangeHint.SELECTION_CHANGED );
    m_profile.fireProfilChanged( hint );
  }

  @Override
  public IProfileRecord[] toPoints( )
  {
    if( Objects.isNull( m_selection ) )
      return new IProfileRecord[] {};

    return ProfileVisitors.findPointsBetween( m_profile, m_selection, true );
  }

  @Override
  public void setRange( final IProfileRecord... points )
  {
    if( ArrayUtils.isEmpty( points ) )
    {
      m_selection = null;
      m_profile.fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.SELECTION_CHANGED ) );
    }
    else
    {
      final FindMinMaxVisitor visitor = new FindMinMaxVisitor( IWspmConstants.POINT_PROPERTY_BREITE );
      ProfileVisitors.visit( visitor, points );

      setRange( Range.between( visitor.getMinimum().getBreite(), visitor.getMaximum().getBreite() ) );
    }
  }
}
