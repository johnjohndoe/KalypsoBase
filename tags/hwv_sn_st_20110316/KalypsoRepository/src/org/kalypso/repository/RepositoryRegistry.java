/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.repository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gernot Belger
 * @author Dirk Kuch
 */
public class RepositoryRegistry implements IRepositoryRegistry
{
  private final Map<String, IRepository> m_repositories = new HashMap<String, IRepository>();

  /**
   * @see org.kalypso.repository.IRepositoryRegistry#getRepository(java.lang.String)
   */
  @Override
  public IRepository getRepository( final String identifier )
  {
    // TODO: evtl. default zurückgeben für bestimmte protokolle wie file, platform, http etc. Ggfs. an strategy
    // delegieren

    final String protocol = getProtocol( identifier );
    return m_repositories.get( protocol );
  }

  private String getProtocol( final String protocol )
  {
    if( protocol.endsWith( "://" ) )
      return protocol.substring( 0, protocol.length() - 3 );

    return protocol;
  }

  /**
   * @see org.kalypso.repository.IRepositoryRegistry#registerProtocol(org.kalypso.repository.IRepository)
   */
  @Override
  public void registerProtocol( final IRepository repository )
  {
    final String identifier = repository.getIdentifier();
    final String protocol = getProtocol( identifier );
    m_repositories.put( protocol, repository );
  }

  public IRepository[] getRepositories( )
  {
    return m_repositories.values().toArray( new IRepository[] {} );
  }
}
