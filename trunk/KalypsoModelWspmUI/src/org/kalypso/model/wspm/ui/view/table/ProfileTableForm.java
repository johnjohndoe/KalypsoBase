/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.viewers.DefaultTableViewer;
import org.kalypso.contribs.eclipse.swt.custom.ExcelTableCursor;
import org.kalypso.contribs.eclipse.swt.custom.ExcelTableCursor.ADVANCE_MODE;
import org.kalypso.model.wspm.core.gml.IProfileSelection;
import org.kalypso.model.wspm.core.gml.ProfileSelection;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileListener;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.chart.IProfilLayerProvider;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.featureview.control.TupleResultTableViewer;
import org.kalypso.ogc.gml.om.table.TupleResultCellModifier;
import org.kalypso.ogc.gml.om.table.TupleResultContentProvider;
import org.kalypso.ogc.gml.om.table.TupleResultLabelProvider;
import org.kalypso.ogc.gml.om.table.command.ITupleResultViewerProvider;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandlerProvider;

/**
 * Shows a {@link org.kalypso.model.wspm.core.profil.IProfile} in a table viewer.<br/>
 * Also shows 'no profile selected' in a form header as well as the current problem markers of the profile.
 * 
 * @author Gernot Belger
 */
public class ProfileTableForm implements ITupleResultViewerProvider
{
  private static final int INITIAL_PROBLEM_SASH_WEIGHT = 150;

  private static final String SETTINGS_SASH_WEIGHT = "sashWeight"; //$NON-NLS-1$

  private final IProfileListener m_profileListener = new IProfileListener()
  {
    @Override
    public void onProfilChanged( final ProfileChangeHint hint )
    {
      handleProfileChanged( hint );
    }

    @Override
    public void onProblemMarkerChanged( final IProfile source )
    {
      handleProblemMarkerChanged();
    }
  };

  private Form m_form;

  private final IDialogSettings m_settings;

  private DefaultTableViewer m_viewer;

  private TupleResultContentProvider m_tupleResultContentProvider;

  private TupleResultLabelProvider m_tupleResultLabelProvider;

  private ProfileProblemView m_problemView = null;

  private MenuManager m_menuManager;

  private SashForm m_sashForm;

  private boolean m_fireSelectionChanged = true;

  /** The selected profile this table shows. Never <code>null</code> but may be empty. */
  private IProfileSelection m_selection = ProfileSelection.fromSelection( StructuredSelection.EMPTY );

  // FIXME: is is abotu problem or profile markers??
  private final UIJob m_markerRefreshJob = new RefreshProfileMarkerJob( this );

  private final UIJob m_updateSelectionJob = new UpdateSelectionJob( this );

  public ProfileTableForm( final IDialogSettings settings )
  {
    m_settings = settings;
    m_menuManager = new MenuManager();
    m_menuManager.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
  }

  public void dispose( )
  {
    m_selection.removeProfileListener( m_profileListener );

    m_menuManager.dispose();
    m_menuManager = null;

    if( m_tupleResultContentProvider != null )
      m_tupleResultContentProvider.dispose();

    if( m_tupleResultLabelProvider != null )
      m_tupleResultLabelProvider.dispose();

    if( m_form != null )
      m_form.dispose();

    m_viewer = null;
  }

  public void createControl( final FormToolkit toolkit, final Composite parent )
  {
    m_form = toolkit.createForm( parent );
    toolkit.decorateFormHeading( m_form );

    final Composite body = m_form.getBody();
    body.setLayout( new FillLayout() );

    m_sashForm = new SashForm( body, SWT.VERTICAL );
    m_sashForm.setSashWidth( 4 );

    m_problemView = new ProfileProblemView( toolkit, m_sashForm );

    m_viewer = new TupleResultTableViewer( m_sashForm, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION );
    final DefaultTableViewer viewer = m_viewer;

    /* initialize height of problem view */
    final int savedWeight = DialogSettingsUtils.getInt( m_settings, SETTINGS_SASH_WEIGHT, INITIAL_PROBLEM_SASH_WEIGHT );
    final int problemWeight = Math.min( 900, savedWeight );
    m_sashForm.setWeights( new int[] { problemWeight, 1000 - problemWeight } );

    final ExcelTableCursor cursor = new ExcelTableCursor( viewer, SWT.BORDER_DASH, ADVANCE_MODE.DOWN, true );

    final Table table = viewer.getTable();

    table.setHeaderVisible( true );
    table.setLinesVisible( true );

    table.setMenu( m_menuManager.createContextMenu( table ) );
    cursor.setMenu( m_menuManager.createContextMenu( cursor ) );

    viewer.addPostSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        handleTableSelectionChanged( event.getSelection() );
      }
    } );

    // FIXME: check; table selection should follow cursor, and tbel selection should change the profile selection, so this should not be necessary
    cursor.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final IProfileRecord[] selection = toPoints( viewer.getSelection() );
        if( ArrayUtils.isNotEmpty( selection ) )
        {
          getProfil().getSelection().setActivePoints( selection );
        }
      }
    } );

    table.addControlListener( new ControlAdapter()
    {
      @Override
      public void controlResized( final ControlEvent e )
      {
        handleSashSizeChanged();
      }
    } );

    updateControl();
  }

  public TableViewer getViewer( )
  {
    return m_viewer;
  }

  public MenuManager getContextMenuManager( )
  {
    return m_menuManager;
  }

  protected void handleSashSizeChanged( )
  {
    if( m_sashForm.isDisposed() )
      return;

    final int[] weights = m_sashForm.getWeights();
    m_settings.put( SETTINGS_SASH_WEIGHT, weights[0] );
  }

  public void setFocus( )
  {
    if( m_viewer == null )
      return;

    final Control control = m_viewer.getControl();
    if( control != null && !control.isDisposed() )
      control.setFocus();
  }

  IProfile getProfil( )
  {
    return m_selection.getProfile();
  }

  public void setProfile( final IProfileSelection selection )
  {
    m_selection.removeProfileListener( m_profileListener );

    m_selection = selection;

    m_selection.addProfilListener( m_profileListener );

    updateControl();
  }

  /**
   * To be called if the profile reference changes, in this case, the whole table is re-generated.
   */
  protected void updateControl( )
  {
    if( m_form == null || m_form.isDisposed() )
      return;

    if( m_selection.isEmpty() )
    {
      m_form.setMessage( Messages.getString( "org.kalypso.model.wspm.ui.view.table.TableView.2" ), IMessageProvider.INFORMATION ); //$NON-NLS-1$

      /* hie problem section */
      m_sashForm.setMaximizedControl( m_viewer.getControl() );

      /* but hide the table -> gray background */
      m_viewer.getTable().setVisible( false );

      return;
    }

    m_viewer.getTable().setVisible( true );

    /* Create handlers for this profile */
    final IProfile profile = m_selection.getProfileFeature().getProfile();

    final IProfilLayerProvider layerProvider = KalypsoModelWspmUIExtensions.createProfilLayerProvider( profile.getType() );
    final IComponentUiHandlerProvider handlerProvider = layerProvider.getComponentUiHandlerProvider( profile );
    if( m_viewer.getContentProvider() != null )
    {
      // Reset input in order to avoid double refresh
      m_viewer.setInput( null );
    }

    m_tupleResultContentProvider = new TupleResultContentProvider( handlerProvider );
    m_tupleResultLabelProvider = new TupleResultLabelProvider( m_tupleResultContentProvider );

    m_viewer.setContentProvider( m_tupleResultContentProvider );
    m_viewer.setLabelProvider( m_tupleResultLabelProvider );
    m_viewer.setCellModifier( new TupleResultCellModifier( m_tupleResultContentProvider ) );

    m_viewer.setInput( profile.getResult() );
    m_form.setMessage( null );

    updateProblemView();

    // FIXME: really necessary?
    m_viewer.getControl().getParent().layout();
  }

  protected final void updateProblemView( )
  {
    if( m_problemView == null )
      return;

    final Control problemControl = m_problemView.getControl();
    if( problemControl == null || problemControl.isDisposed() )
      return;

    final IProfile profile = m_selection.getProfile();

    final int height = m_problemView.updateSections( profile );
    if( height < 0 )
      m_sashForm.setMaximizedControl( m_viewer.getControl() );
    else
      m_sashForm.setMaximizedControl( null );
  }

  @Override
  public TupleResult getTupleResult( )
  {
    if( m_viewer == null )
      return null;

    return (TupleResult)m_viewer.getInput();
  }

  @Override
  public TableViewer getTupleResultViewer( )
  {
    return m_viewer;
  }

  void disableFireSelectionChanged( )
  {
    m_fireSelectionChanged = false;
  }

  void handleProfileChanged( final ProfileChangeHint hint )
  {
    // FIXME: refresh table viewer!! check hints!
    // but only those stuff, that is not already handled by content provider

    if( (hint.getEvent() & ProfileChangeHint.ACTIVE_POINTS_CHANGED) != 0 )
    {
      m_updateSelectionJob.cancel();
      m_updateSelectionJob.schedule( 100 );
    }
  }

  void handleProblemMarkerChanged( )
  {
    // FIXME: nonsense; problem marker changed, but real markers are refreshed?
    m_markerRefreshJob.cancel();
    m_markerRefreshJob.schedule( 100 );
  }

  void handleTableSelectionChanged( final ISelection selection )
  {
    // new selection was triggered from IProfile Selection Change?
    if( !m_fireSelectionChanged )
    {
      m_fireSelectionChanged = true;
      return;
    }

    final IProfileRecord[] records = toPoints( selection );
    if( ArrayUtils.isNotEmpty( records ) )
    {
      final IProfile profile = m_selection.getProfile();
      profile.getSelection().setActivePoints( records );
    }
  }

  protected IProfileRecord[] toPoints( final ISelection selection )
  {
    if( !(selection instanceof IStructuredSelection) )
      return new IProfileRecord[] {};

    final Set<IProfileRecord> records = new LinkedHashSet<>();

    final IStructuredSelection structured = (IStructuredSelection)selection;

    for( final Iterator< ? > itr = structured.iterator(); itr.hasNext(); )
    {
      final IProfileRecord record = (IProfileRecord)itr.next();
      records.add( record );
    }

    return records.toArray( new IProfileRecord[] {} );
  }
}