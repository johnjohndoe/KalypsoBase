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
package org.kalypso.commons.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * This class contains functions for dealing with excel files.
 *
 * @author Holger Albert
 */
public final class ExcelUtilities
{
  private ExcelUtilities( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * This function creates a excel file.
   *
   * @return The object, containing the data of the excel file.
   */
  public static HSSFWorkbook create( )
  {
    /* Create a new workbook. */
    return new HSSFWorkbook();
  }

  /**
   * This function loads a excel file.
   *
   * @param excelFile
   *          The excel file, to be loaded.
   * @return The object, containing the data of the excel file.
   */
  public static HSSFWorkbook load( final File excelFile ) throws IOException
  {
    /* Create the poi file system. */
    final POIFSFileSystem fs = new POIFSFileSystem( new FileInputStream( excelFile ) );

    /* Create the workbook. */
    final HSSFWorkbook wb = new HSSFWorkbook( fs );

    return wb;
  }

  /**
   * This function saves a excel file.
   *
   * @param wb
   *          The workbook, to be saved.
   * @param excelFile
   *          The excel file, to be saved.
   */
  public static void save( final HSSFWorkbook wb, final File excelFile ) throws IOException
  {
    /* The output stream. */
    FileOutputStream outputStream = null;

    try
    {
      /* Create the output stream. */
      outputStream = new FileOutputStream( excelFile );

      /* Write the excel file. */
      wb.write( outputStream );
    }
    finally
    {
      /* Close the output stream. */
      IOUtils.closeQuietly( outputStream );
    }
  }

  /**
   * This function saves a excel file.
   *
   * @param wb
   *          The workbook, to be saved.
   * @param outputStream
   *          The output stream. The stream will not be closed.
   */
  public static void save( final HSSFWorkbook wb, final OutputStream outputStream ) throws IOException
  {
    /* Write to the output stream. */
    wb.write( outputStream );
  }
}