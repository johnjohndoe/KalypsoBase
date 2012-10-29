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
package org.kalypso.gml.ui.coverage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;

/**
 * @author Thomas Jung
 */
public class CoverageManagementHelper
{
  /**
   * Returns an existing grid file.<br>
   */
  private static File toJavaFile( final URL url )
  {
    /* Tries to find a file from the given url. */
    File fileFromUrl = ResourceUtilities.findJavaFileFromURL( url );

    if( fileFromUrl == null )
      fileFromUrl = FileUtils.toFile( url );

    return fileFromUrl;
  }

  /**
   * Returns the underlying grid file for a given coverage as {@link URL}.
   *
   * @return <code>null</code> of no underlying file is defined.
   */
  public static URL getFileLocation( final ICoverage coverage ) throws CoreException
  {
    final GMLWorkspace workspace = coverage.getWorkspace();

    final Object rangeSet = coverage.getRangeSet();
    if( !(rangeSet instanceof RangeSetFile) )
      return null;

    final String fileName = ((RangeSetFile) rangeSet).getFileName();

    try
    {
      return new URL( workspace.getContext(), fileName );
    }
    catch( final MalformedURLException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementHelper0" ), e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }

  /**
   * Deletes the referenced file of a coverage, if it exists.
   */
  public static IStatus deleteRangeSetFile( final ICoverage coverage )
  {
    try
    {
      final URL fileLocation = getFileLocation( coverage );
      if( fileLocation == null )
        return Status.OK_STATUS;

      deleteFile( fileLocation );
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }

    return Status.OK_STATUS;
  }

  /**
   * Deletes a file given by a URL. Useful for grid locations.
   */
  public static void deleteFile( final URL url ) throws CoreException
  {
    final File gridFile = toJavaFile( url );
    if( gridFile != null )
      gridFile.delete();

    final IFile eclipseFile = ResourceUtilities.findFileFromURL( url );
    if( eclipseFile != null )
      eclipseFile.getParent().refreshLocal( IResource.DEPTH_ONE, new NullProgressMonitor() );
  }
}
