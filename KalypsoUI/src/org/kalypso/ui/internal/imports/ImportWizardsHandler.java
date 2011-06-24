package org.kalypso.ui.internal.imports;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.kalypso.contribs.eclipse.ui.dialogs.GenericWizardRegistry;
import org.kalypso.contribs.eclipse.ui.dialogs.IWizardFilter;
import org.kalypso.contribs.eclipse.ui.dialogs.WizardEnablementVisitor;
import org.kalypso.contribs.eclipse.ui.dialogs.WizardFilter;
import org.kalypso.ui.KalypsoGisPlugin;

public class ImportWizardsHandler extends AbstractHandler
{
  private static final String IMPORT_GML_WIZARDS_EXTENSION_POINT = "importWizards"; //$NON-NLS-1$

  private final IWizardRegistry m_registry = new GenericWizardRegistry( KalypsoGisPlugin.getId(), IMPORT_GML_WIZARDS_EXTENSION_POINT );

  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final Shell shell = HandlerUtil.getActiveShellChecked( event );

    final IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelectionChecked( event );

    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    /*
     * Determine the enablement of all wizards in regard to the current context. We need to do this at this point in
     * time, because later (i.e. when the wizard has opened) the context will not longer be valid.
     */
    final WizardEnablementVisitor wizardEnablementVisitor = new WizardEnablementVisitor( context );
    wizardEnablementVisitor.accept( m_registry.getRootCategory() );
    final Map<String, Boolean> enablement = wizardEnablementVisitor.getEnablement();

    final IWizardFilter filter = new WizardFilter( enablement );

    final ImportWizardsWizard importWizard = new ImportWizardsWizard( selection, m_registry );
    importWizard.setFilter( filter );
    final WizardDialog dialog = new WizardDialog( shell, importWizard );
    dialog.open();

    return null;
  }

  @Override
  public void setEnabled( final Object evaluationContext )
  {
    final IEvaluationContext context = (IEvaluationContext) evaluationContext;

    final WizardEnablementVisitor wizardEnablementVisitor = new WizardEnablementVisitor( context );
    wizardEnablementVisitor.accept( m_registry.getRootCategory() );

    final boolean hasEnabled = wizardEnablementVisitor.hasEnabled();

    setBaseEnabled( hasEnabled );
  }
}