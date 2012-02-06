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

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.junit.Test;
import org.kalypso.commons.io.VFSUtilities;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.service.wps.internal.KalypsoServiceWPSDebug;

/**
 * This unittest should test a http connection, accepting every certificate.<br>
 * This test will use the HttpClient bundled in ApacheCommonsVFS.
 * 
 * @author Holger Albert
 */
public class VFSAcceptCertificate
{
  /**
   * This function will try to read a file via https to the local tmp-directory.
   */
  @Test
  public void testVFSAcceptCertificate( ) throws IOException
  {
    /* Configure proxy for testing. */
    System.setProperty( "http.proxySet", "true" );
    System.setProperty( "http.proxyHost", "proxy.bce01.de" );
    System.setProperty( "http.proxyPort", "8080" );

    /* All certificates should be accepted! */
    System.setProperty( "javax.net.ssl.keyStoreType", "JKS" );
    System.setProperty( "javax.net.ssl.keyStore", "C:/Albert/Temp/Projekte/InformDSS/Zertifikate/Client/keystore.jks" );
    System.setProperty( "javax.net.ssl.keyStorePassword", "key4ssl" );
    System.setProperty( "javax.net.ssl.trustStoreType", "JKS" );
    System.setProperty( "javax.net.ssl.trustStore", "C:/Albert/Temp/Projekte/InformDSS/Zertifikate/Server/truststore.jks" );
    System.setProperty( "javax.net.ssl.trustStorePassword", "key4ssl" );

    FileObject remoteFile = VFSUtilities.checkProxyFor( "https://WebDAV:webdav@informdss.bafg.de/webdav/results/CalcJob-0-1210650775870/simulation.log" );

    KalypsoServiceWPSDebug.DEBUG.printf( "Sending request ...\n" );

    File file = new File( FileUtilities.TMP_DIR, "vfs_test.tmp" );
    FileObject localFile = VFSUtilities.getManager().toFileObject( file );
    Assert.assertNotNull( localFile );

    VFSUtilities.copyFileTo( remoteFile, localFile );

    InputStream inputStream = localFile.getContent().getInputStream();

    Assert.assertNotNull( inputStream );

    String content = IOUtils.toString( inputStream );

    KalypsoServiceWPSDebug.DEBUG.printf( "Content:\n" );
    KalypsoServiceWPSDebug.DEBUG.printf( content + "\n" );
  }
}