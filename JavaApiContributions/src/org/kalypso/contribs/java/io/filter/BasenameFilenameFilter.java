package org.kalypso.contribs.java.io.filter;

import java.io.File;
import java.io.FilenameFilter;

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
  private final String m_basename;

  /**
   * The constructor.
   * 
   * @param basename
   *          The basename of the file.
   */
  public BasenameFilenameFilter( final String basename )
  {
    m_basename = basename;
  }

  /**
   * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
   */
  @Override
  public boolean accept( final File dir, final String name )
  {
    final File file = new File( dir, name );
    if( !file.exists() || file.isDirectory() )
      return false;

    final String baseName = nameWithoutExtension( name );
    if( baseName == null )
      return false;

    if( baseName.equals( m_basename ) )
      return true;

    return false;
  }

  private static String nameWithoutExtension( final String fileName )
  {
    final int lastIndexOf = fileName.lastIndexOf( '.' );
    if( lastIndexOf == -1 )
      return fileName;

    return fileName.substring( 0, lastIndexOf );
  }
}