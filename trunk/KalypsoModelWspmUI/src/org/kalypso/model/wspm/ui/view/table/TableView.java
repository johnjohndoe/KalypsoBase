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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.viewers.DefaultTableViewer;
import org.kalypso.contribs.eclipse.swt.custom.ExcelTableCursor;
import org.kalypso.contribs.eclipse.swt.custom.ExcelTableCursor.ADVANCE_MODE;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.gml.IProfileProvider;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileRecord;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.IProfileFeatureSelectionListener;
import org.kalypso.model.wspm.ui.view.ProfileFeatureSeletionHandler;
import org.kalypso.model.wspm.ui.view.chart.IProfilLayerProvider;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.featureview.control.TupleResultTableViewer;
import org.kalypso.ogc.gml.om.table.TupleResultCellModifier;
import org.kalypso.ogc.gml.om.table.TupleResultContentProvider;
import org.kalypso.ogc.gml.om.table.TupleResultLabelProvider;
import org.kalypso.ogc.gml.om.table.command.ITupleResultViewerProvider;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandlerProvider;
import org.kalypso.ogc.gml.selection.IFeatureSelection;

/**
 * TableView für ein Profil. Ist eine feste View auf genau einem Profil.
 * 
 * @author Gernot Belger
 * @author kimwerner
 */
public class TableView extends ViewPart implements ITupleResultViewerProvider, IProfileFeatureSelectionListener
{
  public static final String ID = "org.kalypso.model.wspm.ui.view.table.TableView"; //$NON-NLS-1$

  private static final int INITIAL_PROBLEM_SASH_WEIGHT = 150;

  private static final String SETTINGS_SASH_WEIGHT = "sashWeight"; //$NON-NLS-1$

  private final IDialogSettings m_settings = DialogSettingsUtils.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), ID );

  private Form m_form;

  private FormToolkit m_toolkit;

  protected IProfil m_profile;

  protected DefaultTableViewer m_view;

  private TupleResultContentProvider m_tupleResultContentProvider;

  private TupleResultLabelProvider m_tupleResultLabelProvider;

  private ProfileProblemView m_problemView = null;

  private String m_registeredName;

  protected final UIJob m_markerRefreshJob = new RefreshProfileMarkerJob( this );

  protected final UIJob m_updateSelectionJob = new UpdateSelectionJob( this );

  private final ProfileFeatureSeletionHandler m_handler = new ProfileFeatureSeletionHandler( this );

  // TODO: consider moving this in the content provider: to do this, extends the TupleResultContentProvider to a
  // ProfileContentProvider
  private final IProfilListener m_profileListener = new IProfilListener()
  {
    @Override
    public void onProfilChanged( final ProfilChangeHint hint )
    {
      if( hint.isSelectionChanged() )
      {
        m_updateSelectionJob.cancel();
        m_updateSelectionJob.schedule( 100 );
      }
    }

    @Override
    public void onProblemMarkerChanged( final IProfil source )
    {
      m_markerRefreshJob.cancel();
      m_markerRefreshJob.schedule( 500 );
    }
  };

  private MenuManager m_menuManager;

  private SashForm m_sashForm;

  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );

    m_registeredName = site.getRegisteredName();

    m_handler.doInit( site );

    m_menuManager = new MenuManager();
    m_menuManager.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
  }

  @Override
  public void dispose( )
  {
    super.dispose();

    if( Objects.isNotNull( m_handler ) )
      m_handler.dispose();

    if( m_profile != null )
    {
      m_profile.removeProfilListener( m_profileListener );
    }

    m_menuManager.dispose();
    m_menuManager = null;

    if( m_tupleResultContentProvider != null )
    {
      m_tupleResultContentProvider.dispose();
    }

    if( m_tupleResultLabelProvider != null )
    {
      m_tupleResultLabelProvider.dispose();
    }

    if( m_form != null )
    {
      m_form.dispose();
    }

    m_view = null;
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    final IContextService contextService = (IContextService) getSite().getService( IContextService.class );
    if( contextService != null )
    {
      contextService.activateContext( "org.kalypso.model.wspm.ui.view.table.swt.context" ); //$NON-NLS-1$
    }

    m_toolkit = new FormToolkit( parent.getDisplay() );
    m_form = m_toolkit.createForm( parent );
    m_toolkit.decorateFormHeading( m_form );

    final Composite body = m_form.getBody();
    body.setLayout( new FillLayout() );

    m_sashForm = new SashForm( body, SWT.VERTICAL );
    m_sashForm.setSashWidth( 4 );

    m_problemView = new ProfileProblemView( m_toolkit, m_sashForm, INITIAL_PROBLEM_SASH_WEIGHT );

    m_view = new TupleResultTableViewer( m_sashForm, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION );

    final int savedWeight = DialogSettingsUtils.getInt( m_settings, SETTINGS_SASH_WEIGHT, INITIAL_PROBLEM_SASH_WEIGHT );
    final int problemWeight = Math.min( 900, savedWeight );

    m_sashForm.setWeights( new int[] { problemWeight, 1000 - problemWeight } );

    final ExcelTableCursor cursor = new ExcelTableCursor( m_view, SWT.BORDER_DASH, ADVANCE_MODE.DOWN, true );
    final ControlEditor controlEditor = new ControlEditor( cursor );
    controlEditor.grabHorizontal = true;
    controlEditor.grabVertical = true;

    try
    {
      cursor.setVisible( true );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    try
    {
      cursor.setEnabled( true );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    m_view.getTable().setHeaderVisible( true );
    m_view.getTable().setLinesVisible( true );

    getSite().setSelectionProvider( m_view );
    getSite().registerContextMenu( m_menuManager, m_view );
    m_view.getTable().setMenu( m_menuManager.createContextMenu( m_view.getTable() ) );

    m_view.addPostSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IProfileRecord[] selection = toPoints( event.getSelection() );
        if( ArrayUtils.isNotEmpty( selection ) )
        {
          m_profile.getSelection().setRange( selection );
        }
      }

    } );

    cursor.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final IProfileRecord[] selection = toPoints( m_view.getSelection() );
        if( ArrayUtils.isNotEmpty( selection ) )
        {
          m_profile.getSelection().setRange( selection );
        }
      }
    } );

    m_view.getTable().addControlListener( new ControlAdapter()
    {
      @Override
      public void controlResized( final ControlEvent e )
      {
        handleSashSizeChanged();
      }
    } );

    updateControl();
  }

  protected void handleSashSizeChanged( )
  {
    if( m_sashForm.isDisposed() )
      return;

    final int[] weights = m_sashForm.getWeights();
    m_settings.put( SETTINGS_SASH_WEIGHT, weights[0] );
  }

  @Override
  public void setFocus( )
  {
    if( m_view == null )
      return;

    final Control control = m_view.getControl();
    if( control != null && !control.isDisposed() )
      control.setFocus();
  }

  protected void updateControl( )
  {
    if( m_form == null || m_form.isDisposed() )
      return;

    if( m_profile == null )
    {
      m_form.setMessage( Messages.getString( "org.kalypso.model.wspm.ui.view.table.TableView.2" ), IMessageProvider.INFORMATION ); //$NON-NLS-1$
      setPartName( m_registeredName );

      m_sashForm.setMaximizedControl( m_view.getControl() );
      m_view.getTable().setVisible( false );

      return;
    }

    m_view.getTable().setVisible( true );

    /* Create handlers for this profile */
    setContentDescription( "" ); //$NON-NLS-1$

    final IProfilLayerProvider layerProvider = KalypsoModelWspmUIExtensions.createProfilLayerProvider( m_profile.getType() );
    final IComponentUiHandlerProvider handlerProvider = layerProvider.getComponentUiHandlerProvider( m_profile );
    if( m_view.getContentProvider() != null )
    {
      m_view.setInput( null ); // Reset input in order to avoid double refresh
    }

    setPartName( Messages.getString( "org.kalypso.model.wspm.ui.view.table.TableView.3", m_profile.getStation() ) ); //$NON-NLS-1$
    m_tupleResultContentProvider = new TupleResultContentProvider( handlerProvider );
    m_tupleResultLabelProvider = new TupleResultLabelProvider( m_tupleResultContentProvider );

    m_view.setContentProvider( m_tupleResultContentProvider );
    m_view.setLabelProvider( m_tupleResultLabelProvider );
    m_view.setCellModifier( new TupleResultCellModifier( m_tupleResultContentProvider ) );

    m_view.setInput( m_profile.getResult() );
    m_form.setMessage( null );

    m_view.getControl().getParent().layout();
  }

  @Override
  public void setAdapter( final IWorkbenchPart part, final IFeatureSelection selection )
  {
    m_handler.setAdapter( part, selection );
  }

  @Override
  public void handleProfilProviderChanged( final IProfileProvider provider )
  {
    if( m_profile != null )
      m_profile.removeProfilListener( m_profileListener );

    // TODO: get the profile in the async method call
    m_profile = provider == null ? null : provider.getProfil();

    if( m_profile != null )
    {
      m_profile.addProfilListener( m_profileListener );
    }

    if( m_form != null && !m_form.isDisposed() )
    {
      m_form.getDisplay().asyncExec( new Runnable()
      {
        @Override
        public void run( )
        {
          updateControl();
          updateProblemView();
        }
      } );
    }
  }

  public IProfil getProfil( )
  {
    return m_profile;
  }

  protected final void updateProblemView( )
  {
    if( m_problemView == null )
      return;

    final Control problemControl = m_problemView.getControl();
    if( problemControl == null || problemControl.isDisposed() )
      return;

    final int height = m_problemView.updateSections( m_profile );

// final GridData layoutData = (GridData) problemControl.getLayoutData();

    if( height < 0 )
    {
      m_sashForm.setMaximizedControl( m_view.getControl() );
// layoutData.exclude = true;
// problemControl.setVisible( false );
    }
    else
    {
      m_sashForm.setMaximizedControl( null );
// layoutData.exclude = false;
// problemControl.setVisible( true );
// layoutData.heightHint = Math.min( height, MAX_OUTLINE_HEIGHT );
    }

// m_view.getControl().getParent().layout();
  }

  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( adapter == ITupleResultViewerProvider.class )
      return this;

    return super.getAdapter( adapter );
  }

  @Override
  public TupleResult getTupleResult( )
  {
    if( m_view == null )
      return null;

    return (TupleResult) m_view.getInput();
  }

  @Override
  public TableViewer getTupleResultViewer( )
  {
    return m_view;
  }

  protected IProfileRecord[] toPoints( final ISelection selection )
  {
    if( !(selection instanceof IStructuredSelection) )
      return new IProfileRecord[] {};

    final IStructuredSelection structured = (IStructuredSelection) selection;
    final Iterator< ? > itr = structured.iterator();

    final Set<IProfileRecord> records = new LinkedHashSet<>();

    while( itr.hasNext() )
    {
      final Object object = itr.next();
      if( object instanceof IProfileRecord )
      {
        records.add( (IProfileRecord) object );
      }
      else if( object instanceof IRecord )
      {
        final IRecord record = (IRecord) object;
        final IComponent component = m_profile.hasPointProperty( IWspmConstants.POINT_PROPERTY_BREITE );
        if( Objects.isNull( component ) )
          continue;

        records.add( new ProfileRecord( m_profile, record ) );
      }
    }

    return records.toArray( new IProfileRecord[] {} );
  }
}