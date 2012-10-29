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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.runnables;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

/**
 * @author Dirk Kuch
 */
public class ContainsFileNameVisitor implements IResourceVisitor
{
  private final String m_file;

  private final IFolder m_base;

  private final boolean m_recursive;

  private boolean m_foundEqualName = false;

  public ContainsFileNameVisitor( final IFolder base, final String file, final boolean recursive )
  {
    m_base = base;
    m_recursive = recursive;
    m_file = FilenameUtils.removeExtension( FilenameUtils.getBaseName( file ) );
  }

  @Override
  public boolean visit( final IResource resource )
  {
    if( resource instanceof IFile )
    {
      final String name = FilenameUtils.removeExtension( resource.getName() );
      if( m_file.equals( name ) )
        m_foundEqualName = true;
    }

    if( !m_recursive && resource instanceof IFolder )
      return ((IFolder) resource).equals( m_base );

    return false;
  }

  public boolean containsEqualFileName( )
  {
    return m_foundEqualName;
  }
}
