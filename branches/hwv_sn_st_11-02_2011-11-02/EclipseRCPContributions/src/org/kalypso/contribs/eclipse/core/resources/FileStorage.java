package org.kalypso.contribs.eclipse.core.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.EclipseRCPContributionsPlugin;

/**
 * Storage for a <code>java.io.File</code>.
 * 
 * @author Holger Albert
 */
public class FileStorage extends PlatformObject implements IStorage
{
  /**
   * The file associated with this storage.
   */
  private File m_file;

  /**
   * The constructor.
   * 
   * @param file
   *          The file associated with this storage.
   */
  public FileStorage( File file )
  {
    m_file = file;
  }

  /**
   * @see org.eclipse.core.resources.IStorage#getContents()
   */
  @Override
  public InputStream getContents( ) throws CoreException
  {
    try
    {
      return new FileInputStream( m_file );
    }
    catch( IOException ex )
    {
      throw new CoreException( new Status( IStatus.ERROR, EclipseRCPContributionsPlugin.ID, ex.getLocalizedMessage(), ex ) );
    }
  }

  /**
   * @see org.eclipse.core.resources.IStorage#getFullPath()
   */
  @Override
  public IPath getFullPath( )
  {
    try
    {
      return new Path( m_file.getCanonicalPath() );
    }
    catch( IOException ex )
    {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * @see org.eclipse.core.resources.IStorage#getName()
   */
  @Override
  public String getName( )
  {
    return m_file.getName();
  }

  /**
   * @see org.eclipse.core.resources.IStorage#isReadOnly()
   */
  @Override
  public boolean isReadOnly( )
  {
    return true;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return m_file.hashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object )
  {
    return object instanceof FileStorage && ((FileStorage) object).getFile().equals( m_file );
  }

  /**
   * This function returns the file associated with this storage.
   * 
   * @return The file associated with this storage.
   */
  public File getFile( )
  {
    return m_file;
  }
}