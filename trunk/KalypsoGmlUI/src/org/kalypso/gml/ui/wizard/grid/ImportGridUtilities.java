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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
  public static final String[] SUPPORTED_GRID_FILE_PATTERNS = new String[] { "*.jpg;*.gif;*.tif;*.asc;*.asg;*.dat", "*.tif", "*.jpg", "*.gif", "*.asc;*.dat;*.asg", "*.*" };

  public static final String[] SUPPORTED_GRID_FILE_NAMES = new String[] { "All supported files (*.jpg;*.gif;*.tif;*.asc;*.asg;*.dat)", "TIFF image (*.tif)", "JPEG image (*.jpg)", "GIF image (*.gif)",
      "ASCII grid (*.asc, *.dat, *.asg)", "All files (*.*)" };

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

  public static File[] convertGrids( final File[] gridFiles, final String sourceCRS, final IProgressMonitor monitor ) throws CoreException
  {
    final SubMonitor progress = SubMonitor.convert( monitor, "Konvertiere binäres Format", gridFiles.length );

    /* Convert to .bin if necessary */
    final File[] convertedFiles = new File[gridFiles.length];
    for( int i = 0; i < convertedFiles.length; i++ )
    {
      try
      {
        progress.subTask( gridFiles[i].getName() );
        convertedFiles[i] = convertIfAsc( gridFiles[i], sourceCRS, progress.newChild( 1 ) );
      }
      finally
      {
        ProgressUtilities.worked( progress, 0 ); // only check for cancelled
      }
    }

    return convertedFiles;
  }

  /**
   * Converts .asc to an internal binary format.
   */
  public static File convertIfAsc( final File gridFile, final String sourceCRS, final IProgressMonitor monitor ) throws CoreException
  {
    if( gridFile.getName().toLowerCase().endsWith( ".asc" ) || gridFile.getName().toLowerCase().endsWith( ".asg" ) )
    {
      File binFile;
      try
      {
        binFile = File.createTempFile( gridFile.getName(), ".bin" );
        binFile.deleteOnExit();

        final ConvertAscii2Binary converter = new ConvertAscii2Binary( gridFile.toURI().toURL(), binFile, 2, sourceCRS );
        converter.doConvert( monitor );
      }
      catch( final IOException e )
      {
        e.printStackTrace();
        final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, "Fehler beim Erzeugen einer temporären Datei", e );
        throw new CoreException( status );
      }

      return binFile;
    }

    return gridFile;
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

  public static RectifiedGridDomain[] readDomains( final File[] selectedFiles, final String crs, final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final SubMonitor progress = SubMonitor.convert( monitor, "Lese Metadaten", selectedFiles.length );

      final RectifiedGridDomain[] domains = new RectifiedGridDomain[selectedFiles.length];

      for( int i = 0; i < domains.length; i++ )
      {
        final File file = selectedFiles[i];
        progress.subTask( file.getName() );
        final URL url = file.toURI().toURL();

        final IGridMetaReader reader = GridFileVerifier.getRasterMetaReader( url, crs );

        final IStatus valid = reader.isValid();
        if( !valid.isOK() )
          throw new CoreException( valid );

        domains[i] = getDomain( reader, crs );

        ProgressUtilities.worked( progress, 1 );
      }

      return domains;
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

}
