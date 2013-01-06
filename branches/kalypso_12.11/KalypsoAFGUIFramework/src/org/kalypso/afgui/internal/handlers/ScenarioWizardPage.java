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
package org.kalypso.afgui.internal.handlers;

import java.util.Set;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.commons.databinding.validation.AlreadyExistsValidator;
import org.kalypso.commons.databinding.validation.StringEmptyValidator;
import org.kalypso.contribs.eclipse.ui.forms.MessageProvider;

/**
 * @author Stefan Kurzbach
 */
public class ScenarioWizardPage extends WizardPage
{
  private final static String STR_NEW_NAME_MUST_NOT_BE_EMPTY = Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.0" ); //$NON-NLS-1$

  private final static String STR_ALREADY_EXISTS = Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.1" ); //$NON-NLS-1$

  private static final String STR_FORBIDDEN_FOLDER = Messages.getString( "ScenarioWizardPage.0" ); //$NON-NLS-1$

  private final ScenarioData m_data;

  private IDataBinding m_binding;

  public ScenarioWizardPage( final ScenarioData data )
  {
    super( "newScenarioPage" ); //$NON-NLS-1$

    m_data = data;

    setTitle( Messages.getString( "ScenarioWizardPage.1" ) ); //$NON-NLS-1$
    setDescription( Messages.getString( "ScenarioWizardPage.2" ) ); //$NON-NLS-1$
  }

  @Override
  public void createControl( final Composite parent )
  {
    m_binding = new DatabindingWizardPage( this, null );

    final Composite panel = new Composite( parent, SWT.FILL );
    GridLayoutFactory.swtDefaults().numColumns( 2 ).applyTo( panel );
    setControl( panel );

    final Label newModelNameLabel = new Label( panel, SWT.NONE );
    newModelNameLabel.setText( Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.3" ) ); //$NON-NLS-1$

    final Text nameField = new Text( panel, SWT.BORDER );
    nameField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    if( m_data.isDerivedVisible() )
      createDerivedField( panel );

    final Label commentLabel = new Label( panel, SWT.NONE );
    commentLabel.setText( Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.5" ) ); //$NON-NLS-1$

    final Text commentField = new Text( panel, SWT.BORDER | SWT.WRAP | SWT.MULTI );
    commentField.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 10 ) );

    if( m_data.isCopySubScenariosVisible() )
      createCopySubScenariosButton( panel );

    final Button activateCheck = new Button( panel, SWT.CHECK );
    activateCheck.setText( Messages.getString( "ScenarioWizardPage.3" ) ); //$NON-NLS-1$
    activateCheck.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    /* Bindings */
    final ISWTObservableValue targetName = SWTObservables.observeText( nameField, SWT.Modify );
    final IObservableValue modelName = BeansObservables.observeValue( m_data, ScenarioData.PROPERTY_NAME );
    final DataBinder nameBinder = new DataBinder( targetName, modelName );

    if( m_data.isNameFieldEnabled() )
    {
      final IValidator emptyNameValidator = new StringEmptyValidator( IStatus.ERROR, STR_NEW_NAME_MUST_NOT_BE_EMPTY );

      final Set<String> existingNames = m_data.getExistingNames();
      final IValidator duplicateNameValidator = new AlreadyExistsValidator<>( String.class, existingNames, IStatus.ERROR, STR_ALREADY_EXISTS );

      final Set<String> forbiddenNames = m_data.getExistingFolders();
      final IValidator forbiddenNameValidator = new AlreadyExistsValidator<>( String.class, forbiddenNames, IStatus.ERROR, STR_FORBIDDEN_FOLDER );

      nameBinder.addTargetAfterConvertValidator( emptyNameValidator );
      nameBinder.addTargetAfterConvertValidator( duplicateNameValidator );
      nameBinder.addTargetAfterConvertValidator( forbiddenNameValidator );
    }
    else
      nameField.setEnabled( false );

    m_binding.bindValue( nameBinder );

    final ISWTObservableValue targetComment = SWTObservables.observeText( commentField, SWT.Modify );
    final IObservableValue modelComment = BeansObservables.observeValue( m_data, ScenarioData.PROPERTY_COMMENT );
    m_binding.bindValue( targetComment, modelComment );

    final ISWTObservableValue targetActivate = SWTObservables.observeSelection( activateCheck );
    final IObservableValue modelActivate = BeansObservables.observeValue( m_data, ScenarioData.PROPERTY_ACTIVATE_SCENARIO );
    m_binding.bindValue( targetActivate, modelActivate );

    /* initial message */
    final MessageProvider initialPageMessage = m_data.getInitialPageMessage();
    if( initialPageMessage != null )
      setMessage( initialPageMessage.getMessage(), initialPageMessage.getMessageType() );

    setErrorMessage( null );
  }

  private void createDerivedField( final Composite panel )
  {
    final Label parentLabel = new Label( panel, SWT.NONE );
    parentLabel.setText( Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.4" ) ); //$NON-NLS-1$

    final Text parentTFE = new Text( panel, SWT.BORDER );
    parentTFE.setEditable( false );
    parentTFE.setText( m_data.getParentScenarioPath() );
    parentTFE.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
  }

  private void createCopySubScenariosButton( final Composite panel )
  {
    final Button checkbox = new Button( panel, SWT.CHECK );
    checkbox.setText( Messages.getString( "ScenarioWizardPage.4" ) ); //$NON-NLS-1$
    checkbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    // FIXME Copied derived scenarios are not written into the cases.xml...
    checkbox.setEnabled( false );

    final ISWTObservableValue target = SWTObservables.observeSelection( checkbox );
    final IObservableValue model = BeansObservables.observeValue( m_data, ScenarioData.PROPERTY_COPY_SUB_SCENARIOS );
    m_binding.bindValue( target, model );
  }
}