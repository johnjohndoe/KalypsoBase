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
package org.kalypso.gml.ui.internal.shape;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.databinding.validation.TypedValidator;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.shape.ShapeFile;

/**
 * @author Gernot Belger
 */
public class PathShapeExistsValidator extends TypedValidator<IPath>
{
  private static final String DEFAULT_MESSAGE = Messages.getString( "PathShapeExistsValidator_0" ); //$NON-NLS-1$

  public PathShapeExistsValidator( final int severity )
  {
    this( severity, DEFAULT_MESSAGE );
  }

  public PathShapeExistsValidator( final int severity, final String message )
  {
    super( IPath.class, severity, message );
  }

  /**
   * @see org.kalypso.gml.ui.internal.shape.TypedValidator#doValidate(java.lang.Object)
   */
  @Override
  protected IStatus doValidate( final IPath value ) throws CoreException
  {
    if( value == null )
      return Status.OK_STATUS;

    final IPath basePath = value.removeFileExtension();
    // If path too short, return: it is not our job to validate that
    if( basePath.segmentCount() < 2 )
      return Status.OK_STATUS;

    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IWorkspaceRoot root = workspace.getRoot();

    checkFileExists( root, basePath, ShapeFile.EXTENSION_SHP );
    checkFileExists( root, basePath, ShapeFile.EXTENSION_DBF );
    checkFileExists( root, basePath, ShapeFile.EXTENSION_SHX );

    return Status.OK_STATUS;
  }

  private void checkFileExists( final IWorkspaceRoot root, final IPath basePath, final String extension ) throws CoreException
  {
    final String ext = StringUtils.substringAfter( extension, "." ); //$NON-NLS-1$

    final IPath path = basePath.addFileExtension( ext );

    final IFile file = root.getFile( path );
    if( file.exists() )
      fail();
  }
}
