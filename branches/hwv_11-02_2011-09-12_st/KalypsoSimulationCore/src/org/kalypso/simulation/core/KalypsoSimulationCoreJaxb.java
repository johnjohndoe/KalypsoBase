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
package org.kalypso.simulation.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modelspec;

/**
 * @author Belger
 */
public class KalypsoSimulationCoreJaxb
{
  private static final String STR_FAILED_TO_LOAD_SIMULATION_SPECIFICATION = "Failed to load simulation specification";

  private static final JAXBContext JC = JaxbUtilities.createQuiet( org.kalypso.simulation.core.simspec.ObjectFactory.class );

  private static Unmarshaller createUnmarshaller( )
  {
    try
    {
      return JC.createUnmarshaller();
    }
    catch( final JAXBException e )
    {
      // will not happen
      e.printStackTrace();
      return null;
    }
  }

  public static Modeldata readModeldata( final IFile file ) throws CoreException
  {
    final InputStream contents = file.getContents();
    return (Modeldata) unmarshallAndCloseStream( contents );
  }

  public static Modelspec readModelspec( final URL location ) throws CoreException
  {
    try
    {
      final InputStream contents = location.openStream();
      return (Modelspec) unmarshallAndCloseStream( contents );
    }
    catch( final IOException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoSimulationCorePlugin.getID(), STR_FAILED_TO_LOAD_SIMULATION_SPECIFICATION );
      throw new CoreException( status );
    }
  }

  private static Object unmarshallAndCloseStream( final InputStream contents ) throws CoreException
  {
    try
    {
      final Unmarshaller unmarshaller = KalypsoSimulationCoreJaxb.createUnmarshaller();
      final Object unmarshal = unmarshaller.unmarshal( contents );
      contents.close();
      return unmarshal;
    }
    catch( final JAXBException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoSimulationCorePlugin.getID(), STR_FAILED_TO_LOAD_SIMULATION_SPECIFICATION );
      throw new CoreException( status );
    }
    catch( final IOException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoSimulationCorePlugin.getID(), STR_FAILED_TO_LOAD_SIMULATION_SPECIFICATION );
      throw new CoreException( status );
    }
    finally
    {
      IOUtils.closeQuietly( contents );
    }
  }

}
