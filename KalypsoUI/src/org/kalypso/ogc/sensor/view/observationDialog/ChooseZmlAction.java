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
package org.kalypso.ogc.sensor.view.observationDialog;

import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.PathUtils;
import org.kalypso.contribs.eclipse.ui.dialogs.ResourceListSelectionDialogExt;

/**
 * hack to support local references (doemming)
 * 
 * @author Gernot Belger
 */
public abstract class ChooseZmlAction extends Action
{
  private final ObservationViewer m_viewer;

  private final String m_dialogTitle;

  public ChooseZmlAction( final ObservationViewer viewer, final String dialogTitle )
  {
    m_viewer = viewer;
    m_dialogTitle = dialogTitle;
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final URL context = m_viewer.getContext();
    final Shell shell = m_viewer.getShell();

    final IContainer baseDir = getBaseDir();
    final ResourceListSelectionDialogExt dialog = new ResourceListSelectionDialogExt( shell, baseDir, IResource.FILE ); //$NON-NLS-1$
    dialog.setSelector( new ZmlResourceSelector() );
    dialog.setInitialPattern( "*" ); //$NON-NLS-1$

    dialog.setTitle( m_dialogTitle );
    dialog.setBlockOnOpen( true );

    if( dialog.open() != Window.OK )
      return;

    final Object[] result = dialog.getResult();
    if( result.length == 0 )
      return;

    if( !(result[0] instanceof IFile) )
      return;

    final IFile resultFile = (IFile)result[0];

    final IPath resultPath = resultFile.getFullPath();

    final IPath contextPath = findContextPath( context );

    final IPath relativePath = PathUtils.makeRelativ( contextPath, resultPath );

    final String href = relativePath.toString();

    m_viewer.setInput( href, m_viewer.getShow() );
  }

  private IPath findContextPath( final URL context )
  {
    final IPath contextPath = ResourceUtilities.findPathFromURL( context );

    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IResource contextResource = root.findMember( contextPath, false );
    // No valid context, return null so we get an absolute path
    if( contextResource == null )
      return null;

    /* If it is a file, return the parent else we get ../ as relative path to this file. */
    if( contextResource instanceof IFile )
      return ((IFile)contextResource).getParent().getFullPath();

    /* It is a container, just return it's path */
    return contextPath;
  }

  protected abstract IContainer getBaseDir( );
}