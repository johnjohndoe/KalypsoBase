/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.gml.ui.wizard.grid;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.PathUtils;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.grid.ConvertAscii2Binary;
import org.kalypso.grid.GridFileVerifier;
import org.kalypso.grid.IGridMetaReader;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;
import org.kalypsodeegree_impl.gml.binding.commons.CoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain.OffsetVector;

/**
 * @author Gernot Belger
 */
public class ImportGridUtilities
{
  public static final String[] SUPPORTED_GRID_FILE_PATTERNS = new String[] { "*.asc;*.asg;*.dat;*.bin", "*.asc;*.dat;*.asg", "*.bin", "*.*" };

  public static final String[] SUPPORTED_GRID_FILE_NAMES = new String[] { "All supported formats (*.asc;*.asg;*.dat;*.bin)", "ESRI ASCII Grid-Files (*.asc, *.dat, *.asg)",
      "Kalypso Grid-Files (*.bin)", "All files (*.*)" };

  private ImportGridUtilities( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  /**
   * Chooses grid files from the file system.
   */
  public static File[] chooseFiles( final Shell shell, final String dialogTitle, final String initialDir )
  {
    final FileDialog dialog = new FileDialog( shell, SWT.OPEN | SWT.MULTI );
    dialog.setFilterExtensions( ImportGridUtilities.SUPPORTED_GRID_FILE_PATTERNS );
    dialog.setFilterNames( ImportGridUtilities.SUPPORTED_GRID_FILE_NAMES );
    dialog.setFilterIndex( 0 );
    dialog.setText( dialogTitle );
    dialog.setFilterPath( initialDir );

    final String result = dialog.open();
    if( result == null )
      return null;

    final String[] fileNames = dialog.getFileNames();
    final String filterPath = dialog.getFilterPath();
    final File dir = new File( filterPath );
    final File[] files = new File[fileNames.length];
    for( int i = 0; i < fileNames.length; i++ )
      files[i] = new File( dir, fileNames[i] );

    return files;
  }

  /**
   * Converts .asc to an internal binary format.
   */
  private static File convertIfAsc( final File gridFile, final File destDir, final String sourceCRS, final IProgressMonitor monitor ) throws CoreException
  {
    // ESRI-ASCII Files: convert to .ascbin
    File destFile = null;

    try
    {
      final String gridFileName = gridFile.getName();
      final String basename = FilenameUtils.getBaseName( gridFileName );
      final String extension = FilenameUtils.getExtension( gridFileName ).toLowerCase();
      if( "asc".equals( extension ) || "asg".equals( extension ) || "dat".equals( extension ) )
      {
        try
        {
          final String destFileName = basename + ".bin";
          destFile = new File( destDir, destFileName );
          if( destFile.exists() )
          {
            destFile = null; // elese if will be deleted
            throw new CoreException( StatusUtilities.createStatus( IStatus.ERROR, String.format( "Destination file already exists: %s", destFileName ), null ) );
          }
          final ConvertAscii2Binary converter = new ConvertAscii2Binary( gridFile.toURI().toURL(), destFile, 2, sourceCRS );
          converter.doConvert( monitor );
          return destFile;
        }
        catch( final MalformedURLException e )
        {
          e.printStackTrace();
          final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, "Fehler beim Konvertieren von .asc nach .bin", e );
          throw new CoreException( status );
        }
      }

      // Kalypso-BIN Files: just copy
      if( "bin".equals( extension ) )
      {
        try
        {
          destFile = new File( destDir, gridFileName );
          if( destFile.exists() )
          {
            destFile = null; // elese if will be deleted
            throw new CoreException( StatusUtilities.createStatus( IStatus.ERROR, String.format( "Destination file already exists: %s", destFile ), null ) );
          }
          FileUtils.copyFile( gridFile, destFile );
          return destFile;
        }
        catch( final IOException e )
        {
          e.printStackTrace();
          final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, "Fehler beim Kopieren der .bin Datei", e );
          throw new CoreException( status );
        }
      }

      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, String.format( "Unknown grid format: %s", extension ), null );
      throw new CoreException( status );
    }
    catch( final CoreException e )
    {
      if( destFile != null )
        destFile.delete();

      throw e;
    }
  }

  /**
   * Copies an external file into a container of the workspace.<br>
   * If a file with the same name already exists in the workspace, the user will be asked what to do.
   */
  public static IFile[] importExternalFiles( final Shell shell, final File[] files, final IContainer targetFolder, final IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException, CoreException
  {
    final IOverwriteQuery overwriteQuery = new IOverwriteQuery()
    {
      public String queryOverwrite( final String pathString )
      {
        if( MessageDialog.openQuestion( shell, "Datei import", "Die Datei " + pathString + " exisitert bereits.\nSoll Sie überschrieben werden?" ) )
          return IOverwriteQuery.YES;
        return IOverwriteQuery.NO;
      }
    };

    final List<File> fileList = Arrays.asList( files );
    final ImportOperation operation = new ImportOperation( targetFolder.getFullPath(), FileSystemStructureProvider.INSTANCE, overwriteQuery, fileList );
    operation.setOverwriteResources( false );
    operation.setCreateContainerStructure( false );
    operation.setContext( shell );
    operation.run( monitor );

    final IStatus status = operation.getStatus();
    if( !status.isOK() )
      throw new CoreException( status );

    final IFile[] result = new IFile[files.length];
    for( int i = 0; i < result.length; i++ )
      result[i] = targetFolder.getFile( new Path( files[i].getName() ) );

    return result;
  }

  /**
   * Adds one coverage to a CoverageCollection
   */
  public static ICoverage addCoverage( final String name, final RectifiedGridDomain domain, final IFile gridFile, final ICoverageCollection coverages, final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final SubMonitor progress = SubMonitor.convert( monitor, 1 + 10 );

      final String mimeType = "image/" + gridFile.getFileExtension();

      final Feature coveragesFeature = coverages.getFeature();
      final GMLWorkspace workspace = coveragesFeature.getWorkspace();

      final URL context = workspace.getContext();
      final String filePath = createRelativeGridPath( context, gridFile );

      final ICoverage newCoverage = CoverageCollection.addCoverage( coverages, domain, filePath, mimeType );
      newCoverage.setName( name );

      ProgressUtilities.worked( progress, 1 );

      /* Fire model event */
      workspace.fireModellEvent( new FeatureStructureChangeModellEvent( workspace, coveragesFeature, newCoverage.getFeature(), FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD ) );

      return newCoverage;
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, "Ungültiger Dateipfad: " + gridFile.getFullPath().toOSString(), e );
      throw new CoreException( status );
    }
  }

  private static String createRelativeGridPath( final URL context, final IFile gridFile ) throws MalformedURLException
  {
    final IFile contextFile = ResourceUtilities.findFileFromURL( context );
    if( contextFile != null )
    {
      final IPath relativePath = PathUtils.makeRelativ( contextFile.getFullPath().removeLastSegments( 1 ), gridFile.getFullPath() );
      if( relativePath != null )
        return relativePath.toPortableString();
    }

    final IFolder contextFolder = ResourceUtilities.findFolderFromURL( context );
    if( contextFolder != null )
    {
      final IPath relativePath = PathUtils.makeRelativ( contextFolder.getFullPath(), gridFile.getFullPath() );
      if( relativePath != null )
        return relativePath.toPortableString();
    }

    final URL outputGridUrl = ResourceUtilities.createURL( gridFile );
    return outputGridUrl.toExternalForm();
  }

  /**
   * Reads the grid domain for a given grid file. Tries to determine its file-format (.asc, tif, etc.) and reads the
   * world-information accoding to the format.
   */
  public static RectifiedGridDomain readDomain( final File gridFile, final String crs ) throws CoreException
  {
    try
    {
      final URL url = gridFile.toURI().toURL();

      final IGridMetaReader reader = GridFileVerifier.getRasterMetaReader( url, crs );

      final IStatus valid = reader.isValid();
      if( !valid.isOK() )
        throw new CoreException( valid );

      return getDomain( reader, crs );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, "Ungültiger Dateifpad", e );
      throw new CoreException( status );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, "Fehler beim Lesen der Raster-Metadaten", e );
      throw new CoreException( status );
    }
  }

  /**
   * TODO: this method should not be necessary: the meta-reader should return the domain and nothing else.
   *
   * @see org.kalypso.grid.IGridMetaReader#getDomain(java.lang.String)
   */
  public static RectifiedGridDomain getDomain( final IGridMetaReader reader, final String crs ) throws Exception
  {
    final double oX = reader.getOriginCornerX();
    final double oY = reader.getOriginCornerY();

    final Double[] upperLeftCorner = new Double[] { oX, oY };

    final double xx = reader.getVectorXx();
    final double xy = reader.getVectorXy();
    final double yx = reader.getVectorYx();
    final double yy = reader.getVectorYy();
    final OffsetVector offsetX = new OffsetVector( xx, xy );
    final OffsetVector offsetY = new OffsetVector( yx, yy );

    return reader.getCoverage( offsetX, offsetY, upperLeftCorner, crs );
  }

  /**
   * Imports an external grid file into a coverage collection.<br>
   * The file will be also imported into the workspace into the given {@link IFolder}.
   *
   * @param coverageCollection
   *          A new coverage-feature will be added to this collection.
   * @param gridFile
   *          The input file
   * @param name
   *          The name which wil bet set to the coverage (gml:name)
   * @param crs
   *          The Coordinate system of the grid-input-file
   * @param domain
   *          Optional: if non-<code>null</code>, this domain will be used for adding the coverage.
   * @param targetFolder
   *          The input grid gets copied/converted into this targewt folder
   */
  public static ICoverage importGrid( final ICoverageCollection coverageCollection, final File gridFile, final String name, final String crs, final IContainer targetFolder, final RectifiedGridDomain domain, final IProgressMonitor monitor ) throws CoreException
  {
    final String taskName = String.format( "Importing %s", gridFile.getName() );
    final SubMonitor progress = SubMonitor.convert( monitor, taskName, 100 );

    IFile targetFile = null;
    try
    {
      final RectifiedGridDomain rectDomain = domain == null ? ImportGridUtilities.readDomain( gridFile, crs ) : domain;
      ProgressUtilities.worked( progress, 5 );

      final File destDir = targetFolder.getLocation().toFile();
      final File destFile = convertIfAsc( gridFile, destDir, crs, progress.newChild( 85 ) );
      targetFile = targetFolder.getFile( new Path( destFile.getName() ) );

      return ImportGridUtilities.addCoverage( name, rectDomain, targetFile, coverageCollection, progress.newChild( 5 ) );
    }
    finally
    {
      if( targetFile == null )
        targetFolder.refreshLocal( IResource.DEPTH_INFINITE, progress.newChild( 5 ) );
      else
        targetFile.refreshLocal( IResource.DEPTH_ZERO, progress.newChild( 5 ) );
    }
  }

}
