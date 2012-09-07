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
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.ui.i18n.Messages;
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
    final Set<IRecord> result = new HashSet<>();
    if( selection instanceof IStructuredSelection )
    {
      for( final Iterator< ? > it = ((IStructuredSelection) selection).iterator(); it.hasNext(); )
      {
        final Object element = it.next();
        if( element instanceof IRecord )
        {
          result.add( (IRecord) element );
        }
      }
    }

    return result;
  }

  @Override
  public boolean accept( final IProfil profil, final IProfileRecord point )
  {
    return m_points.contains( point );
  }

  @Override
  public String getDescription( )
  {
    return Messages.getString( "TableSelectionProfilePointFilter.0" ); //$NON-NLS-1$
  }

  @Override
  public String getId( )
  {
    return getClass().getName();
  }

  @Override
  public String getName( )
  {
    return Messages.getString( "TableSelectionProfilePointFilter.1" ); //$NON-NLS-1$
  }

  @Override
  public String getUsageHint( )
  {
    return "tableSelection"; //$NON-NLS-1$
  }

}
