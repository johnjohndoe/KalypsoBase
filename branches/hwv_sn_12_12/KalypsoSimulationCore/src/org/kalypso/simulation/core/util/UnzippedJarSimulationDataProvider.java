/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.internal.queued.ModelspecData;

/**
 * @author belger
 */
public class UnzippedJarSimulationDataProvider extends JarSimulationDataProvider
{
  private URL m_baseURL;

  private File m_tmpdir;

  /**
   * @param modelspec
   *          The modelspec of the simulation. Used to determine the type of each input. If null, the default type is
   *          URL for backwards compability.
   */
  public UnzippedJarSimulationDataProvider( final DataHandler zipHandler, final ModelspecData modelspec, final SimulationDataPath[] input )
  {
    super( zipHandler, modelspec, input );
  }

  @Override
  public void dispose( )
  {
    if( m_tmpdir != null )
      FileUtilities.deleteRecursive( m_tmpdir );
  }

  @Override
  protected URL getBaseURL( ) throws IOException
  {
    if( m_baseURL == null )
    {
      final File tmpdir = getTmpDir();
      if( tmpdir == null )
        return null;

      m_baseURL = tmpdir.toURI().toURL();
    }

    return m_baseURL;
  }

  private File getTmpDir( ) throws IOException
  {
    if( m_tmpdir == null )
    {
      BufferedInputStream handlerStream = null;
      try
      {
        handlerStream = new BufferedInputStream( m_zipHandler.getInputStream() );

        m_tmpdir = FileUtilities.createNewTempDir( "CalcJobInputData" ); //$NON-NLS-1$

        ZipUtilities.unzip( handlerStream, m_tmpdir );
      }
      catch( final IOException e )
      {
        // reset tmpdir on any error
        m_tmpdir = null;

        throw e;
      }
      finally
      {
        IOUtils.closeQuietly( handlerStream );
      }
    }

    return m_tmpdir;
  }

}
