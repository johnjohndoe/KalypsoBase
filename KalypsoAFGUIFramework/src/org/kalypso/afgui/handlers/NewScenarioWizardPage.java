/**
 *
 */
package org.kalypso.afgui.handlers;

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
public class NewScenarioWizardPage extends WizardPage
{
  private final static String STR_NEW_NAME_MUST_NOT_BE_EMPTY = Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.0" ); //$NON-NLS-1$

  private final static String STR_ALREADY_EXISTS = Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.1" ); //$NON-NLS-1$

  private final NewScenarioData m_data;

  private IDataBinding m_binding;

  public NewScenarioWizardPage( final NewScenarioData data )
  {
    super( "newScenarioPage" ); //$NON-NLS-1$

    m_data = data;

    setTitle( "Properties" );
    setDescription( "Please enter the properties of the new scenario" );
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

    final Label parentLabel = new Label( panel, SWT.NONE );
    parentLabel.setText( Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.4" ) ); //$NON-NLS-1$

    final Text parentTFE = new Text( panel, SWT.BORDER );
    parentTFE.setEditable( false );
    parentTFE.setText( m_data.getParentScenarioName() );
    parentTFE.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

    final Label commentLabel = new Label( panel, SWT.NONE );
    commentLabel.setText( Messages.getString( "org.kalypso.afgui.handlers.NewSimulationModelControlBuilder.5" ) ); //$NON-NLS-1$

    final Text commentField = new Text( panel, SWT.BORDER | SWT.WRAP | SWT.MULTI );
    commentField.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 10 ) );

    /* Bindings */
    final ISWTObservableValue targetName = SWTObservables.observeText( nameField, SWT.Modify );
    final IObservableValue modelName = BeansObservables.observeValue( m_data, NewScenarioData.PROPERTY_NAME );

    final IValidator emptyNameValidator = new StringEmptyValidator( IStatus.ERROR, STR_NEW_NAME_MUST_NOT_BE_EMPTY );

    final Set<String> existingNames = m_data.getExistingNames();
    final IValidator duplicateNameValidator = new AlreadyExistsValidator<>( String.class, existingNames, IStatus.ERROR, STR_ALREADY_EXISTS );

    m_binding.bindValue( targetName, modelName, emptyNameValidator, duplicateNameValidator );

    final ISWTObservableValue targetComment = SWTObservables.observeText( commentField, SWT.Modify );
    final IObservableValue modelComment = BeansObservables.observeValue( m_data, NewScenarioData.PROPERTY_COMMENT );
    m_binding.bindValue( targetComment, modelComment );
  }
}