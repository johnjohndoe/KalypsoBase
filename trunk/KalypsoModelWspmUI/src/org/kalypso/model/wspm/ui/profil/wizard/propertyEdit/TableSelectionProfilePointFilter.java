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
package org.kalypso.model.wspm.ui.profil.wizard.propertyEdit;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.observation.result.IRecord;

/**
 * @author kimwerner
 */
public class TableSelectionProfilePointFilter implements IProfilePointFilter
{
  private final Collection<IRecord> m_points;

  public TableSelectionProfilePointFilter( final ISelection selection )
  {
    m_points = toPoints( selection );
  }

  private Collection<IRecord> toPoints( final ISelection selection )
  {
    final Set<IRecord> result = new HashSet<IRecord>();
    if( selection instanceof IStructuredSelection )
    {
      for( final Iterator< ? > it = ((IStructuredSelection) selection).iterator(); it.hasNext(); )
      {
        final Object element = it.next();
        if( element instanceof IRecord )
          result.add( (IRecord) element );
      }
    }

    return result;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#accept(org.kalypso.model.wspm.core.profil.IProfil,
   *      org.kalypso.model.wspm.core.profil.IProfilPoint)
   */
  public boolean accept( final IProfil profil, final IRecord point )
  {
    return m_points.contains( point );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return "Selected table rows";
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#getId()
   */
  @Override
  public String getId( )
  {
    return getClass().getName();
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#getName()
   */
  @Override
  public String getName( )
  {
    return "Selection";
  }

}
