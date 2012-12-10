package org.kalypso.ui.editor.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.i18n.Messages;

/**
 * Adds a new feature from an yet unknown schema to a workspace. TODO: no yet implemented
 * 
 * @author Gernot Belger
 */
public final class NewFeatureFromExternalSchemaAction extends Action
{
  public NewFeatureFromExternalSchemaAction( )
  {
    super( Messages.getString( "org.kalypso.ui.editor.actions.FeatureActionUtilities.4" ) ); //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.widget.getDisplay().getActiveShell();
    MessageDialog.openInformation( shell, Messages.getString( "org.kalypso.ui.editor.actions.FeatureActionUtilities.5" ), Messages.getString( "org.kalypso.ui.editor.actions.FeatureActionUtilities.6" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }
}