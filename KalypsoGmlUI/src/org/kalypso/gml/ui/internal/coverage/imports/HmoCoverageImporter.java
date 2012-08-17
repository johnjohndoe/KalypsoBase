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
package org.kalypso.gml.ui.internal.coverage.imports;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.java.io.FilePattern;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.MultiSurfaceCoverage;

/**
 * Imports a BCE .hmo file as coverage.
 *
 * @author Gernot Belger
 */
public class HmoCoverageImporter implements ICoverageImporter
{
  @Override
  public FilePattern getFilePattern( )
  {
    return new FilePattern( "*.hmo", "BCE HMO-Files" ); //$NON-NLS-1$
  }

  @Override
  public ICoverage importCoverage( final ICoverageCollection coverageContainer, final File dataFile, final String crs, final IContainer dataContainer, final IProgressMonitor monitor ) throws CoreException
  {
    final File tempFile = FileUtilities.getNewTempFile( "hmoConverter", "gml" );//$NON-NLS-1$ //$NON-NLS-2$

    try
    {
      // TODO: read hmo and convert to GM_Triangulated surface

      // TODO: determine bbox/boundary and set as domainSet

      /* add as MultiSurface-Coverage */
      final IFeatureBindingCollection<ICoverage> coverages = coverageContainer.getCoverages();
      final ICoverage newCoverage = coverages.addNew( MultiSurfaceCoverage.FEATURE_MULTI_SURFACE_COVERAGE );

      // TODO: move gm_triangulated surface into workspace
      final File targetDir = dataContainer.getLocation().toFile();
      final File targetFile = AbstractGridCoverageImporter.createTargetFile( dataFile, targetDir, "gml" ); //$NON-NLS-1$

      FileUtils.moveFile( tempFile, targetFile );

      /* Fire model event */
      final GMLWorkspace workspace = coverageContainer.getWorkspace();
      workspace.fireModellEvent( new FeatureStructureChangeModellEvent( workspace, coverageContainer, newCoverage, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD ) );

      return newCoverage;
    }
    catch( final IOException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), "Failed to convert triangulated surface", e );
      throw new CoreException( status );
    }
    finally
    {
      FileUtils.deleteQuietly( tempFile );
    }
  }
}