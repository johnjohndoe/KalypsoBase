package org.kalypso.ui.editor.gmleditor.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.ui.editor.gmleditor.part.GmlTreeView;

public class ExportGmltreeHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final Shell shell = HandlerUtil.getActiveShellChecked( event );
    final IWorkbenchSite site = HandlerUtil.getActiveSiteChecked( event );

    final GmlTreeView treeViewer = GmltreeHandlerUtils.getTreeViewerChecked( event );
    if( treeViewer == null )
      return null;

    final String commandName = HandlerUtils.getCommandName( event );

    // FIXME: open gml export wizard

    return null;
  }
}