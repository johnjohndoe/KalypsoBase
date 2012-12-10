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
package org.kalypso.ogc.gml;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.serialize.GmlSerializer;

import com.google.common.base.Charsets;

/**
 * @author Gernot Belger
 */
public class GmlWorkspaceProvider extends AbstractGmlWorkspaceProvider
{
  private final CommandableWorkspace m_workspace2;

  public GmlWorkspaceProvider( final CommandableWorkspace workspace )
  {
    m_workspace2 = workspace;
  }

  @Override
  public void startLoading( )
  {
    setWorkspace( m_workspace2, Status.OK_STATUS );
  }

  @Override
  public void save( final IProgressMonitor monitor ) throws CoreException
  {
    final CommandableWorkspace workspace = getWorkspace();
    if( workspace == null )
      return;

    final URL context = workspace.getContext();
    final IFile workspaceFile = ResourceUtilities.findFileFromURL( context );
    final File javaFile = FileUtils.toFile( context );

    try
    {
      if( workspaceFile != null )
        GmlSerializer.saveWorkspace( workspace, workspaceFile );
      else if( javaFile != null )
        GmlSerializer.serializeWorkspace( javaFile, workspace, Charsets.UTF_8.name() );
      else
      {
        // nothing to do, throw an exception?
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), "Failed to save workspace", e );
      throw new CoreException( status );
    }
  }
}