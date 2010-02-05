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
package org.kalypso.repository;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.repository.container.DefaultRepositoryContainer;
import org.kalypso.repository.container.IRepositoryContainer;

/**
 * @author Dirk Kuch
 */
public final class RepositoryContainerSingelton
{
  private static RepositoryContainerSingelton INSTANCE = null;

  private final IRepositoryContainer m_container = new DefaultRepositoryContainer();

  private RepositoryContainerSingelton( )
  {
    init();
  }

  private void init( )
  {
    try
    {
      final IRepositoryResolver[] resolvers = RepositoriesExtensions.retrieveTimeSeriesBrowserRepositories();
      for( final IRepositoryResolver resolver : resolvers )
      {
        m_container.addRepository( resolver.getRepository() );
      }
    }
    catch( final CoreException e )
    {
      KalypsoRepository.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  public static RepositoryContainerSingelton getInstance( )
  {
    if( INSTANCE == null )
      INSTANCE = new RepositoryContainerSingelton();

    return INSTANCE;
  }

  public IRepositoryContainer getContainer( )
  {
    return m_container;
  }
}
