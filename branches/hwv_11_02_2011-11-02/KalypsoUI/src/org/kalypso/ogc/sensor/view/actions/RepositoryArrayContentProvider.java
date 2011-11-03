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
package org.kalypso.ogc.sensor.view.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.repository.IRepositoryFilter;
import org.kalypso.repository.RepositoriesExtensions;
import org.kalypso.repository.conf.RepositoryFactoryConfig;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * @author Dirk Kuch
 */
public class RepositoryArrayContentProvider extends ArrayContentProvider implements IStructuredContentProvider
{
  private IRepositoryFilter[] m_filters;

  public RepositoryArrayContentProvider( )
  {
    try
    {
      m_filters = RepositoriesExtensions.retrieveRepositoryFilters();
    }
    catch( final CoreException e )
    {
      KalypsoGisPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      m_filters = null;
    }
  }

  /**
   * @see org.eclipse.jface.viewers.ArrayContentProvider#getElements(java.lang.Object)
   */
  @Override
  public Object[] getElements( final Object inputElement )
  {
    if( inputElement instanceof RepositoryFactoryConfig[] )
    {
      final List<RepositoryFactoryConfig> filtered = new ArrayList<RepositoryFactoryConfig>();

      final RepositoryFactoryConfig[] factories = (RepositoryFactoryConfig[]) inputElement;
      for( final RepositoryFactoryConfig factory : factories )
      {

        if( ArrayUtils.isEmpty( m_filters ) )
          filtered.add( factory );
        else
        {
          for( final IRepositoryFilter filter : m_filters )
          {
            if( !filter.select( factory ) )
              continue;

            filtered.add( factory );
          }
        }
      }

      return filtered.toArray( new RepositoryFactoryConfig[] {} );
    }

    return super.getElements( inputElement );
  }
}
