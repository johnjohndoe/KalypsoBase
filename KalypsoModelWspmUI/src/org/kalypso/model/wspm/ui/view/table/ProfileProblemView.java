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
package org.kalypso.model.wspm.ui.view.table;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.kalypso.contribs.eclipse.jface.action.ActionHyperlink;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.MarkerIndex;
import org.kalypso.model.wspm.ui.i18n.Messages;

/**
 * @author kimwerner
 * @use the whole HeadClientArea to show the Profiles Problemmarkers
 */
public class ProfileProblemView
{
  private final FormToolkit m_toolkit;

  // TODO: put into dialog settings instead
  private final Map<Integer, Boolean> m_expansionState = new HashMap<Integer, Boolean>();

// protected final int m_maxHeight;

  private final ScrolledForm m_form;

  public ProfileProblemView( final FormToolkit toolkit, final Composite parent, final int maxHeight )
  {
    m_toolkit = toolkit;
// m_maxHeight = maxHeight;

    m_form = m_toolkit.createScrolledForm( parent );
    m_form.setExpandVertical( true );

    GridLayoutFactory.swtDefaults().numColumns( 2 ).applyTo( m_form.getBody() );
  }

  private void createSection( final MarkerIndex markerIndex, final IProfil profil, final Composite parent, final int color, final int severity, final String messageKey )
  {
    final IMarker[] markers = markerIndex.get( severity );

    if( markers.length == 0 )
      return;

    if( markers.length == 1 )
    {
      createMarkerControl( profil, parent, color, markers[0] );
      return;
    }

    createMarkerSection( profil, parent, color, messageKey, markers, severity );
  }

  private void createMarkerSection( final IProfil profil, final Composite parent, final int color, final String messageKey, final IMarker[] markers, final int severity )
  {
    final Section section = m_toolkit.createSection( parent, ExpandableComposite.TWISTIE );
    section.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 2, 1 ) );
    section.setLayout( new GridLayout( 2, false ) );
    section.setTitleBarForeground( section.getDisplay().getSystemColor( color ) );
    section.setToggleColor( section.getDisplay().getSystemColor( color ) );

    final String multiMessage = Messages.getString( messageKey, markers.length );
    section.setText( multiMessage );

    final Composite sectionClient = m_toolkit.createComposite( section );
    section.setClient( sectionClient );
    GridLayoutFactory.fillDefaults().numColumns( 2 ).applyTo( sectionClient );

    for( final IMarker marker : markers )
      createMarkerControl( profil, sectionClient, color, marker );

    final Boolean state = m_expansionState.get( severity );
    final boolean expansionState = state == null ? false : state;

    section.setExpanded( expansionState );
    section.addExpansionListener( new ExpansionAdapter()
    {
      @Override
      public void expansionStateChanged( final ExpansionEvent e )
      {
        handleSectionExpanded( severity, e.getState() );
      }
    } );
  }

  protected void handleSectionExpanded( final int severity, final boolean state )
  {
    m_expansionState.put( severity, state );
  }

  private void createMarkerControl( final IProfil profil, final Composite parent, final int color, final IMarker marker )
  {
    final QuickFixAction quickFixAction = new QuickFixAction( marker, profil );
    ActionHyperlink.createHyperlink( m_toolkit, parent, SWT.WRAP, quickFixAction );

    final MarkerInfoAction markerInfoAction = new MarkerInfoAction( marker, profil );
    final ImageHyperlink link = ActionHyperlink.createHyperlink( m_toolkit, parent, SWT.WRAP, markerInfoAction );
    link.setForeground( parent.getDisplay().getSystemColor( color ) );
    link.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
  }

  public final int updateSections( final IProfil profil )
  {
    if( m_form.isDisposed() )
      return -1;

    final Composite body = m_form.getBody();
    ControlUtils.disposeChildren( body );

    if( profil == null )
      return -1;

    final boolean hasSections = createSections( body, profil );
    if( !hasSections )
      return -1;

    m_form.reflow( true );
    final Point size = body.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    return size.y;
  }

  private boolean createSections( final Composite parent, final IProfil profil )
  {
    final MarkerIndex markerIndex = profil.getProblemMarker();
    if( !(markerIndex != null && markerIndex.hasMarkers()) )
      return false;

    createSection( markerIndex, profil, parent, SWT.COLOR_RED, IMarker.SEVERITY_ERROR, "org.kalypso.model.wspm.ui.view.table.ProfileProblemView.3" ); //$NON-NLS-1$
    createSection( markerIndex, profil, parent, SWT.COLOR_DARK_YELLOW, IMarker.SEVERITY_WARNING, "org.kalypso.model.wspm.ui.view.table.ProfileProblemView.4" ); //$NON-NLS-1$
    createSection( markerIndex, profil, parent, SWT.COLOR_DARK_BLUE, IMarker.SEVERITY_INFO, "org.kalypso.model.wspm.ui.view.table.ProfileProblemView.5" ); //$NON-NLS-1$

    return true;
  }

  public Control getControl( )
  {
    return m_form;
  }
}