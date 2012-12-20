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
package org.kalypso.contribs.java.io;

import java.io.File;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;

/**
 * A simple helper class that represents a known file pattern (like *.shp).<br/>
 * Should be reused to reduce duplicate code for file filters in file dialogs.
 *
 * @author Gernot Belger
 */
public class FilePattern
{
  private final String m_pattern;

  private final String m_filterName;

  public FilePattern( final String extension, final String filterName )
  {
    m_pattern = extension;
    m_filterName = filterName;
  }

  /**
   * The file pattern
   */
  public String getPattern( )
  {
    return m_pattern;
  }

  /**
   * The (human readable, translated) name of the file type represented by this extension, to be used in file dialogs.<br/>
   * Example: 'All Files' (Note: without the trailing '(*.*)' )
   */
  public String getFilterName( )
  {
    return m_filterName;
  }

  /**
   * The (human readable, translated) name of the file type represented by this extension, to be used in file dialogs.<br/>
   * Example: 'All Files (*.*)' (Note: including the trailing '(*.*)' )
   */
  public String getFilterLabel( )
  {
    return String.format( "%s (%s)", m_filterName, m_pattern );
  }

  public boolean matches( final File file )
  {
    return matches( file.getName() );
  }

  public boolean matches( final String filename )
  {
    final String[] wildcards = StringUtils.split( m_pattern, ';' );
    final WildcardFileFilter filter = new WildcardFileFilter( wildcards );
    return filter.accept( null, filename );
  }
}