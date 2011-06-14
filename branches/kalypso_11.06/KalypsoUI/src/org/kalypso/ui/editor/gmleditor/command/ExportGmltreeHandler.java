package org.kalypso.ui.editor.gmleditor.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.ui.editor.gmleditor.part.GmlTreeView;

public class ExportGmltreeHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final Shell shell = HandlerUtil.getActiveShellChecked( event );

    final GmlTreeView treeViewer = GmltreeHandlerUtils.getTreeViewerChecked( event );
    if( treeViewer == null )
      return null;

    final IStructuredSelection selection = treeViewer.getSelection();

    final ExportGmlWizard exportWizard = new ExportGmlWizard( selection );
    final WizardDialog dialog = new WizardDialog( shell, exportWizard );
    dialog.open();

    return null;
  }
}