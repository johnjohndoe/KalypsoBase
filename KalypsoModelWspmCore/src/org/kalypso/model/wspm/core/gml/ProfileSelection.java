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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileListener;
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
      final IProfileSelection profileSelection = ProfileSelection.fromObjectOrNull( itr.next() );
      if( profileSelection != null )
        return profileSelection;
    }

    return new ProfileSelection( null, null );
  }

  private static IProfileSelection fromObjectOrNull( final Object element )
  {
    if( element instanceof IProfileFeature )
      return new ProfileSelection( (IProfileFeature)element, element );

    if( element instanceof IAdaptable )
    {
      final IProfileFeature profile = (IProfileFeature)((IAdaptable)element).getAdapter( IProfileFeature.class );
      if( profile != null )
        return new ProfileSelection( profile, element );
    }

    return null;
  }

  private final Object m_source;

  private final IProfileFeature m_profileFeature;

  /* Fixed reference to the profile, we need to create another sleection object if that changes */
  private final IProfile m_profile;

  /* Fixed reference to the result, we need to create another sleection object if that changes */
  private final Object m_result;

  public ProfileSelection( final IProfileFeature profileFeature, final Object source )
  {
    m_profileFeature = profileFeature;
    m_source = source;
    m_profile = profileFeature == null ? null : profileFeature.getProfile();
    m_result = getResult( profileFeature );
  }

  private static Object getResult( final IProfileFeature profileFeature )
  {
    if( profileFeature == null )
      return null;

    // HACK: If type not set, force it to be the tuhh-profile. We need this, as tuhh-profile are created via
    // the gml-tree which knows nothing about profiles... Everyone else should create profile programatically
    // and directly set the preferred type.
    if( profileFeature.getProfileType() == null )
      profileFeature.setProfileType( "org.kalypso.model.wspm.tuhh.profiletype" ); //$NON-NLS-1$

    return ProfileAndResults.findResultNode( profileFeature );
  }

  @Override
  public IProfileFeature getProfileFeature( )
  {
    return m_profileFeature;
  }

  @Override
  public IProfile getProfile( )
  {
    return m_profile;
  }

  @Override
  public Object getSource( )
  {
    return m_source;
  }

  @Override
  public Object getResult( )
  {
    return m_result;
  }

  @Override
  public boolean isEmpty( )
  {
    return m_profileFeature == null;
  }

  @Override
  public void addProfilListener( final IProfileListener profileListener )
  {
    if( m_profile == null )
      return;

    m_profile.addProfilListener( profileListener );
  }

  @Override
  public void removeProfileListener( final IProfileListener profileListener )
  {
    if( m_profile == null )
      return;

    m_profile.addProfilListener( profileListener );
  }
}