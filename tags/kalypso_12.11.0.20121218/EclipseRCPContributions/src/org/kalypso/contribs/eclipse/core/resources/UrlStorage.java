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
package org.kalypso.contribs.eclipse.core.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.internal.EclipseRCPContributionsPlugin;

/**
 * A {@link IStorage} implementation based on {@link URL}.<br/>
 * The storage contains an additional context {@link URL} that may serve as alternative context.
 *
 * @author Gernot Belger
 */
public class UrlStorage extends PlatformObject implements IStorageWithContext
{
  private final URL m_content;

  private final URL m_context;

  public UrlStorage( final URL content, final URL context )
  {
    m_content = content;
    m_context = context;
  }

  @Override
  public URL getContext( )
  {
    return m_context;
  }

  @Override
  public InputStream getContents( ) throws CoreException
  {
    try
    {
      return m_content.openStream();
    }
    catch( final IOException e )
    {
      e.printStackTrace();

      final String message = String.format( "Failed to open resource at %s", m_content );
      final IStatus status = new Status( IStatus.ERROR, EclipseRCPContributionsPlugin.ID, message, e );
      throw new CoreException( status );
    }
  }

  @Override
  public IPath getFullPath( )
  {
    return null;
  }

  @Override
  public String getName( )
  {
    final String path = m_content.getPath();
    return FilenameUtils.getName( FilenameUtils.removeExtension( path ) );
  }

  @Override
  public boolean isReadOnly( )
  {
    return true;
  }
}