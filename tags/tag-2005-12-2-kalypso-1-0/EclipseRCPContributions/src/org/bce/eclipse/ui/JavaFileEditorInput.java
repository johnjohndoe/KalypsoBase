package org.bce.eclipse.ui;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * An EditorInput which wraps an {@link java.io.File}.
 * Get the file via {@link #getAdapter(Class) getAdapter(File.class)}
 * 
 * @author gernot
 *
 */
public class JavaFileEditorInput implements IEditorInput
{
  private final File m_file;

  public JavaFileEditorInput( final File file )
  {
    m_file = file;
  }
  
  /**
   * @see org.eclipse.ui.IEditorInput#exists()
   */
  public boolean exists( )
  {
    return m_file.exists();
  }

  /**
   * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
   */
  public ImageDescriptor getImageDescriptor( )
  {
    return null;
  }

  /**
   * @see org.eclipse.ui.IEditorInput#getName()
   */
  public String getName( )
  {
    return m_file.getName();
  }

  /**
   * @see org.eclipse.ui.IEditorInput#getPersistable()
   */
  public IPersistableElement getPersistable( )
  {
    return null;
  }

  /**
   * @see org.eclipse.ui.IEditorInput#getToolTipText()
   */
  public String getToolTipText( )
  {
    return m_file.getAbsolutePath();
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  public Object getAdapter( final Class adapter )
  {
    if( adapter == File.class )
      return m_file;
    
    return null;
  }
}
