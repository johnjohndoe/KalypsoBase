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
package org.kalypsodeegree_impl.model.feature;

import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;

final class RegisterVisitor implements FeatureVisitor
{
  private final GMLWorkspace_Impl m_workspace;

  public RegisterVisitor( final GMLWorkspace_Impl workspace )
  {
    m_workspace = workspace;
  }

  @Override
  public boolean visit( final Feature f )
  {
    final String id = f.getId();

    // ACHTUNG!!! bitte BEIDE Zeilen ein- und auskommentieren!
    // if( m_indexMap.containsKey( id ) )
    // System.out.println( "Workspace already contains a feature with id: " +
    // id );

    // if( !(f instanceof IXLinkedFeature) && (id == null || id.length() == 0 ))
    if( id == null || id.length() == 0 )
    {
      // FIXME: should never happen!
      // id = m_workspace.createFeatureId( f.getFeatureType() );
      System.out.println( "Feature has no id: " + f );
      // f.setId( id );
    }

    // TODO: better generate new ids and remember wich ones are generated (because
    // we dont want to write the generated ones)
    // IDEA: put an prefix before the generated id (a 'strong' prefix which will not be in any other id)
    // When writing the gml, we then can quickly determine if the id is generated

    // TODO: do not put null-ids into this map ? What sideeffects do we expect
    m_workspace.register( f );
    return true;
  }
}