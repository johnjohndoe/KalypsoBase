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

package org.kalypso.ui.wizard.shape;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.commons.databinding.swt.WorkspaceFileBinding;
import org.kalypso.commons.databinding.validation.NotNullValidator;
import org.kalypso.transformation.ui.CRSSelectionPanel;
import org.kalypso.transformation.ui.validators.CRSInputValidator;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.shape.ImportShapeFileData.StyleImport;
import org.kalypsodeegree.graphics.sld.UserStyle;

/**
 * FIXME:
 * <ul>
 * <li>dialog settings!</li>
 * <li>if shape is selected, preselect sld with same name</li>
 * <li></li>
 * </ul>
 *
 * @author kuepfer
 */
public class ImportShapeFileImportPage extends WizardPage
{
  private CRSSelectionPanel m_crsPanel;

  private IProject m_project;

  private ViewerFilter m_filter;

  private DatabindingWizardPage m_binding;

  private final ImportShapeFileData m_data;

  public ImportShapeFileImportPage( final String pageName, final String title, final ImageDescriptor titleImage, final ImportShapeFileData data )
  {
    super( pageName, title, titleImage );

    m_data = data;

    setDescription( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.1" ) ); //$NON-NLS-1$
    setPageComplete( false );
  }

  @Override
  public void createControl( final Composite parent )
  {
    m_binding = new DatabindingWizardPage( this, null );

    initializeDialogUnits( parent );

    final Composite panel = new Composite( parent, SWT.NULL );
    GridLayoutFactory.swtDefaults().applyTo( panel );
    panel.setFont( parent.getFont() );

    createSourceGroup( panel );
    createStyleGroup( panel );

    setControl( panel );

    setMessage( null, IMessageProvider.NONE );
    setErrorMessage( null );
  }

  private void createSourceGroup( final Composite parent )
  {
    final Group fileGroup = new Group( parent, SWT.NULL );
    GridLayoutFactory.swtDefaults().numColumns( 3 ).equalWidth( false ).applyTo( fileGroup );
    fileGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

    fileGroup.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.2" ) ); //$NON-NLS-1$

    final Label sourceFileLabel = new Label( fileGroup, SWT.NONE );
    sourceFileLabel.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.3" ) ); //$NON-NLS-1$

    createShapeFileChooser( fileGroup );
    createCrsChooser( fileGroup );
  }

  private void createStyleGroup( final Composite parent )
  {
    final Group styleGroup = new Group( parent, SWT.NULL );
    styleGroup.setLayout( new GridLayout( 3, false ) );
    styleGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    styleGroup.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.5" ) ); //$NON-NLS-1$

    createStyleChooser( styleGroup );
  }

  private void createShapeFileChooser( final Composite parent )
  {
    final FileAndHistoryData shapeFile = m_data.getShapeFile();

    final IObservableValue modelFile = BeansObservables.observeValue( shapeFile, FileAndHistoryData.PROPERTY_PATH );
    final IObservableValue modelHistory = BeansObservables.observeValue( shapeFile, FileAndHistoryData.PROPERTY_HISTORY );

    final String dialogMessage = Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.14" ); //$NON-NLS-1$
    final String[] extensions = new String[] { "shp" }; //$NON-NLS-1$

    final WorkspaceFileBinding fileBinding = new WorkspaceFileBinding( m_binding, modelFile, dialogMessage, extensions );

    fileBinding.setInputContainer( m_project );
    fileBinding.setFilter( m_filter );

    final Control fileField = fileBinding.createFileFieldWithHistory( parent, modelHistory );
    fileField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    fileBinding.createFileSearchButton( parent );
  }

  private void createCrsChooser( final Composite parent )
  {
    m_crsPanel = new CRSSelectionPanel( parent, SWT.NONE );
    m_crsPanel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );

    final IObservableValue target = m_crsPanel.observe();
    final IObservableValue model = BeansObservables.observeValue( m_data, ImportShapeFileData.PROPERTY_SRS );

    final NotNullValidator<String> notNullValidator = new NotNullValidator<>( String.class, IStatus.ERROR, Messages.getString( "ImportShapeFileImportPage.1" ) ); //$NON-NLS-1$

    m_binding.bindValue( target, model, notNullValidator, new CRSInputValidator() );
  }

  private void createStyleChooser( final Composite parent )
  {
    createStyleTypeRadio( parent );

    final WorkspaceFileBinding styleFileBinding = createStyleFileControls( parent );

    createStyleNameControls( parent );

    styleFileBinding.getFileBinding().updateTargetToModel();
  }

  private void createStyleTypeRadio( final Composite parent )
  {
    final Composite radioPanel = new Composite( parent, SWT.NONE );
    GridLayoutFactory.fillDefaults().applyTo( radioPanel );
    radioPanel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );

    final StyleImport[] styleImportTypes = StyleImport.values();
    for( final StyleImport styleImportType : styleImportTypes )
      addStyleRadio( radioPanel, styleImportType );
  }

  private WorkspaceFileBinding createStyleFileControls( final Composite parent )
  {
    final Label styleLabel = new Label( parent, SWT.NONE );
    styleLabel.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.6" ) ); //$NON-NLS-1$

    final IObservableValue modelFile = BeansObservables.observeValue( m_data.getStyleFile(), FileAndHistoryData.PROPERTY_PATH );
    final IObservableValue modelHistory = BeansObservables.observeValue( m_data.getStyleFile(), FileAndHistoryData.PROPERTY_HISTORY );

    final String dialogMessage = Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.14" ); //$NON-NLS-1$
    final String[] extensions = new String[] { "sld" }; //$NON-NLS-1$

    final WorkspaceFileBinding fileBinding = new WorkspaceFileBinding( m_binding, modelFile, dialogMessage, extensions );
    fileBinding.setInputContainer( m_project );

    final Control fileField = fileBinding.createFileFieldWithHistory( parent, modelHistory );
    fileField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final Button searchButton = fileBinding.createFileSearchButton( parent );

    /* file is optional iff import type is 'existing' */
    fileBinding.setIsOptional( m_data.getStyleImportType() != StyleImport.selectExisting );

    // final DataBindingContext bindingContext = m_binding.getBindingContext();
    m_data.addPropertyChangeListener( ImportShapeFileData.PROPERTY_STYLE_IMPORT_TYPE, new PropertyChangeListener()
    {
      @Override
      public void propertyChange( final PropertyChangeEvent evt )
      {
        final IObservableValue validationStatus = fileBinding.getFileBinding().getValidationStatus();

        fileBinding.setIsOptional( evt.getNewValue() != StyleImport.selectExisting );

        // If style file is currently bad, clear it now, else the error message remains
        final IStatus value = (IStatus) validationStatus.getValue();
        if( value != null && !value.isOK() && evt.getNewValue() != StyleImport.selectExisting )
          modelFile.setValue( null );

        // we need to force revalidation here, but how...?!
        fileBinding.getFileBinding().validateTargetToModel();
      }
    } );

    /* Enablement */
    final IObservableValue modelEnablement = BeansObservables.observeValue( m_data, ImportShapeFileData.PROPERTY_STYLE_CONTROLS_ENABLED );

    final IObservableValue targetLabelEnablement = SWTObservables.observeEnabled( styleLabel );
    m_binding.bindValue( targetLabelEnablement, modelEnablement );

    final IObservableValue targetFieldEnablement = SWTObservables.observeEnabled( fileField );
    m_binding.bindValue( targetFieldEnablement, modelEnablement );

    final IObservableValue targetButtonEnablement = SWTObservables.observeEnabled( searchButton );
    m_binding.bindValue( targetButtonEnablement, modelEnablement );

    return fileBinding;
  }

  private void createStyleNameControls( final Composite parent )
  {
    final Label styleNameLabel = new Label( parent, SWT.NONE );
    styleNameLabel.setText( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.8" ) ); //$NON-NLS-1$

    final ComboViewer styleNameChooser = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
    styleNameChooser.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    styleNameChooser.setContentProvider( new ArrayContentProvider() );
    styleNameChooser.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        if( element instanceof UserStyle )
        {
          final UserStyle userStyle = (UserStyle) element;
          final String title = userStyle.getTitle();
          if( StringUtils.isBlank( title ) )
            return userStyle.getName();
          return title;
        }

        return ObjectUtils.toString( element );
      }
    } );

    /* bind input */
    final IObservableValue targetInput = ViewersObservables.observeInput( styleNameChooser );
    final IObservableValue modelInput = BeansObservables.observeValue( m_data, ImportShapeFileData.PROPERTY_STYLES );
    m_binding.bindValue( targetInput, modelInput );

    /* bind selection */
    final IObservableValue targetSelection = ViewersObservables.observeSinglePostSelection( styleNameChooser );
    final IObservableValue modelSelection = BeansObservables.observeValue( m_data, ImportShapeFileData.PROPERTY_STYLE );
    m_binding.bindValue( targetSelection, modelSelection );

    /* Enablement */
    final IObservableValue modelEnablement = BeansObservables.observeValue( m_data, ImportShapeFileData.PROPERTY_STYLE_NAME_CONTROL_ENABLED );

    final IObservableValue targetLabelEnablement = SWTObservables.observeEnabled( styleNameLabel );
    m_binding.bindValue( targetLabelEnablement, modelEnablement );

    final IObservableValue targetComboEnablement = SWTObservables.observeEnabled( styleNameChooser.getControl() );
    m_binding.bindValue( targetComboEnablement, modelEnablement );
  }

  private Button addStyleRadio( final Composite parent, final StyleImport styleImport )
  {
    final Button radio = new Button( parent, SWT.RADIO );
    radio.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
    radio.setText( styleImport.toString() );
    radio.setToolTipText( styleImport.getTooltip() );

    final ISWTObservableValue target = SWTObservables.observeSelection( radio );
    final IObservableValue model = BeansObservables.observeValue( m_data, ImportShapeFileData.PROPERTY_STYLE_IMPORT_TYPE );

    final DataBinder binder = new DataBinder( target, model );
    binder.setModelToTargetConverter( new StyleImportTypeToRadioConverter( styleImport ) );
    binder.setTargetToModelConverter( new StyleImportTypeFromRadioConverter( styleImport ) );

    m_binding.bindValue( binder );

    return radio;
  }

  public void setViewerFilter( final ViewerFilter filter )
  {
    m_filter = filter;
  }

  void setProjectSelection( final IProject project )
  {
    m_project = project;
  }
}