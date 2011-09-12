package org.kalypso.contribs.eclipse.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;

/**
 * This matching strategies matches if both editor and the given input adapt to an equal
 * {@link org.eclipse.core.resources.IFile}.
 * 
 * @author belger
 */
public class FileResourceEditorMatchingStrategy implements IEditorMatchingStrategy
{
  @Override
  public boolean matches( final IEditorReference editorRef, final IEditorInput input )
  {
    final IEditorPart editor = editorRef.getEditor( false );
    final Object editorFile = editor.getAdapter( IFile.class );
    final Object inputFile = input.getAdapter( IFile.class );

    return inputFile != null && inputFile.equals( editorFile );
  }
}
