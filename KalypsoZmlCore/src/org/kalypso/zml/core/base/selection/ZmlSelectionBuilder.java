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
package org.kalypso.zml.core.base.selection;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.zml.core.base.IMultipleZmlSourceElement;
import org.kalypso.zml.core.base.obsprovider.RepositoryItemZmlSelectionBuilder;

/**
 * @author Dirk Kuch
 */
public final class ZmlSelectionBuilder
{
  private ZmlSelectionBuilder( )
  {

  }

  public static IMultipleZmlSourceElement[] getSelection( final SelectionChangedEvent event )
  {
    return getSelection( (IStructuredSelection) event.getSelection() );

  }

  private static IMultipleZmlSourceElement[] getSelection( final IStructuredSelection selection )
  {
    final Iterator< ? > iterator = selection.iterator();

    final Set<IRepositoryItem> items = new LinkedHashSet<>();

    while( iterator.hasNext() )
    {
      final Object obj = iterator.next();
      if( obj instanceof IRepositoryItem )
      {
        final IRepositoryItem item = (IRepositoryItem) obj;
        if( item.hasAdapter( IObservation.class ) )
        {
          items.add( item );
        }
      }
    }

    final RepositoryItemZmlSelectionBuilder selectionBuilder = new RepositoryItemZmlSelectionBuilder();
    return selectionBuilder.toSelection( items.toArray( new IRepositoryItem[] {} ) );
  }

}
