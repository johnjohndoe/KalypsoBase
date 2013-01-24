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
package org.kalypso.shape.tools;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Gernot Belger
 */
public class TmpFileHelper
{
  private final Collection<File> m_tmpFiles = new LinkedList<File>();

  private final boolean m_deleteOnExit;

  public TmpFileHelper( final boolean deleteOnExit )
  {
    m_deleteOnExit = deleteOnExit;
  }

  public File create( final String prefix, final String suffix )
  {
    final File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
    final File tmpFile = new File( tmpDir, prefix + System.currentTimeMillis() + suffix );

    addFiles( tmpFile );

    return tmpFile;
  }

  public void addFiles( final File... files )
  {
    for( final File file : files )
    {
      m_tmpFiles.add( file );
      if( m_deleteOnExit )
        file.deleteOnExit();
    }
  }
}
