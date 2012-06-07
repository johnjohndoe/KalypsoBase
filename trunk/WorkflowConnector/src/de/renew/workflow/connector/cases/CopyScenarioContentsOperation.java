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
package de.renew.workflow.connector.cases;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * @author Gernot Belger
 */
final class CopyScenarioContentsOperation extends WorkspaceModifyOperation
{
  private final IFolder m_parentFolder;

  private final IFolder m_newFolder;

  private final List<IFolder> m_scenarioFolders;

  private final IDerivedScenarioCopyFilter m_filter;

  CopyScenarioContentsOperation( final ISchedulingRule rule, final IFolder parentFolder, final IFolder newFolder, final List<IFolder> scenarioFolders, final IDerivedScenarioCopyFilter filter )
  {
    super( rule );

    m_parentFolder = parentFolder;
    m_newFolder = newFolder;
    m_scenarioFolders = scenarioFolders;
    m_filter = filter;
  }

  @Override
  protected void execute( final IProgressMonitor monitor ) throws CoreException
  {
    final SubMonitor submonitor = SubMonitor.convert( monitor, getTotalChildCount( m_parentFolder ) );
    m_parentFolder.accept( new IResourceVisitor()
    {
      @Override
      public boolean visit( final IResource resource ) throws CoreException
      {
        return doVisitResource( resource, submonitor );
      }
    } );
  }

  protected boolean doVisitResource( final IResource resource, final SubMonitor submonitor ) throws CoreException
  {
    if( m_parentFolder.equals( resource ) )
    {
      return true;
    }
    else if( m_scenarioFolders.contains( resource ) )
    {
      // ignore scenario folder
      return false;
    }

    if( m_filter.copy( resource ) )
    {
      final IPath parentFolderPath = m_parentFolder.getFullPath();
      final IPath resourcePath = resource.getFullPath();

      final IPath relativePath = resourcePath.removeFirstSegments( parentFolderPath.segments().length );

      if( resource instanceof IFolder )
      {
        m_newFolder.getFolder( relativePath ).create( true, true, submonitor.newChild( 1 ) );
      }
      else if( resource instanceof IFile )
      {
        resource.copy( m_newFolder.getFullPath().append( relativePath ), true, submonitor.newChild( 1 ) );
      }

      return true;
    }

    if( !m_filter.copy( resource ) && resource instanceof IFolder )
      return false;

    return true;
  }

  private int getTotalChildCount( final IContainer container )
  {
    try
    {
      final IResource[] members = container.members();
      int count = 0;
      for( int i = 0; i < members.length; i++ )
      {
        if( !m_filter.copy( members[i] ) )
          continue;

        if( members[i].getType() == IResource.FILE )
          count++;
        else
          count += getTotalChildCount( (IContainer) members[i] );
      }
      return count;
    }
    catch( final CoreException ex )
    {
      return 0;
    }
  }
}