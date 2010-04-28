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
package org.kalypso.model.wspm.ui.profil.wizard.propertyEdit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.kalypso.contribs.eclipse.jface.dialog.ListSelectionComposite;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.observation.result.IRecord;

/**
 * Presents all registered profile filters as check-buttons and let the user choose a subset of them.
 * 
 * @author Gernot Belger
 */
public class ProfileFilterComposite extends ListSelectionComposite
{
  private static ILabelProvider LABEL_PROVIDER = new LabelProvider()
  {
    @Override
    public String getText( final Object element )
    {
      return ((IProfilePointFilter) element).getName();
    }
  };

  private final Collection<IProfilePointFilter> m_filters = new ArrayList<IProfilePointFilter>();

  /**
   * Same as {@link ProfileFilterComposite#ProfileFilterComposite(new ArrayContentProvider(), ILabelProvider)}
   */
  public ProfileFilterComposite( )
  {
    this( new ArrayContentProvider(), LABEL_PROVIDER );
  }

  public ProfileFilterComposite( final IStructuredContentProvider contentProvider, final ILabelProvider labelProvider )
  {
    super( contentProvider, labelProvider );

    final IProfilePointFilter[] filters = KalypsoModelWspmCoreExtensions.getProfilePointFilters();
    m_filters.addAll( Arrays.asList( filters ) );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.dialog.ListSelectionComposite#createControl(org.eclipse.swt.widgets.Composite,
   *      int)
   */
  @Override
  public Control createControl( final Composite parent, final int style )
  {
    final Control control = super.createControl( parent, style );

    setInput( m_filters );

    return control;
  }

  public void setCheckedFilters( final String[] idArray )
  {
    final Set<IProfilePointFilter> checkedFilters = new HashSet<IProfilePointFilter>();
    for( final IProfilePointFilter filter : m_filters )
    {
      if( ArrayUtils.contains( idArray, filter.getId() ) )
        checkedFilters.add( filter );
    }

    setCheckedElements( checkedFilters.toArray( new IProfilePointFilter[checkedFilters.size()] ) );
  }

  public boolean accept( final IProfil profil, final IRecord point )
  {
    final Object[] checkedElements = getCheckedElements();
    for( final Object filter : checkedElements )
    {
      if( filter instanceof IProfilePointFilter && ((IProfilePointFilter) filter).accept( profil, point ) )
        return true;
    }
    return false;
  }

  public void addFilter( final IProfilePointFilter filter )
  {
    m_filters.add( filter );
    refresh();
  }

}
