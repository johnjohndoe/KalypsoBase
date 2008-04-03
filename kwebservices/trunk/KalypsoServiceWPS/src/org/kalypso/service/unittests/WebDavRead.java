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
package org.kalypso.service.unittests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.provider.webdav.WebdavFileProvider;
import org.junit.Test;
import org.kalypso.commons.java.io.FileUtilities;

/**
 * Testcase for reading a file from a webdav using vfs.
 * 
 * @author Holger Albert
 */
public class WebDavRead
{
  /**
   * This function tries to copy a file from a webdav.
   */
  @Test
  public void testWebDavRead( ) throws IOException
  {
    DefaultFileSystemManager manager = new DefaultFileSystemManager();
    manager.addProvider( "webdav", new WebdavFileProvider() );
    manager.addProvider( "file", new DefaultLocalFileProvider() );
    manager.init();

    FileObject davFile = manager.resolveFile( "webdav://albert:gnimfe@ibpm.bjoernsen.de/dav/pub/Test/test.txt" );
    Assert.assertNotNull( davFile );

    File file = new File( FileUtilities.TMP_DIR, "davRead.txt" );
    FileObject tmpFile = manager.toFileObject( file );
    Assert.assertNotNull( tmpFile );

    InputStream is = null;
    OutputStream os = null;

    try
    {
      is = davFile.getContent().getInputStream();
      os = tmpFile.getContent().getOutputStream();

      /* Copying ... */
      IOUtils.copy( is, os );

      is.close();
      os.close();
    }
    finally
    {
      IOUtils.closeQuietly( is );
      IOUtils.closeQuietly( os );
    }

    Assert.assertTrue( tmpFile.exists() );
  }

  /**
   * This function creates the JUnit4TestAdapter.
   * 
   * @return JUnit4TestAdapter
   */
  public static junit.framework.Test suite( )
  {
    return new JUnit4TestAdapter( WebDavRead.class );
  }
}