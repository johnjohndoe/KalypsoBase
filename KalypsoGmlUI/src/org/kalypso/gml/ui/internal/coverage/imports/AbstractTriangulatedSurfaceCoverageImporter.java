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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.java.io.FilePattern;
import org.kalypso.gml.processes.tin.MultiSurfaceCoverage;
import org.kalypso.gml.processes.tin.TriangulatedSurfaceFeature;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.coverage.imports.AbstractGridCoverageImporter;
import org.kalypso.gml.ui.coverage.imports.ICoverageImporter;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;

/**
 * @author Holger Albert
 */
public abstract class AbstractTriangulatedSurfaceCoverageImporter implements ICoverageImporter
{
  @Override
  public abstract FilePattern getFilePattern( );

  /**
   * Default implementation that returns the source file itself. Most import only import exactly one file.
   */
  @Override
  public File[] getSourceFiles( final File sourceFile )
  {
    return new File[] { sourceFile };
  }

  @Override
  public ICoverage importCoverage( final ICoverageCollection coverageContainer, final File dataFile, final String crs, final IContainer dataContainer, IProgressMonitor monitor ) throws CoreException
  {
    /* Monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    /* Get a temporary file. */
    final File tempFile = FileUtilities.getNewTempFile( "hmoConverter", "gmlz" );//$NON-NLS-1$ //$NON-NLS-2$

    try
    {
      /* Monitor. */
      monitor.beginTask( Messages.getString( "AbstractTriangulatedSurfaceCoverageImporter_0" ), 300 ); //$NON-NLS-1$
      monitor.subTask( Messages.getString( "AbstractTriangulatedSurfaceCoverageImporter_1" ) ); //$NON-NLS-1$

      /* Read the input data. */
      final GM_TriangulatedSurface gmSurface = readInputData( dataFile, crs, new SubProgressMonitor( monitor, 100 ) );

      /* Monitor. */
      monitor.subTask( Messages.getString( "AbstractTriangulatedSurfaceCoverageImporter_2" ) ); //$NON-NLS-1$

      /* Transform into the Kalypso coordinate system. */
      final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
      final GM_Object transformedSurface = geoTransformer.transform( gmSurface );

      /* Create the workspace. */
      final GMLWorkspace tmpWorkspace = FeatureFactory.createGMLWorkspace( TriangulatedSurfaceFeature.FEATURE_TRIANGULATED_SURFACE, null, null );
      final TriangulatedSurfaceFeature rootFeature = (TriangulatedSurfaceFeature)tmpWorkspace.getRootFeature();
      rootFeature.setTriangulatedSurface( (GM_TriangulatedSurface)transformedSurface );

      /* Save the workspace. */
      GmlSerializer.serializeWorkspace( tempFile, tmpWorkspace, "UTF-8" ); //$NON-NLS-1$

      /* Monitor. */
      monitor.worked( 100 );
      monitor.subTask( Messages.getString( "AbstractTriangulatedSurfaceCoverageImporter_4" ) ); //$NON-NLS-1$

      /* Get the target file. */
      final File targetDir = dataContainer.getLocation().toFile();
      final File targetFile = AbstractGridCoverageImporter.createTargetFile( dataFile, targetDir, "gmlz" ); //$NON-NLS-1$

      /* Add as MultiSurface-Coverage. */
      final IFeatureBindingCollection<ICoverage> coverages = coverageContainer.getCoverages();
      final ICoverage newCoverage = coverages.addNew( MultiSurfaceCoverage.FEATURE_MULTI_SURFACE_COVERAGE );
      newCoverage.setName( dataFile.getName() );

      /* Update the bounded by property. */
      final GM_Envelope boundedBy = gmSurface.getEnvelope();
      final GM_Envelope boundedByTransformed = geoTransformer.transform( boundedBy );
      newCoverage.setProperty( Feature.QN_BOUNDED_BY, boundedByTransformed );

      /* Update the range set property. */
      final String externalResource = buildRelativePath( dataContainer, coverageContainer.getWorkspace().getContext(), targetFile );
      final RangeSetFile rangeSetFile = new RangeSetFile( externalResource );
      rangeSetFile.setMimeType( "application/gml+xml" ); //$NON-NLS-1$
      newCoverage.setRangeSet( rangeSetFile );

      /* Update the envelopes. */
      newCoverage.setEnvelopesUpdated();

      /* Move the file. */
      FileUtils.moveFile( tempFile, targetFile );

      /* Fire model event. */
      final GMLWorkspace workspace = coverageContainer.getWorkspace();
      workspace.fireModellEvent( new FeatureStructureChangeModellEvent( workspace, coverageContainer, newCoverage, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD ) );

      /* Monitor. */
      monitor.worked( 100 );

      return newCoverage;
    }
    catch( final Exception e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), Messages.getString( "AbstractTriangulatedSurfaceCoverageImporter_6" ), e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
    finally
    {
      FileUtils.deleteQuietly( tempFile );
      dataContainer.refreshLocal( IResource.DEPTH_INFINITE, new NullProgressMonitor() );
      monitor.done();
    }
  }

  @Override
  public String getTargetExtension( )
  {
    return "gmlz"; //$NON-NLS-1$
  }

  private String buildRelativePath( final IContainer dataContainer, final URL context, final File targetFile ) throws URIException, MalformedURLException
  {
    final IFile file = dataContainer.getFile( Path.fromPortableString( targetFile.getName() ) );
    return AbstractGridCoverageImporter.createRelativeGridPath( context, file );
  }

  protected abstract GM_TriangulatedSurface readInputData( final File dataFile, final String crs, final IProgressMonitor monitor ) throws CoreException, MalformedURLException;
}