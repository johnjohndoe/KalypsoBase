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

package org.kalypso.ui.wizard.gml;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.commons.databinding.swt.WorkspaceFileBinding;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.ui.editor.gmleditor.part.GMLContentProvider;
import org.kalypso.ui.editor.gmleditor.part.GMLLabelProvider;
import org.kalypso.ui.i18n.Messages;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * @author Kuepferle
 */
public class GmlFileImportPage extends WizardPage
{
  private final PropertyChangeListener m_gmlFileListener = new PropertyChangeListener()
  {
    @Override
    public void propertyChange( final PropertyChangeEvent evt )
    {
      handleGmlFileChanged();
    }
  };

  // FIXME: tree should expand when input is changed
  // private final static int DEFAULT_EXPANSION_LEVEL = 3;

  private final GmlFileImportData m_data;

  private DatabindingWizardPage m_binding;

  public GmlFileImportPage( final String pageName, final String title, final GmlFileImportData data )
  {
    super( pageName );

    setTitle( title );

    m_data = data;
  }

  @Override
  public void createControl( final Composite parent )
  {
    m_binding = new DatabindingWizardPage( this, null );

    final Composite topComposite = new Composite( parent, SWT.NULL );
    topComposite.setLayout( new GridLayout() );
    topComposite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

    final Control fileControl = createFileGroup( topComposite );
    fileControl.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );

    final Control treeControl = createTreeView( topComposite );
    treeControl.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    setControl( topComposite );
    setPageComplete( false );

    m_data.getGmlFile().addPropertyChangeListener( FileAndHistoryData.PROPERTY_PATH, m_gmlFileListener );
  }

  private Control createTreeView( final Composite composite )
  {
    final TreeViewer treeViewer = new TreeViewer( composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );

    final GMLContentProvider contentProvider = new GMLContentProvider( true );

    final GMLXPath rootPath = m_data.getRootPath();
    if( rootPath != null )
      contentProvider.setRootPath( rootPath );

    treeViewer.setContentProvider( contentProvider );
    treeViewer.setLabelProvider( new GMLLabelProvider() );
    treeViewer.setUseHashlookup( true );

    /* binding */
    final IObservableValue targetInput = ViewersObservables.observeInput( treeViewer );
    final IObservableValue modelInput = BeansObservables.observeValue( m_data, GmlFileImportData.PROPERTY_WORKSPACE );
    m_binding.bindValue( targetInput, modelInput );

    final IViewerObservableValue targetSelection = ViewersObservables.observeSinglePostSelection( treeViewer );
    final IObservableValue modelSelection = BeansObservables.observeValue( m_data, GmlFileImportData.PROPERTY_SELECTION );
    m_binding.bindValue( targetSelection, modelSelection, new GmlFileSelectionValidator( m_data ) );

    return treeViewer.getControl();
  }

  private Control createFileGroup( final Composite parent )
  {
    final Group group = new Group( parent, SWT.NULL );
    GridLayoutFactory.swtDefaults().numColumns( 3 ).equalWidth( false ).applyTo( group );

    group.setText( Messages.getString( "org.kalypso.ui.wizard.gmlGmlFileImportPage.0" ) ); //$NON-NLS-1$

    final Label fileLabel = new Label( group, SWT.NONE );
    fileLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
    fileLabel.setText( Messages.getString( "org.kalypso.ui.wizard.gmlGmlFileImportPage.1" ) ); //$NON-NLS-1$

    final FileAndHistoryData gmlFileData = m_data.getGmlFile();

    final IObservableValue fileTarget = BeansObservables.observeValue( gmlFileData, FileAndHistoryData.PROPERTY_PATH );
    final IObservableValue historyTarget = BeansObservables.observeValue( gmlFileData, FileAndHistoryData.PROPERTY_HISTORY );

    final String dialogMessage = Messages.getString( "org.kalypso.ui.wizard.gmlGmlFileImportPage.3" ); //$NON-NLS-1$
    final WorkspaceFileBinding fileBinding = new WorkspaceFileBinding( m_binding, fileTarget, dialogMessage, new String[] { "gml" } ); //$NON-NLS-1$

    final IProject selectedProject = m_data.getSelectedProject();
    if( selectedProject != null )
      fileBinding.setInputContainer( selectedProject );

    final ViewerFilter filter = m_data.getFilter();
    if( filter != null )
      fileBinding.setFilter( filter );

    final Control fileControl = fileBinding.createFileFieldWithHistory( group, historyTarget );
    fileControl.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    fileBinding.createFileSearchButton( group );

    return group;
  }

  void handleGmlFileChanged( )
  {
    final ICoreRunnableWithProgress operation = new GmlFileImportLoadOperation( m_data );

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, false, operation );
    if( !status.isOK() )
      StatusDialog.open( getShell(), status, getWizard().getWindowTitle() );
  }

  public void setViewerFilter( final ViewerFilter filter )
  {
    m_data.setViewerFilter( filter );
  }
}