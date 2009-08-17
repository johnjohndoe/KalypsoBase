package org.kalypso.gml.ui.handler;

import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;

public class ImportGridHandler extends AbstractHandler implements IHandler
{
  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final EvaluationContext context = (EvaluationContext) event.getApplicationContext();
    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );
    final IStructuredSelection selection = (IStructuredSelection) context.getVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME );

    final Object firstElement = selection.getFirstElement();

    final ICoverageCollection cc = findCoverageCollection( firstElement );
    if( cc == null )
      throw new ExecutionException( "No coverage collection found", null );

    // Choose target folder
    final URL gmlContext = cc.getFeature().getWorkspace().getContext();
    final IFile gmlFile = ResourceUtilities.findFileFromURL( gmlContext );
    final IContainer gmlFolder = gmlFile == null ? null : gmlFile.getParent();

    final AddRectifiedGridCoveragesWizard wizard = new AddRectifiedGridCoveragesWizard( cc, gmlFolder, true );
    final WizardDialog wizardDialog = new WizardDialog( shell, wizard );
    wizardDialog.open();

    return null;
  }

  private ICoverageCollection findCoverageCollection( final Object firstElement )
  {
    if( firstElement instanceof Feature )
    {
      final Feature fate = (Feature) firstElement;
      return (ICoverageCollection) fate.getAdapter( ICoverageCollection.class );
    }

    return null;
  }



}
