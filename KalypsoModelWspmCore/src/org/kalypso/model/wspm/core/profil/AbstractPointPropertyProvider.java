/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.model.wspm.core.profil;

import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.TupleResult;

/**
 * @author kimwerner
 */
public abstract class AbstractPointPropertyProvider implements IProfilPointPropertyProvider
{
  protected final Set<String> m_properties = new LinkedHashSet<String>();

  protected final Set<String> m_markers = new LinkedHashSet<String>();

  @Override
  public IProfil createProfil( )
  {
    return createProfil( new TupleResult(), null );
  }

  @Override
  public Object getDefaultValue( final String propertyID )
  {
    final IComponent component = getPointProperty( propertyID );
    if( component == null )
      return null;
    return component.getDefaultValue();
  }

  @Override
  public String[] getPointProperties( )
  {
    return m_properties.toArray( new String[] {} );
  }

  @Override
  public boolean isMarker( final String markerID )
  {
    return m_markers.contains( markerID );
  }

  @Override
  public boolean providesPointProperty( final String property )
  {
    // FIXME: why has this been set to true?
    return true;
// return m_properties.contains( property );
  }

  /**
   * @throws IllegalArgumentException
   */
  @Override
  public void checkComponents( final TupleResult result )
  {
    final IComponent[] components = result.getComponents();
    for( final IComponent component : components )
    {
      if( !providesPointProperty( component.getId() ) )
        throw new IllegalArgumentException( "unknown component: " + component.getName() ); //$NON-NLS-1$
    }
  }
}