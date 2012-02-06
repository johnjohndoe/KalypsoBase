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
package org.kalypso.commons.java.util.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

/**
 * @author Dirk Kuch
 */
public class ApacheZipUtilities
{

  public static void pack( final File archiveTarget, final File packDir ) throws IOException
  {
    pack( archiveTarget, packDir, new IFileFilter()
    {
      @Override
      public boolean accept( final File file )
      {
        return true;
      }
    } );
  }

  private static void pack( final File archiveTarget, final File packDir, final IFileFilter filter ) throws IOException
  {
    if( !packDir.isDirectory() )
    {
      return;
    }

    final BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( archiveTarget ) );
    final ZipArchiveOutputStream zos = new ZipArchiveOutputStream( bos );
    zos.setEncoding( "IBM850" ); ////$NON-NLS-1$
    zos.setMethod( ZipArchiveOutputStream.DEFLATED );

    final File[] files = packDir.listFiles();
    for( final File file : files )
    {
      processFiles( packDir, file, zos, filter );
    }

    zos.close();

    bos.flush();
    bos.close();
  }

  private static void processFiles( final File packDir, final File file, final ZipArchiveOutputStream out, final IFileFilter filter ) throws IOException
  {
    if( !filter.accept( file ) )
      return;

    if( file.isDirectory() )
    {
      final ZipArchiveEntry entry = new ZipArchiveEntry( convertFileName( packDir, file ) + "/" ); //$NON-NLS-1$
      out.putArchiveEntry( entry );
      out.closeArchiveEntry();

      final File[] files = file.listFiles();
      for( final File f : files )
      {
        processFiles( packDir, f, out, filter );
      }
    }
    else
    {
      final BufferedInputStream bis = new BufferedInputStream( new FileInputStream( file ) );

      final ZipArchiveEntry entry = new ZipArchiveEntry( file, convertFileName( packDir, file ) );
      final CRC32 crc = new CRC32();

      out.putArchiveEntry( entry );

      final byte[] buf = new byte[4096];
      int len = 0;

      while( (len = bis.read( buf )) > 0 )
      {
        out.write( buf, 0, len );
        crc.update( buf, 0, len );
      }

      entry.setCrc( crc.getValue() );

      bis.close();
      out.closeArchiveEntry();
    }
  }

  private static String convertFileName( final File packDir, final File file )
  {
    final String strPackDir = packDir.getAbsolutePath();
    final String strFileDir = file.getAbsolutePath();

    if( strFileDir.contains( strPackDir ) )
    {
      String string = strFileDir.substring( strPackDir.length() + 1 );

      /**
       * openOffice don't like \ in zip archives!!!
       */
      string = string.replaceAll( "\\\\", "/" ); //$NON-NLS-1$ //$NON-NLS-2$

      return string;
    }

    return file.getName();
  }
}
