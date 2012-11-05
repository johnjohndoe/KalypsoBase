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
package org.kalypso.model.wspm.core.profil.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.core.profil.visitors.FindMinMaxVisitor;
import org.kalypso.model.wspm.core.profil.visitors.ProfileVisitors;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileRecord;
import org.kalypso.observation.result.IComponent;

/**
 * @author Dirk Kuch
 */
public class RangeSelection implements IRangeSelection
{
  private IComponent m_activePointProperty;

  private Range<Double> m_selection;

  private final AbstractProfile m_profile;

  private Double m_cursor;

  public RangeSelection( final AbstractProfile profile )
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
    m_profile.fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.ACTIVE_PROPERTY_CHANGED ) );
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

    int selectionCount = 0;
    for( final IProfileRecord record : m_profile.getPoints() )
    {
      final boolean isSelected = selection.contains( record.getBreite() );
      ((ProfileRecord)record).setSelected( isSelected );

      if( isSelected )
        selectionCount++;
    }

    if( selection.getMinimum() == selection.getMaximum() && selectionCount == 0 )
    {
      final IProfileRecord previous = m_profile.findPreviousPoint( selection.getMinimum() );
      if( previous != null )
        ((ProfileRecord)previous).setSelected( !selection.contains( previous.getBreite() ) );
    }

    final ProfileChangeHint hint = new ProfileChangeHint( ProfileChangeHint.SELECTION_CHANGED | ProfileChangeHint.ACTIVE_POINTS_CHANGED );
    m_profile.fireProfilChanged( hint );
  }

  @Override
  public IProfileRecord[] toPoints( )
  {
    if( Objects.isNull( m_selection ) )
      return new IProfileRecord[] {};

    return ProfileVisitors.findPointsBetween( m_profile, m_selection, true );
  }

  public final void setActivePointsInternal( final IProfileRecord... points )
  {
    for( final IProfileRecord record : m_profile.getPoints() )
      ((ProfileRecord)record).setSelected( false );

    for( final IProfileRecord record : points )
      ((ProfileRecord)record).setSelected( true );

    m_profile.fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.ACTIVE_POINTS_CHANGED ) );
  }

  @Override
  public void setActivePoints( final IProfileRecord... points )
  {
    if( ArrayUtils.getLength( points ) == 1 )
    {
      final IProfileRecord point = points[0];
      final Double width = point.getBreite();
      if( Objects.isNull( width ) )
        return;
      setRange( Range.is( width ) );

      setActivePointsInternal( point );
    }
    else
    {
      setActivePointsInternal( points );

      final FindMinMaxVisitor visitor = new FindMinMaxVisitor( IWspmConstants.POINT_PROPERTY_BREITE );
      ProfileVisitors.visit( visitor, points );

      final Double min = visitor.getMinimum().getBreite();
      final Double max = visitor.getMaximum().getBreite();
      if( Objects.allNull( min, max ) )
        return;
      else if( Objects.isNull( min, max ) )
        setRange( Range.is( Objects.firstNonNull( min, max ) ) );
      else
        setRange( Range.between( min, max ) );
    }
  }

  @Override
  public boolean isEmpty( )
  {
    return Objects.isNull( m_selection );
  }

  @Override
  public void setCursor( final Double breite )
  {
    m_cursor = breite;
    m_profile.fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.SELECTION_CURSOR_CHANGED ) );
  }

  @Override
  public Double getCursor( )
  {
    return m_cursor;
  }
}