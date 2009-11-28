package org.kalypso.contribs.java.io.filter;

import java.io.File;
import java.io.FilenameFilter;

import org.kalypso.contribs.java.io.FileUtilities;

/**
 * This filter filters all files with the same basename as given.
 * 
 * @author Holger Albert
 */
public class BasenameFilenameFilter implements FilenameFilter
{
  /**
   * The basename.
   */
  private String m_basename;

  /**
   * The constructor.
   * 
   * @param basename
   *          The basename of the file.
   */
  public BasenameFilenameFilter( String basename )
  {
    m_basename = basename;
  }

  /**
   * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
   */
  public boolean accept( File dir, String name )
  {
    File file = new File( dir, name );
    if( !file.exists() || file.isDirectory() )
      return false;

    String baseName = FileUtilities.nameWithoutExtension( name );
    if( baseName == null )
      return false;

    if( baseName.equals( m_basename ) )
      return true;

    return false;
  }
}