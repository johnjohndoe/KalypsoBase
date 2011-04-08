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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.contribs.java.io.filter.BasenameFilenameFilter;

/**
 * @author Holger Albert
 */
public class ShapeUtilities
{
  /**
   * The constructor.
   */
  public ShapeUtilities( )
  {
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
  public static String[] copyShape( File shapeFile, File destinationDirectory, String destinationBasename ) throws IOException
  {
    /* The shape parameter is not allowed to be a directory. */
    if( shapeFile.isDirectory() )
      throw new IllegalArgumentException( "The provided shape path points to a directory..." );

    /* The destination parameter must be a directory. */
    if( !destinationDirectory.isDirectory() )
      throw new IllegalArgumentException( "The provided destination path points to a file..." );

    /* Source. */
    File shapeDirectory = new File( FilenameUtils.getFullPath( shapeFile.getAbsolutePath() ) );

    /* Get all filenames of the files from shape (e.g. get all filenames with the same basename specified). */
    String[] filenames = shapeDirectory.list( new BasenameFilenameFilter( FilenameUtils.getBaseName( shapeFile.getName() ) ) );

    /* Copy all found files. */
    List<String> files = new ArrayList<String>();
    for( int i = 0; i < filenames.length; i++ )
    {
      /* Get the filename. */
      String filename = filenames[i];

      /* The filenames. */
      String sourceFilename = filename;
      String destinationFilename = filename;

      /* If a destination basename is given, use it. */
      if( destinationBasename != null && destinationBasename.length() > 0 )
      {
        String extension = FilenameUtils.getExtension( filename );
        destinationFilename = destinationBasename + "." + extension;
      }

      /* The files. */
      File source = new File( shapeDirectory, sourceFilename );
      File destination = new File( destinationDirectory, destinationFilename );

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
  public static void copyShape( IFile shape, IFolder destinationFolder, String destinationBasename ) throws Exception
  {
    /* Convert to normal files. */
    File shapeFile = shape.getLocation().toFile();
    File destinationDirectory = destinationFolder.getLocation().toFile();

    /* Copy. */
    String[] files = copyShape( shapeFile, destinationDirectory, destinationBasename );

    /* Refresh the files. */
    for( int i = 0; i < files.length; i++ )
    {
      /* Get the file. */
      String file = files[i];

      /* Get the destination file. */
      IFile destinationFile = destinationFolder.getFile( file );

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
  public static void deleteShape( File shapeFile )
  {
    /* The shape parameter is not allowed to be a directory. */
    if( shapeFile.isDirectory() )
      throw new IllegalArgumentException( "The provided shape path points to a directory..." );

    /* The shape directory. */
    File shapeDirectory = new File( FilenameUtils.getFullPath( shapeFile.getAbsolutePath() ) );

    /* Get all filenames of the files from shape (e.g. get all filenames with the same basename specified). */
    String[] filenames = shapeDirectory.list( new BasenameFilenameFilter( FilenameUtils.getBaseName( shapeFile.getName() ) ) );

    /* Delete all found files. */
    for( int i = 0; i < filenames.length; i++ )
    {
      /* Get the filename. */
      String filename = filenames[i];

      /* The files. */
      File file = new File( shapeDirectory, filename );

      /* Delete. */
      file.delete();
    }
  }
}