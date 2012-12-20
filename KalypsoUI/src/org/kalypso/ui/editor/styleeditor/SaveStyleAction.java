package org.kalypso.ui.editor.styleeditor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public final class SaveStyleAction extends Action
{
  private final SLDComposite m_sldEditor;

  public SaveStyleAction( final SLDComposite sldEditor )
  {
    super( Messages.getString( "SaveStyleAction_0" ), ImageProvider.IMAGE_STYLEEDITOR_SAVE ); //$NON-NLS-1$

    m_sldEditor = sldEditor;

    setToolTipText( MessageBundle.STYLE_EDITOR_SAVE_STYLE );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.display.getActiveShell();
    final IKalypsoStyle style = m_sldEditor.getKalypsoStyle();

    final IStatus result = style.save( shell );
    final String errorMsg = String.format( Messages.getString( "SaveStyleAction_1" ) ); //$NON-NLS-1$
    ErrorDialog.openError( shell, Messages.getString( "SaveStyleAction_2" ), errorMsg, result ); //$NON-NLS-1$

    m_sldEditor.updateActions();
  }

  public void update( )
  {
    final IKalypsoStyle style = m_sldEditor.getKalypsoStyle();

    setEnabled( style != null && style.isDirty() );
  }
}