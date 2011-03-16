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
package org.kalypso.repository.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.IRepositoryItemVisitor;
import org.kalypso.repository.RepositoryException;

/**
 * @author Dirk Kuch
 */
public final class RepositoryVisitors
{
  private RepositoryVisitors( )
  {
  }

  public static void accept( final IRepositoryItem item, final IRepositoryItemVisitor visitor, final IProgressMonitor monitor, final String job ) throws RepositoryException
  {
    final IRepositoryItem[] children = item.getChildren();
    monitor.beginTask( job, children.length );

    for( final IRepositoryItem child : children )
    {
      if( monitor.isCanceled() )
        return;

      monitor.subTask( String.format( "Zweig: %s", child.getIdentifier() ) );
      if( !visitor.visit( child ) )
        return;

      monitor.worked( 1 );
    }

    monitor.done();
  }

  public static void accept( final IRepositoryItem item, final IRepositoryItemVisitor visitor ) throws RepositoryException
  {
    final IRepositoryItem[] children = item.getChildren();
    for( final IRepositoryItem child : children )
    {
      if( !visitor.visit( child ) )
        break;
    }

  }

}
