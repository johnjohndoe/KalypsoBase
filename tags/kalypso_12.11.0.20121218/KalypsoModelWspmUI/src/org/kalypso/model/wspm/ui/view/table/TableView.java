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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.IProfileSelection;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.IProfileSelectionListener;
import org.kalypso.model.wspm.ui.view.ProfileFeatureSeletionHandler;
import org.kalypso.ogc.gml.om.table.command.ITupleResultViewerProvider;
import org.kalypso.ogc.gml.selection.IFeatureSelection;

/**
 * TableView für ein Profil. Ist eine feste View auf genau einem Profil.
 *
 * @author Gernot Belger
 * @author kimwerner
 */
public class TableView extends ViewPart implements IProfileSelectionListener
{
  public static final String ID = "org.kalypso.model.wspm.ui.view.table.TableView"; //$NON-NLS-1$

  private final IDialogSettings m_settings = DialogSettingsUtils.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), ID );

  private final ProfileFeatureSeletionHandler m_handler = new ProfileFeatureSeletionHandler( this );

  private FormToolkit m_toolkit;

  private String m_registeredName;

  private ProfileTableForm m_tableControl;

  private UIJob m_updateControl;

  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );

    m_registeredName = site.getRegisteredName();

    m_handler.doInit( site );
  }

  @Override
  public void dispose( )
  {
    super.dispose();

    if( Objects.isNotNull( m_handler ) )
      m_handler.dispose();

    if( m_tableControl != null )
      m_tableControl.dispose();

    if( m_toolkit != null )
      m_toolkit.dispose();
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    final IContextService contextService = (IContextService)getSite().getService( IContextService.class );
    if( contextService != null )
      contextService.activateContext( "org.kalypso.model.wspm.ui.view.table.swt.context" ); //$NON-NLS-1$

    m_tableControl = new ProfileTableForm( m_settings );

    m_toolkit = new FormToolkit( parent.getDisplay() );

    m_tableControl.createControl( m_toolkit, parent );

    final TableViewer viewer = m_tableControl.getViewer();
    getSite().setSelectionProvider( viewer );

    final MenuManager menuManager = m_tableControl.getContextMenuManager();

    getSite().registerContextMenu( menuManager, viewer );

    final IProfileSelection selection = m_handler.getProfileSelection();
    handleProfilSourceChanged( selection );
  }

  @Override
  public void setFocus( )
  {
    if( m_tableControl == null )
      return;

    m_tableControl.setFocus();
  }

  @Override
  public void setAdapter( final IWorkbenchPart part, final IFeatureSelection selection )
  {
    m_handler.setAdapter( part, selection );
  }

  @Override
  public void handleProfilSourceChanged( final IProfileSelection selection )
  {
    if( m_tableControl == null )
      return;

    /* If no reference changed, do nothing. The table reacts to inner profile changes */
    final IProfile newProfile = selection.getProfile();
    final IProfile oldProfile = m_tableControl.getProfil();
    if( newProfile == oldProfile )
      return;

    if( m_updateControl != null )
      m_updateControl.cancel();

    m_updateControl = new UIJob( "updating table view" ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        if( monitor.isCanceled() )
          return Status.CANCEL_STATUS;

        updateTableControl( selection );

        return Status.OK_STATUS;
      }
    };

    m_updateControl.setSystem( true );
    m_updateControl.setUser( false );

    m_updateControl.schedule( 100 );
  }

  void updateTableControl( final IProfileSelection selection )
  {
    /* update messages */
    if( selection.isEmpty() )
      setPartName( m_registeredName );
    else
    {
      final IProfileFeature profile = selection.getProfileFeature();
      setPartName( Messages.getString( "org.kalypso.model.wspm.ui.view.table.TableView.3", profile.getBigStation() ) ); //$NON-NLS-1$
    }

    m_tableControl.setProfile( selection );
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == ITupleResultViewerProvider.class )
      return m_tableControl;

    return super.getAdapter( adapter );
  }

  public ProfileTableForm getTableControl( )
  {
    return m_tableControl;
  }
}