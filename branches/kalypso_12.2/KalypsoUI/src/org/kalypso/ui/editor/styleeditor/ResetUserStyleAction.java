package org.kalypso.ui.editor.styleeditor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ui.ImageProvider;

/**
 * @author Gernot Belger
 */
public final class ResetUserStyleAction extends Action
{
  private final SLDComposite m_sldEditor;

  public ResetUserStyleAction( final SLDComposite sldEditor )
  {
    super( "Reset User Style", ImageProvider.IMAGE_STYLEEDITOR_RESET );

    m_sldEditor = sldEditor;

    setToolTipText( MessageBundle.STYLE_EDITOR_RESET_STYLE );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.display.getActiveShell();
    final IKalypsoStyle style = m_sldEditor.getKalypsoStyle();

    if( !MessageDialog.openConfirm( shell, getText(), "Delete the user defined style and reset to default values?" ) )
      return;

    final IStatus result = style.reset( shell );

    final String errorMsg = String.format( "Failed to reset style." );
    ErrorDialog.openError( shell, "Save Style", errorMsg, result );

    m_sldEditor.updateControl();
  }

  public void update( )
  {
    final IKalypsoStyle style = m_sldEditor.getKalypsoStyle();
    setEnabled( style != null && style.isResetable() );
  }
}