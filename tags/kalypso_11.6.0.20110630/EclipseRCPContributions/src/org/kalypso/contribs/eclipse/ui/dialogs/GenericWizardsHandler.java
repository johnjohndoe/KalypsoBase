package org.kalypso.contribs.eclipse.ui.dialogs;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.wizards.IWizardRegistry;

public abstract class GenericWizardsHandler extends AbstractHandler
{
  private final IPropertyChangeListener m_listener = new IPropertyChangeListener()
  {
    @Override
    public void propertyChange( final PropertyChangeEvent event )
    {
      handleExpressionChanged();
    }
  };

  private final IWizardRegistry m_registry;

  private final IEvaluationService m_service;

  public GenericWizardsHandler( final String plugin, final String extensionPoint )
  {
    m_registry = new GenericWizardRegistry( plugin, extensionPoint );
    m_service = (IEvaluationService) PlatformUI.getWorkbench().getService( IEvaluationService.class );

    final RegisterExpressionsVisitor visitor = new RegisterExpressionsVisitor( m_service, m_listener );
    visitor.accept(  m_registry.getRootCategory() );
  }

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

    final GenericWizardsWizard importWizard = createWizard( selection, m_registry );
    importWizard.setFilter( filter );
    final WizardDialog dialog = new WizardDialog( shell, importWizard );
    dialog.open();

    return null;
  }

  protected abstract GenericWizardsWizard createWizard( IStructuredSelection selection, IWizardRegistry registry );

  protected void handleExpressionChanged( )
  {
    final IEvaluationContext currentState = m_service.getCurrentState();
    setEnabled( currentState );
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