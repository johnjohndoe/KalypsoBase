/**
 *
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
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.commons.databinding.validation.AlreadyExistsValidator;
import org.kalypso.commons.databinding.validation.StringEmptyValidator;

/**
 * @author Stefan Kurzbach
 */
public class ScenarioWizardPage extends WizardPage
{
  private final static String STR_NEW_NAME_MUST_NOT_BE_EMPTY = Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.0" ); //$NON-NLS-1$

  private final static String STR_ALREADY_EXISTS = Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.1" ); //$NON-NLS-1$

  private static final String STR_FORBIDDEN_FOLDER = Messages.getString("ScenarioWizardPage.0"); //$NON-NLS-1$

  private final ScenarioData m_data;

  private IDataBinding m_binding;

  public ScenarioWizardPage( final ScenarioData data )
  {
    super( "newScenarioPage" ); //$NON-NLS-1$

    m_data = data;

    setTitle( Messages.getString("ScenarioWizardPage.1") ); //$NON-NLS-1$
    setDescription( Messages.getString("ScenarioWizardPage.2") ); //$NON-NLS-1$
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
    activateCheck.setText( Messages.getString("ScenarioWizardPage.3") ); //$NON-NLS-1$
    activateCheck.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );

    /* Bindings */
    final ISWTObservableValue targetName = SWTObservables.observeText( nameField, SWT.Modify );
    final IObservableValue modelName = BeansObservables.observeValue( m_data, ScenarioData.PROPERTY_NAME );

    final IValidator emptyNameValidator = new StringEmptyValidator( IStatus.ERROR, STR_NEW_NAME_MUST_NOT_BE_EMPTY );

    final Set<String> existingNames = m_data.getExistingNames();
    final IValidator duplicateNameValidator = new AlreadyExistsValidator<>( String.class, existingNames, IStatus.ERROR, STR_ALREADY_EXISTS );

    final Set<String> frobiddenNames = m_data.getExistingFolders();
    final IValidator forbiddenNameValidator = new AlreadyExistsValidator<>( String.class, frobiddenNames, IStatus.ERROR, STR_FORBIDDEN_FOLDER );

    m_binding.bindValue( targetName, modelName, emptyNameValidator, duplicateNameValidator, forbiddenNameValidator );

    final ISWTObservableValue targetComment = SWTObservables.observeText( commentField, SWT.Modify );
    final IObservableValue modelComment = BeansObservables.observeValue( m_data, ScenarioData.PROPERTY_COMMENT );
    m_binding.bindValue( targetComment, modelComment );

    final ISWTObservableValue targetActivate = SWTObservables.observeSelection( activateCheck );
    final IObservableValue modelActivate = BeansObservables.observeValue( m_data, ScenarioData.PROPERTY_ACTIVATE_SCENARIO );
    m_binding.bindValue( targetActivate, modelActivate );
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
    checkbox.setText( Messages.getString("ScenarioWizardPage.4") ); //$NON-NLS-1$
    checkbox.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );

    // FIXME Copied derived scenarios are not written into the cases.xml...
    checkbox.setEnabled( false );

    final ISWTObservableValue target = SWTObservables.observeSelection( checkbox );
    final IObservableValue model = BeansObservables.observeValue( m_data, ScenarioData.PROPERTY_COPY_SUB_SCENARIOS );
    m_binding.bindValue( target, model );
  }
}