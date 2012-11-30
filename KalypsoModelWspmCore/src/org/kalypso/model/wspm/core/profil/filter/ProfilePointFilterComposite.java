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

import org.apache.commons.lang3.ArrayUtils;
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
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;

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

  private static final String SETTINGS_FILTER_IDS = "settings.filters.ids"; //$NON-NLS-1$

  private static ILabelProvider LABEL_PROVIDER = new ProfilePointFilterLabelProvider();

  private final Collection<IProfilePointFilter> m_filters = new ArrayList<>();

  private IDialogSettings m_dialogSettings;

  private final String m_usageHint;

  /**
   * Same as {@link ProfileFilterComposite#ProfileFilterComposite(new ArrayContentProvider(), LABEL_PROVIDER, null)}
   */
  public ProfilePointFilterComposite( )
  {
    this( new ArrayContentProvider(), LABEL_PROVIDER, null );
  }

  /**
   * Same as {@link ProfileFilterComposite#ProfileFilterComposite(new ArrayContentProvider(), ILabelProvider)}
   */
  public ProfilePointFilterComposite( final String useageHint )
  {
    this( new ArrayContentProvider(), LABEL_PROVIDER, useageHint );
  }

  private ProfilePointFilterComposite( final IStructuredContentProvider contentProvider, final ILabelProvider labelProvider, final String usageHint )
  {
    super( contentProvider, labelProvider );

    m_usageHint = usageHint;

    final IProfilePointFilter[] filters = KalypsoModelWspmCoreExtensions.getProfilePointFilters( usageHint );
    m_filters.addAll( Arrays.asList( filters ) );
  }

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
    {
      m_dialogSettings.put( SETTINGS_FILTER_IDS, ids );
    }
  }

  @Override
  public Control createControl( final Composite parent, final int style )
  {
    final Control control = super.createControl( parent, style );

    setInput( m_filters );

    return control;
  }

  private void setCheckedFilters( final String[] idArray )
  {
    final Set<IProfilePointFilter> checkedFilters = new HashSet<>();
    for( final IProfilePointFilter filter : m_filters )
    {
      if( ArrayUtils.contains( idArray, filter.getId() ) )
      {
        checkedFilters.add( filter );
      }
    }

    setCheckedElements( checkedFilters.toArray( new IProfilePointFilter[checkedFilters.size()] ) );
  }

  /**
   * This implementation accepts a point, if any of the selected filters accepts the point.
   */
  @Override
  public boolean accept( final IProfile profil, final IProfileRecord point )
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

  @Override
  public String getUsageHint( )
  {
    return m_usageHint;
  }

  @Override
  public String getDescription( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getId( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName( )
  {
    throw new UnsupportedOperationException();
  }

  public IProfileRecord[] getSelectedPoints( final IProfile profile )
  {
    final IProfileRecord[] points = profile.getPoints();
    final Object[] checkedElements = getCheckedElements();
    if( checkedElements.length == 0 )
      return points;

    final Collection<IProfileRecord> filteredPoints = new ArrayList<>( points.length );
    for( final IProfileRecord point : points )
    {
      if( accept( profile, point ) )
      {
        filteredPoints.add( point );
      }
    }

    return filteredPoints.toArray( new IProfileRecord[filteredPoints.size()] );
  }

}
