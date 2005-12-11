package org.bce.eclipse.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;

/**
 * An action with an associated {@link org.eclipse.ui.IEditorPart}
 * 
 * @author Belger
 */
public interface IEditorPartAction extends IAction
{
  public void setEditorPart( final IEditorPart targetEditor );
}
