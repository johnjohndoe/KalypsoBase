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
package org.kalypso.gml.ui.internal.coverage.imports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.commons.databinding.swt.FileBinding;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserDelegateOpen;
import org.kalypso.gml.ui.KalypsoGmlUiExtensions;
import org.kalypso.gml.ui.coverage.CoverageManagementAction;
import org.kalypso.gml.ui.coverage.ImportCoverageData;
import org.kalypso.gml.ui.coverage.imports.CoverageFormats;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.transformation.ui.CRSSelectionPanel;

/**
 * @author Dirk Kuch
 * @author Gernot Belger
 */
public class PageSelectGeodataFiles extends WizardPage
{
  private final ImportCoverageData m_data;

  private DatabindingWizardPage m_binding;

  protected final Map<CoverageManagementAction, Boolean> m_checkedActions;

  public PageSelectGeodataFiles( final ImportCoverageData data )
  {
    super( "pageSelect" ); //$NON-NLS-1$

    m_data = data;
    m_binding = null;
    m_checkedActions = new HashMap<>();
  }

  @Override
  public void createControl( final Composite parent )
  {
    m_binding = new DatabindingWizardPage( this, null );

    final Composite container = new Composite( parent, SWT.NULL );
    GridLayoutFactory.swtDefaults().applyTo( container );
    setControl( container );

    /* File group. */
    final Group fileGroup = new Group( container, SWT.NONE );
    fileGroup.setLayout( new GridLayout( 2, false ) );
    fileGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    fileGroup.setText( Messages.getString( "org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFiles.12" ) ); //$NON-NLS-1$

    final FileAndHistoryData sourceFile = m_data.getSourceFile();
    final IObservableValue modelFile = BeansObservables.observeValue( sourceFile, FileAndHistoryData.PROPERTY_FILE );
    final IObservableValue modelHistory = BeansObservables.observeValue( sourceFile, FileAndHistoryData.PROPERTY_HISTORY );

    final FileChooserDelegateOpen delegate = CoverageFormats.createFileOpenDelegate();
    final FileBinding fileBinding = new FileBinding( m_binding, modelFile, delegate );

    final Control historyControl = fileBinding.createFileFieldWithHistory( fileGroup, modelHistory );
    historyControl.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final Button fileButton = fileBinding.createFileSearchButton( fileGroup, historyControl );
    fileButton.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );

    /* Additional actions. */
    createAdditionalActions( fileGroup );

    /* Coordinate system combo. */
    createCoordinateSystemCombo( container );

    /* Data folder. */
    createGridFolderControl( container );
  }

  private void createAdditionalActions( final Composite parent )
  {
    try
    {
      final CoverageManagementAction[] additionalActions = KalypsoGmlUiExtensions.createCoverageManagementActions();
      for( final CoverageManagementAction additionalAction : additionalActions )
      {
        if( !CoverageManagementAction.ROLE_WIZARD.equals( additionalAction.getActionRole() ) )
          continue;

        if( !additionalAction.isVisible() )
          continue;

        final Button additionalButton = new Button( parent, SWT.CHECK );
        additionalButton.setText( additionalAction.getActionText() );
        additionalButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
        additionalButton.addSelectionListener( new SelectionAdapter()
        {
          @Override
          public void widgetSelected( final SelectionEvent e )
          {
            final Button source = (Button)e.getSource();
            m_checkedActions.put( additionalAction, new Boolean( source.getSelection() ) );
          }
        } );

        m_checkedActions.put( additionalAction, Boolean.FALSE );
      }
    }
    catch( final CoreException ex )
    {
      ex.printStackTrace();
    }
  }

  private void createCoordinateSystemCombo( final Composite parent )
  {
    final CRSSelectionPanel crsPanel = new CRSSelectionPanel( parent, SWT.NONE );
    crsPanel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    crsPanel.setToolTipText( Messages.getString( "org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFiles.0" ) ); //$NON-NLS-1$

    final IObservableValue targetSrs = crsPanel.observe();
    final IObservableValue modelSrs = BeansObservables.observeValue( m_data, ImportCoverageData.PROPERTY_SOURCE_SRS );
    m_binding.bindValue( targetSrs, modelSrs );
  }

  private void createGridFolderControl( final Composite parent )
  {
    final Group folderGroup = new Group( parent, SWT.NONE );
    folderGroup.setLayout( new GridLayout( 2, false ) );
    folderGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    folderGroup.setText( Messages.getString( "org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFiles.1" ) ); //$NON-NLS-1$
    folderGroup.setToolTipText( Messages.getString( "org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFiles.2" ) ); //$NON-NLS-1$

    final Text tFolder = new Text( folderGroup, SWT.BORDER );
    tFolder.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        handleFolderModified( tFolder.getText() );
      }
    } );
    tFolder.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );

    final boolean allowUserChangeGridFolder = m_data.isChangeDataFolderAllowed();

    tFolder.setEnabled( allowUserChangeGridFolder );

    final Button buttonFolder = new Button( folderGroup, SWT.NONE );
    buttonFolder.setText( "..." ); //$NON-NLS-1$
    buttonFolder.setEnabled( allowUserChangeGridFolder );
    buttonFolder.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        browseForFolder( tFolder );
      }
    } );

    final String dataFolderPath = m_data.getDataContainerPath();
    if( dataFolderPath != null )
      tFolder.setText( dataFolderPath );
  }

  protected void handleFolderModified( final String text )
  {
    m_data.setDataContainerPath( text );
  }

  protected void browseForFolder( final Text tFolder )
  {
    final IContainer initialRoot = m_data.getDataContainer();

    final ContainerSelectionDialog dialog = new ContainerSelectionDialog( getShell(), initialRoot, false, Messages.getString( "org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFiles.3" ) ); //$NON-NLS-1$
    dialog.setTitle( Messages.getString( "org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFiles.4" ) ); //$NON-NLS-1$
    if( dialog.open() == Window.OK )
    {
      final IPath newPath = (IPath)dialog.getResult()[0];
      tFolder.setText( newPath.toPortableString() );
    }
  }

  public CoverageManagementAction[] getCheckedActions( )
  {
    final List<CoverageManagementAction> checkedActions = new ArrayList<>();

    final Set<Entry<CoverageManagementAction, Boolean>> entries = m_checkedActions.entrySet();
    for( final Entry<CoverageManagementAction, Boolean> entry : entries )
    {
      final Boolean checked = entry.getValue();
      if( checked.booleanValue() )
        checkedActions.add( entry.getKey() );
    }

    return checkedActions.toArray( new CoverageManagementAction[] {} );
  }
}