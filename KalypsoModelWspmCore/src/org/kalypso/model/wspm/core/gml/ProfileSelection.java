/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.core.gml;

import java.util.Iterator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.result.ProfileAndResults;

/**
 * The source of a profile selection: the {@link org.kalypso.model.wspm.core.gml.IProfileFeature} and its origin.
 *
 * @author Gernot Belger
 */
public class ProfileSelection implements IProfileSelection
{
  public static IProfileSelection fromSelection( final IStructuredSelection selection )
  {
    for( final Iterator< ? > itr = selection.iterator(); itr.hasNext(); )
    {
      final IProfileSelection profileAndSource = ProfileSelection.fromObject( itr.next() );
      if( profileAndSource != null )
        return profileAndSource;
    }

    return null;
  }

  public static IProfileSelection fromObject( final Object element )
  {
    if( element instanceof IProfileFeature )
      return new ProfileSelection( (IProfileFeature)element, element );

    if( element instanceof IAdaptable )
    {
      final IProfileFeature profile = (IProfileFeature)((IAdaptable)element).getAdapter( IProfileFeature.class );
      return new ProfileSelection( profile, element );
    }

    return null;
  }

  private final Object m_source;

  private final IProfileFeature m_profileFeature;

  private ProfileSelection( final IProfileFeature profile, final Object source )
  {
    m_profileFeature = profile;
    m_source = source;
  }

  @Override
  public int hashCode( )
  {
    return m_source.hashCode();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj == null )
      return false;

    if( obj == this )
      return true;

    if( obj.getClass() != getClass() )
      return false;

    final ProfileSelection other = (ProfileSelection)obj;

    return new EqualsBuilder().append( m_source, other.m_source ).isEquals();
  }

  @Override
  public IProfileFeature getProfileFeature( )
  {
    return m_profileFeature;
  }

  public IProfile getProfile( )
  {
    return m_profileFeature.getProfile();
  }

  @Override
  public Object getSource( )
  {
    return m_source;
  }

  @Override
  public Object getResult( )
  {
    // HACK: If type not set, force it to be the tuhh-profile. We need this, as tuhh-profile are created via
    // the gml-tree which knows nothing about profiles... Everyone else should create profile programatically
    // and directly set the preferred type.
    if( m_profileFeature.getProfileType() == null )
      m_profileFeature.setProfileType( "org.kalypso.model.wspm.tuhh.profiletype" ); //$NON-NLS-1$

    return ProfileAndResults.findResultNode( m_profileFeature );
  }
}