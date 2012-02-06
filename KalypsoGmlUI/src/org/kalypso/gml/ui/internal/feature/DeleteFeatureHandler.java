package org.kalypso.gml.ui.internal.feature;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.ogc.gml.command.DeleteFeatureCommand;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ui.editor.actions.TableFeatureControlUtils;
import org.kalypsodeegree.model.feature.Feature;

public class DeleteFeatureHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final Shell shell = HandlerUtil.getActiveShellChecked( event );
    final ISelection selection = HandlerUtil.getCurrentSelectionChecked( event );
    if( !(selection instanceof IFeatureSelection) )
      throw new ExecutionException( "Handler only works on IFeatureSelection, check enablement" ); //$NON-NLS-1$

    final IFeatureSelection featureSelection = (IFeatureSelection) selection;

    /* Get all selected features. */
    final EasyFeatureWrapper[] allFeatures = featureSelection.getAllFeatures();

    /* Build the delete command. */
    final DeleteFeatureCommand command = TableFeatureControlUtils.deleteFeaturesFromSelection( allFeatures, shell );
    if( command != null )
    {
      try
      {
        /* At least one selected feature must exist, otherwise the command would be null. */
        final CommandableWorkspace workspace = allFeatures[0].getWorkspace();
        workspace.postCommand( command );
      }
      catch( final Exception e )
      {
        e.printStackTrace();

        final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, "", e ); //$NON-NLS-1$

        final String title = HandlerUtils.getCommandName( event );
        ErrorDialog.openError( shell, title, Messages.getString( "org.kalypso.ui.editor.actions.FeatureRemoveActionDelegate.5" ), status ); //$NON-NLS-1$
      }
      finally
      {
        final Feature[] features = FeatureSelectionHelper.getFeatures( featureSelection );
        featureSelection.getSelectionManager().changeSelection( features, new EasyFeatureWrapper[0] );
      }
    }
    return null;
  }
}