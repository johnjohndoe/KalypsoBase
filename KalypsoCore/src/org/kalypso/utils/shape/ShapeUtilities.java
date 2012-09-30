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
package org.kalypso.utils.shape;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.contribs.eclipse.core.resources.FileFilterVisitor;
import org.kalypso.contribs.java.io.filter.BasenameFilenameFilter;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.dbf.IDBFField;
import org.kalypso.shape.deegree.Shape2GML;

/**
 * @author Holger Albert
 */
public final class ShapeUtilities
{
  private ShapeUtilities( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * This function copies the files of a shape to the destination folder.
   *
   * @param shapeFile
   *          One of the shape files. The others will be determined by removing the file extension and listing the files
   *          of the parent with the same basename.
   * @param destinationDirectory
   *          To this directory the shape will be copied.
   * @param destinationBasename
   *          The basename for the destination files of the shape.
   * @return The copied files (only their filenames).
   */
  public static String[] copyShape( final File shapeFile, final File destinationDirectory, final String destinationBasename ) throws IOException
  {
    /* The shape parameter is not allowed to be a directory. */
    if( shapeFile.isDirectory() )
      throw new IllegalArgumentException( Messages.getString("ShapeUtilities_0") ); //$NON-NLS-1$

    /* The destination parameter must be a directory. */
    if( !destinationDirectory.isDirectory() )
      throw new IllegalArgumentException( Messages.getString("ShapeUtilities_1") ); //$NON-NLS-1$

    /* Source. */
    final File shapeDirectory = new File( FilenameUtils.getFullPath( shapeFile.getAbsolutePath() ) );

    /* Get all filenames of the files from shape (e.g. get all filenames with the same basename specified). */
    final String[] filenames = shapeDirectory.list( new BasenameFilenameFilter( FilenameUtils.getBaseName( shapeFile.getName() ) ) );

    /* Copy all found files. */
    final List<String> files = new ArrayList<>();
    for( final String filename : filenames )
    {
      /* The filenames. */
      final String sourceFilename = filename;
      String destinationFilename = filename;

      /* If a destination basename is given, use it. */
      if( destinationBasename != null && destinationBasename.length() > 0 )
      {
        final String extension = FilenameUtils.getExtension( filename );
        destinationFilename = destinationBasename + "." + extension; //$NON-NLS-1$
      }

      /* The files. */
      final File source = new File( shapeDirectory, sourceFilename );
      final File destination = new File( destinationDirectory, destinationFilename );

      /* Copy. */
      FileUtils.copyFile( source, destination );

      /* This one was copied. */
      files.add( destinationFilename );
    }

    return files.toArray( new String[] {} );
  }

  /**
   * This function copies the files of a shape to the destination folder.
   *
   * @param shape
   *          One of the shape files. The others will be determined by removing the file extension and listing the files
   *          of the parent with the same basename.
   * @param destinationFolder
   *          To this folder the shape will be copied.
   * @param destinationBasename
   *          The basename for the destination files of the shape.
   */
  public static void copyShape( final IFile shape, final IFolder destinationFolder, final String destinationBasename ) throws Exception
  {
    /* Convert to normal files. */
    final File shapeFile = shape.getLocation().toFile();
    final File destinationDirectory = destinationFolder.getLocation().toFile();

    /* Copy. */
    final String[] files = copyShape( shapeFile, destinationDirectory, destinationBasename );

    /* Refresh the files. */
    for( final String file : files )
    {
      /* Get the destination file. */
      final IFile destinationFile = destinationFolder.getFile( file );

      /* Refresh the destination file. */
      destinationFile.refreshLocal( IResource.DEPTH_ZERO, new NullProgressMonitor() );
    }
  }

  /**
   * This function deletes the files of a shape.
   *
   * @param shapeFile
   *          One of the shape files. The others will be determined by removing the file extension and listing the files
   *          of the parent with the same basename.
   */
  public static void deleteShape( final File shapeFile )
  {
    /* The shape parameter is not allowed to be a directory. */
    if( shapeFile.isDirectory() )
      throw new IllegalArgumentException( Messages.getString("ShapeUtilities_3") ); //$NON-NLS-1$

    /* The shape directory. */
    final File shapeDirectory = new File( FilenameUtils.getFullPath( shapeFile.getAbsolutePath() ) );

    /* Get all filenames of the files from shape (e.g. get all filenames with the same basename specified). */
    final String[] filenames = shapeDirectory.list( new BasenameFilenameFilter( FilenameUtils.getBaseName( shapeFile.getName() ) ) );

    /* Delete all found files. */
    for( final String filename : filenames )
    {
      /* The files. */
      final File file = new File( shapeDirectory, filename );

      /* Delete. */
      file.delete();
    }
  }

  /**
   * Fetches the names of all attributes from a shape file. Closes the shape afterwards.
   *
   * @param shapeBasePath
   *          The full path to the shape file (without .shp extension).
   */
  public static String[] readShapeAttributes( final String shapeBasePath ) throws IOException, DBaseException
  {
    try (final ShapeFile shape = new ShapeFile( shapeBasePath, Charset.defaultCharset(), FileMode.READ ))
    {
      final IDBFField[] fields = shape.getFields();
      final String[] propertyNames = new String[fields.length];
      for( int i = 0; i < propertyNames.length; i++ )
        propertyNames[i] = fields[i].getName();
      return propertyNames;
    }
  }

  /**
   * This function loads the shape and returns the feature type of its features.
   *
   * @param shapeFile
   *          The path of the shape file without extension.
   * @return The feature type.
   */
  public static IFeatureType findFeatureType( final String shapeFile ) throws IOException, DBaseException
  {
    try (ShapeFile sf = new ShapeFile( shapeFile, Charset.defaultCharset(), FileMode.READ ))
    {
      final ShapeType shapeType = sf.getShapeType();
      final IDBFField[] fields = sf.getFields();

      // TODO: as before, but this is still strange
      final String key = Integer.toString( shapeFile.hashCode() );

      return Shape2GML.createFeatureType( key, shapeType, fields );
    }
  }

  /**
   * Find all files of a shape file (i.e. .shp, .dbf and ..shx), but only if they really exist.
   * 
   * @param shapeFile
   *          Path to the file with extension '.shp'.
   */
  public static IFile[] getExistingShapeFiles( final IFile shapeFile ) throws CoreException
  {
    final IContainer parent = shapeFile.getParent();
    final IPath basePath = shapeFile.getFullPath().removeFileExtension();

    final String name = basePath.lastSegment();
    final FileFilter filter = new NameFileFilter( new String[] { name + ShapeFile.EXTENSION_SHP, name + ShapeFile.EXTENSION_DBF, name + ShapeFile.EXTENSION_SHX }, IOCase.SYSTEM );
    final FileFilterVisitor visitor = new FileFilterVisitor( filter );
    parent.accept( visitor );
    return visitor.getFiles();
  }
}