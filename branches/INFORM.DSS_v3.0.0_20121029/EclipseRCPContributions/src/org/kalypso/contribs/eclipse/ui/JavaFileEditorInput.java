package org.kalypso.contribs.eclipse.ui;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * An EditorInput which wraps an {@link java.io.File}. Get the file via {@link #getAdapter(Class)
 * getAdapter(File.class)}
 *
 * @author gernot
 */
public class JavaFileEditorInput implements IEditorInput
{
  private final File m_file;

  public JavaFileEditorInput( final File file )
  {
    m_file = file;
  }

  @Override
  public boolean exists( )
  {
    return m_file.exists();
  }

  @Override
  public ImageDescriptor getImageDescriptor( )
  {
    return null;
  }

  @Override
  public String getName( )
  {
    return m_file.getName();
  }

  @Override
  public IPersistableElement getPersistable( )
  {
    return null;
  }

  @Override
  public String getToolTipText( )
  {
    return m_file.getAbsolutePath();
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == File.class )
      return m_file;

    return null;
  }
}
