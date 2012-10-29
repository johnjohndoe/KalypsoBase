package org.kalypso.gml.ui.internal.feature;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.featureview.views.FeatureView;
import org.kalypso.gml.ui.i18n.Messages;

public class EditFeatureHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final Shell shell = HandlerUtil.getActiveShellChecked( event );

    try
    {
      final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked( event );
      final IWorkbenchPage page = window.getActivePage();
      page.showView( FeatureView.ID, null, IWorkbenchPage.VIEW_ACTIVATE );
    }
    catch( final PartInitException e )
    {
      e.printStackTrace();
      final String title = HandlerUtils.getCommandName( event );
      ErrorDialog.openError( shell, title, Messages.getString( "org.kalypso.ui.editor.gmleditor.actions.EditFeature.2" ), e.getStatus() ); //$NON-NLS-1$
    }

    return null;
  }
}