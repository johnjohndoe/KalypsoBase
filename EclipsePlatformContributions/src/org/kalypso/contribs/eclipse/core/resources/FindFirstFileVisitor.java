package org.kalypso.contribs.eclipse.core.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

public class FindFirstFileVisitor implements IResourceVisitor
{
  private final String m_name;

  private final boolean m_ignoreCase;

  private IFile m_result = null;

  public FindFirstFileVisitor( final String name, final boolean ignoreCase )
  {
    m_name = name;
    m_ignoreCase = ignoreCase;
  }
  
  public IFile getFile( )
  {
    return m_result;
  }

  @Override
  public boolean visit( final IResource resource )
  {
    if( m_result == null && resource instanceof IFile )
    {
      final IFile file = (IFile) resource;
      final String name = file.getName();
      if( m_ignoreCase && name.equalsIgnoreCase( m_name )
          || (!m_ignoreCase && name.equals( m_name )) )
        m_result = file;
    }

    return true;
  }

}
