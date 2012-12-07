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
package org.kalypso.gml.ui.coverage.imports;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.grid.IGridMetaReader;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;
import org.kalypsodeegree_impl.gml.binding.commons.CoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;

/**
 * @author Gernot Belger
 */
public abstract class AbstractGridCoverageImporter implements ICoverageImporter
{
  /**
   * Default implementation that returns the source file itself. Most import only import exactly one file.
   */
  @Override
  public File[] getSourceFiles( final File sourceFile )
  {
    return new File[] { sourceFile };
  }

  public static File createTargetFile( final File sourceFile, final File targetDir, final String suffix ) throws CoreException
  {
    final String basename = FilenameUtils.getBaseName( sourceFile.getName() );

    final String destFileName = basename + "." + suffix; //$NON-NLS-1$

    final File targetFile = new File( targetDir, destFileName );
    if( targetFile.exists() )
    {
      final String message = Messages.getString( "org.kalypso.gml.ui.wizard.grid.ImportGridUtilities.0", destFileName ); //$NON-NLS-1$
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), message ) );
    }

    return targetFile;
  }

  @Override
  public ICoverage importCoverage( final ICoverageCollection coverageContainer, final File dataFile, final String crs, final IContainer dataContainer, final IProgressMonitor monitor ) throws CoreException
  {
    final String filename = dataFile.getName();
    final String taskName = Messages.getString( "org.kalypso.gml.ui.wizard.grid.ImportGridUtilities.9", filename ); //$NON-NLS-1$
    final SubMonitor progress = SubMonitor.convert( monitor, taskName, 100 );

    try
    {
      final URL dataLocation = dataFile.toURI().toURL();

      final IGridMetaReader reader = createRasterMetaReader( dataLocation, crs );
      final IStatus valid = reader.isValid();
      if( !valid.isOK() )
        throw new CoreException( valid );

      final RectifiedGridDomain rectDomain = reader.getDomain( crs );

      ProgressUtilities.worked( progress, 5 );

      final GMLWorkspace workspace = coverageContainer.getWorkspace();
      final URL context = workspace.getContext();

      final String targetFilePath = importDataFile( dataFile, dataContainer, crs, context, progress.newChild( 90 ) );

      return addCoverage( filename, rectDomain, targetFilePath, coverageContainer, progress.newChild( 5 ) );
    }
    catch( final MalformedURLException | URIException e )
    {
      final String message = Messages.getString( "AbstractGridCoverageImporter.0" ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), message, e );
      throw new CoreException( status );
    }
  }

  @Override
  public String getTargetExtension( )
  {
    return "bin"; //$NON-NLS-1$
  }

  /**
   * Adds one coverage to a CoverageCollection
   */
  public static ICoverage addCoverage( final String name, final RectifiedGridDomain domain, final String filePath, final ICoverageCollection coverageContainer, final IProgressMonitor monitor )
  {
    final SubMonitor progress = SubMonitor.convert( monitor, 1 + 10 );

    final String extension = FilenameUtils.getExtension( filePath );
    final String mimeType = String.format( "image/%s", extension ); //$NON-NLS-1$

    final GMLWorkspace workspace = coverageContainer.getWorkspace();

    final ICoverage newCoverage = CoverageCollection.addRectifiedGridCoverage( coverageContainer, domain, filePath, mimeType );
    newCoverage.setName( name );

    ProgressUtilities.worked( progress, 1 );

    /* Fire model event */
    workspace.fireModellEvent( new FeatureStructureChangeModellEvent( workspace, coverageContainer, newCoverage, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD ) );

    return newCoverage;
  }

  private String importDataFile( final File dataFile, final IContainer dataContainer, final String crs, final URL context, final IProgressMonitor monitor ) throws URIException, MalformedURLException, CoreException
  {
    try
    {
      final File targetDir = dataContainer.getLocation().toFile();

      final String targetFilename = doImportData( dataFile, targetDir, crs, monitor );

      final IFile targetFile = dataContainer.getFile( Path.fromPortableString( targetFilename ) );

      return createRelativeGridPath( context, targetFile );
    }
    finally
    {
      dataContainer.refreshLocal( IResource.DEPTH_ONE, null );
    }
  }

  /**
   * @return The filename of the imported data file (target filename).
   */
  protected abstract String doImportData( final File sourceFile, final File targetDir, final String sourceSRS, final IProgressMonitor monitor ) throws CoreException;

  public static String createRelativeGridPath( final URL context, final IFile gridFile ) throws MalformedURLException, URIException
  {
    final IFile contextFile = ResourceUtilities.findFileFromURL( context );

    final IPath gridPath = gridFile.getFullPath();

    if( contextFile != null )
    {
      final IPath contextFolderPath = contextFile.getFullPath().removeLastSegments( 1 );
      final IPath relativePath = gridPath.makeRelativeTo( contextFolderPath );
      return relativePath.toPortableString();
    }

    final IFolder contextFolder = ResourceUtilities.findFolderFromURL( context );
    if( contextFolder != null )
    {
      final IPath contextFolderPath = contextFolder.getFullPath();
      final IPath relativePath = gridPath.makeRelativeTo( contextFolderPath );
      return relativePath.toPortableString();
    }

    final URL outputGridUrl = ResourceUtilities.createURL( gridFile );
    return outputGridUrl.toExternalForm();
  }

  protected abstract IGridMetaReader createRasterMetaReader( final URL data, final String sourceSRS );
}