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
package org.kalypso.model.wspm.core.profil.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.kalypso.model.wspm.core.profil.IProfileMetadata;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;

/**
 * @author Holger Albert
 */
class ProfileMetadata implements IProfileMetadata
{
  private final ProfileMetadataObserver m_parent;

  private final Map<String, String> m_metadata;

  private final int m_hintMask;

  public ProfileMetadata( final ProfileMetadataObserver parent, final int hintMask )
  {
    m_parent = parent;
    m_hintMask = hintMask;
    m_metadata = new HashMap<>();
  }

  @Override
  public String[] getKeys( )
  {
    return m_metadata.keySet().toArray( new String[] {} );
  }

  @Override
  public String getMetadata( final String key )
  {
    return m_metadata.get( key );
  }

  @Override
  public void setMetadata( final String key, final String value )
  {
    final String oldValue = getMetadata( key );
    if( ObjectUtils.equals( oldValue, value ) )
      return;

    m_metadata.put( key, value );
    fireProfileChanged();
  }

  @Override
  public String removeMetadata( final String key )
  {
    final String removed = m_metadata.remove( key );
    fireProfileChanged();
    return removed;
  }

  private void fireProfileChanged( )
  {
    if( m_parent == null )
      return;

    /* Create the profile change hint. */
    final ProfileChangeHint hint = new ProfileChangeHint( m_hintMask );

    /* Fire the profile changed event. */
    m_parent.fireProfilChanged( hint );
  }
}