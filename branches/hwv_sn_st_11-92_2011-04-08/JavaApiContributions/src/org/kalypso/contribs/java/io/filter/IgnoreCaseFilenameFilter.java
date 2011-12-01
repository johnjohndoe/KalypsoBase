package org.kalypso.contribs.java.io.filter;

import java.io.File;
import java.io.FilenameFilter;

public final class IgnoreCaseFilenameFilter implements FilenameFilter
{
  private final String m_fileName;

  public IgnoreCaseFilenameFilter( final String fileName )
  {
    m_fileName = fileName;
  }

  @Override
  public boolean accept( final File folder, final String name )
  {
    return name.equalsIgnoreCase( m_fileName );
  }
}