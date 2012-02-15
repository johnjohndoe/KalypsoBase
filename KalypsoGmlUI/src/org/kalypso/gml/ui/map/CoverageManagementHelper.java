/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.gml.ui.map;

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
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage;

/**
 * @author Thomas Jung
 */
public class CoverageManagementHelper
{
  /**
   * Returns an existing grid file.<br>
   */
  public static File getGridFile( final URL url )
  {
    /* Tries to find a file from the given url. */
    File fileFromUrl = ResourceUtilities.findJavaFileFromURL( url );

    if( fileFromUrl == null )
      fileFromUrl = FileUtils.toFile( url );

    return fileFromUrl;
  }

  /**
   * Deletes the referenced file of a coverage, if it exists.
   */
  public static IStatus deleteGridFile( final ICoverage coverageToDelete )
  {
    final RectifiedGridCoverage coverage = (RectifiedGridCoverage) coverageToDelete;
    final Feature feature = coverage;
    final GMLWorkspace workspace = feature.getWorkspace();

    final Object rangeSet = coverage.getRangeSet();
    if( !(rangeSet instanceof RangeSetFile) )
      return Status.OK_STATUS;

    final String fileName = ((RangeSetFile) rangeSet).getFileName();

    try
    {
      final URL url = new URL( workspace.getContext(), fileName );
      final File gridFile = getGridFile( url );
      if( gridFile != null )
        gridFile.delete();

      final IFile eclipseFile = ResourceUtilities.findFileFromURL( url );
      if( eclipseFile != null )
        eclipseFile.getParent().refreshLocal( IResource.DEPTH_ONE, new NullProgressMonitor() );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      return StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementHelper0" ) ); //$NON-NLS-1$
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementHelper0" ) ); //$NON-NLS-1$
    }

    return Status.OK_STATUS;
  }

}
