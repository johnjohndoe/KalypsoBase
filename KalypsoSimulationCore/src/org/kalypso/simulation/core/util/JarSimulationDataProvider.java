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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.kalypso.simulation.core.ISimulationDataProvider;
import org.kalypso.simulation.core.SimulationDataPath;
import org.kalypso.simulation.core.internal.queued.ModelspecData;

/**
 * @author belger
 */
public class JarSimulationDataProvider extends AbstractSimulationDataProvider implements ISimulationDataProvider
{
  private URL m_baseURL;

  private File m_jarfile;

  protected final DataHandler m_zipHandler;

  /**
   * @param modelspec
   *          The modelspec of the simulation. Used to determine the type of each input. If null, the default type is
   *          URL for backwards compability.
   */
  public JarSimulationDataProvider( final DataHandler zipHandler, final ModelspecData modelspec, final SimulationDataPath[] input )
  {
    super( modelspec, input );
    m_zipHandler = zipHandler;
  }

  @Override
  public void dispose( )
  {
    if( m_jarfile != null )
      m_jarfile.delete();

  }

  @Override
  protected URL getBaseURL( ) throws IOException
  {
    if( m_baseURL == null )
    {
      final File jarfile = getJarFile();
      if( jarfile == null )
        return null;

      m_baseURL = new URL( "jar:" + jarfile.toURI().toURL().toString() + "!/" );
    }

    return m_baseURL;
  }

  private File getJarFile( ) throws IOException
  {
    if( m_jarfile == null )
    {
      FileOutputStream jarstream = null;
      InputStream handlerStream = null;
      try
      {
        handlerStream = m_zipHandler.getInputStream();
        m_jarfile = File.createTempFile( "CalcJobInputData", ".jar" );
        m_jarfile.deleteOnExit();

        jarstream = new FileOutputStream( m_jarfile );
        IOUtils.copy( handlerStream, jarstream );
      }
      finally
      {
        IOUtils.closeQuietly( jarstream );
        IOUtils.closeQuietly( handlerStream );
      }
    }

    return m_jarfile;
  }
}
