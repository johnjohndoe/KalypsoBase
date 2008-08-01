package org.kalypso.gml.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.kalypso.gml.ui.wizard.grid.RectifiedGridCoverageImportWizard;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;

public class ImportGridHandler extends AbstractHandler implements IHandler
{
  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final EvaluationContext context = (EvaluationContext) event.getApplicationContext();
    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );
    final IStructuredSelection selection = (IStructuredSelection) context.getVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME );

    final RectifiedGridCoverageImportWizard wizard = new RectifiedGridCoverageImportWizard();
    final Object firstElement = selection.getFirstElement();

    final ICoverageCollection cc;
    if( firstElement instanceof Feature )
    {
      final Feature fate = (Feature) firstElement;
      cc = (ICoverageCollection) fate.getAdapter( ICoverageCollection.class );
    }
    else
      cc = null;

    wizard.setCoverageCollection( cc );

    wizard.init( PlatformUI.getWorkbench(), selection );
    final WizardDialog wizardDialog = new WizardDialog( shell, wizard );
    return wizardDialog.open();
  }

}
