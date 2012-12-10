/**
 * 
 */
package org.kalypso.contribs.eclipse.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;

/**
 * Default implementation of {@link org.kalypso.contribs.eclipse.ui.IEditorPartAction}. Just stores a reference
 * to the given editor.
 * 
 * @author Belger
 */
public abstract class AbstractEditorPartAction extends Action implements IEditorPartAction
{
  private IEditorPart m_targetEditor;

  public AbstractEditorPartAction( )
  {
    super();
  }

  public AbstractEditorPartAction( String text, ImageDescriptor image )
  {
    super( text, image );
  }

  public AbstractEditorPartAction( String text, int style )
  {
    super( text, style );
  }

  public AbstractEditorPartAction( String text )
  {
    super( text );
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.IEditorPartAction#setEditorPart(org.eclipse.ui.IEditorPart)
   */
  @Override
  public void setEditorPart( final IEditorPart targetEditor )
  {
    m_targetEditor = targetEditor;
  }

  public IEditorPart getEditorPart( )
  {
    return m_targetEditor;
  }
}
