/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.contribs.java.io.visitor;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.kalypso.contribs.java.io.FileVisitor;

/**
 * Collects all visited files which are accpeted by the given {@link java.io.FileFilter}.
 * 
 * @author belger
 */
public class FileFilterVisitor implements FileVisitor
{
  private final List<File> m_result = new ArrayList<File>();

  private final FileFilter m_fileFilter;

  private final boolean m_recurseIntoAccepted;

  private final boolean m_recurseIntoNonAccepted;

  /**
   * @param recurseIntoNonMatched
   *          If false, recursion will stop on resources which did not match the pattern.
   * @param recurseIntoMatched
   *          If false, recursion will stop on resources which did match the pattern.
   */
  public FileFilterVisitor( final FileFilter fileFilter, final boolean recurseIntoAccepted, final boolean recurseIntoNonAccepted )
  {
    m_fileFilter = fileFilter;
    m_recurseIntoAccepted = recurseIntoAccepted;
    m_recurseIntoNonAccepted = recurseIntoNonAccepted;
  }

  public File[] getResult( )
  {
    return m_result.toArray( new File[m_result.size()] );
  }

  public FileFilter getFileFilter( )
  {
    return m_fileFilter;
  }

  /**
   * @see org.kalypso.contribs.java.io.FileVisitor#visit(java.io.File)
   */
  public boolean visit( final File file )
  {
    if( m_fileFilter.accept( file ) )
    {
      m_result.add( file );

      return m_recurseIntoAccepted;
    }

    return m_recurseIntoNonAccepted;
  }

}
