package org.kalypso.ui.editor.styleeditor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public final class ResetUserStyleAction extends Action
{
  private final SLDComposite m_sldEditor;

  public ResetUserStyleAction( final SLDComposite sldEditor )
  {
    super( Messages.getString( "ResetUserStyleAction_0" ), ImageProvider.IMAGE_STYLEEDITOR_RESET ); //$NON-NLS-1$

    m_sldEditor = sldEditor;

    setToolTipText( MessageBundle.STYLE_EDITOR_RESET_STYLE );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.display.getActiveShell();
    final IKalypsoStyle style = m_sldEditor.getKalypsoStyle();

    if( !MessageDialog.openConfirm( shell, getText(), Messages.getString( "ResetUserStyleAction_1" ) ) ) //$NON-NLS-1$
      return;

    final IStatus result = style.reset( shell );

    final String errorMsg = String.format( Messages.getString( "ResetUserStyleAction_2" ) ); //$NON-NLS-1$
    ErrorDialog.openError( shell, Messages.getString( "ResetUserStyleAction_3" ), errorMsg, result ); //$NON-NLS-1$

    m_sldEditor.updateControl();
  }

  public void update( )
  {
    final IKalypsoStyle style = m_sldEditor.getKalypsoStyle();
    setEnabled( style != null && style.isResetable() );
  }
}