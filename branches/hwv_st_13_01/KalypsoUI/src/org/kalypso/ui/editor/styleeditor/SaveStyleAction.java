package org.kalypso.ui.editor.styleeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ui.ImageProvider;

/**
 * @author Gernot Belger
 */
public final class SaveStyleAction extends Action
{
  private final SLDComposite m_sldEditor;

  public SaveStyleAction( final SLDComposite sldEditor )
  {
    super( "Save", ImageProvider.IMAGE_STYLEEDITOR_SAVE );

    m_sldEditor = sldEditor;

    setToolTipText( MessageBundle.STYLE_EDITOR_SAVE_STYLE );
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.display.getActiveShell();
    final IKalypsoStyle style = m_sldEditor.getKalypsoStyle();
    final String label = style.getLabel();
    final String titel = String.format( "Save - UserStyle '%s'", label );
    final String msg = String.format( "Save UserStyle '%s'?", label );
    MessageDialog.openConfirm( shell, titel, msg );

    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        style.save( monitor );
        return Status.OK_STATUS;
      }
    };

    final IStatus result = ProgressUtilities.busyCursorWhile( operation );
    final String errorMsg = String.format( "Failed to save style." );
    ErrorDialog.openError( shell, titel, errorMsg, result );

    update();
  }

  public void update( )
  {
    final IKalypsoStyle style = m_sldEditor.getKalypsoStyle();

    setEnabled( style != null && style.isDirty() );
  }

}