/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Bj�rnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universit�t Hamburg-Harburg, Institut f�r Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.core.resources;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsPlugin;

/**
 * @author belger
 */
public final class FolderUtilities
{
  /** Do not instantiate */
  private FolderUtilities( )
  {
    //
  }

  /**
   * Creates this folder and its parent folder if not yet existant.
   */
  public static void mkdirs( final IContainer folder ) throws CoreException
  {
    if( folder == null || folder.exists() )
      return;

    if( !(folder instanceof IFolder) )
      throw new CoreException( new Status( IStatus.ERROR, EclipsePlatformContributionsPlugin.getID(), 0, "Cannot mkdirs project or workspace", null ) ); //$NON-NLS-1$

    // create parents
    mkdirs( folder.getParent() );

    ((IFolder) folder).create( false, true, new NullProgressMonitor() );
  }

  public static IFolder createUnusedFolder( final IFolder parentFolder, final String prefix )
  {
    int i = 0;
    while( true )
    {
      final IFolder f = parentFolder.getFolder( prefix + i );
      if( !f.exists() )
        return f;

      i++;
    }
  }
}