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
package org.kalypso.model.wspm.core.profil.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.kalypso.contribs.eclipse.jface.dialog.ListSelectionComposite;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.observation.result.IRecord;

/**
 * Presents all registered profile filters as check-buttons and let the user choose a subset of them.<br>
 * Also acts as a {@link IProfilePointFilter} itself, based on the current choice of filters.
 * 
 * @author Gernot Belger
 */
public class ProfilePointFilterComposite extends ListSelectionComposite implements IProfilePointFilter
{
  // Some commonly used i10n strings
  public static final String STR_GROUP_TEXT = Messages.getString( "org.kalypso.model.wspm.core.profil.filter.ProfilePointFilterComposite.0" ); //$NON-NLS-1$

  private final static String SETTINGS_FILTER_IDS = "settings.filters.ids"; //$NON-NLS-1$

  private static ILabelProvider LABEL_PROVIDER = new ProfilePointFilterLabelProvider();

  private final Collection<IProfilePointFilter> m_filters = new ArrayList<IProfilePointFilter>();

  private IDialogSettings m_dialogSettings;

  /**
   * Same as {@link ProfileFilterComposite#ProfileFilterComposite(new ArrayContentProvider(), ILabelProvider)}
   */
  public ProfilePointFilterComposite( )
  {
    this( new ArrayContentProvider(), LABEL_PROVIDER );
  }

  public ProfilePointFilterComposite( final IStructuredContentProvider contentProvider, final ILabelProvider labelProvider )
  {
    super( contentProvider, labelProvider );

    final IProfilePointFilter[] filters = KalypsoModelWspmCoreExtensions.getProfilePointFilters();
    m_filters.addAll( Arrays.asList( filters ) );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.dialog.ListSelectionComposite#fireCheckStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
   */
  @Override
  protected void fireCheckStateChanged( final CheckStateChangedEvent event )
  {
    super.fireCheckStateChanged( event );

    handleFilterChanged();
  }

  protected void handleFilterChanged( )
  {
    final Object[] checkedElements = getCheckedElements();
    final String[] ids = new String[checkedElements.length];
    for( int i = 0; i < ids.length; i++ )
    {
      final IProfilePointFilter filter = (IProfilePointFilter) checkedElements[i];
      ids[i] = filter.getId();
    }

    if( m_dialogSettings != null )
      m_dialogSettings.put( SETTINGS_FILTER_IDS, ids );
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

  private void setCheckedFilters( final String[] idArray )
  {
    final Set<IProfilePointFilter> checkedFilters = new HashSet<IProfilePointFilter>();
    for( final IProfilePointFilter filter : m_filters )
    {
      if( ArrayUtils.contains( idArray, filter.getId() ) )
        checkedFilters.add( filter );
    }

    setCheckedElements( checkedFilters.toArray( new IProfilePointFilter[checkedFilters.size()] ) );
  }

  /**
   * This implementation accepts a point, if any of the selected filters accepts the point.
   * 
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#accept(org.kalypso.model.wspm.core.profil.IProfil,
   *      org.kalypso.observation.result.IRecord)
   */
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

  public void setDialogSettings( final IDialogSettings dialogSettings )
  {
    m_dialogSettings = dialogSettings;

    if( dialogSettings != null )
    {
      final String[] idArray = dialogSettings.getArray( SETTINGS_FILTER_IDS );
      setCheckedFilters( idArray );
    }

  }

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#getDescription()
   */
  @Override
  public String getDescription( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#getId()
   */
  @Override
  public String getId( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter#getName()
   */
  @Override
  public String getName( )
  {
    throw new UnsupportedOperationException();
  }

}
