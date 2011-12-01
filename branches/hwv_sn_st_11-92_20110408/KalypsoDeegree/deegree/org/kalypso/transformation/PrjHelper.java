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
package org.kalypso.transformation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.net.HttpClientUtilities;
import org.kalypsodeegree.KalypsoDeegreePlugin;

/**
 * Helper for ESRI .prj files.
 * 
 * @author Gernot Belger
 */
public final class PrjHelper
{
  private static final String PRJ_EXTENSION = ".prj"; //$NON-NLS-1$

  private static final String PRJ_CACHE_DIR = "prjCache"; //$NON-NLS-1$

  private PrjHelper( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  public synchronized static final void fetchPrjFile( final String coordinateSystem, final File destination, final IProgressMonitor monitor ) throws CoreException
  {
    final String name = String.format( "Fetching PRJ for %s", coordinateSystem );
    monitor.beginTask( name, IProgressMonitor.UNKNOWN );

    try
    {
      final String code = CRSHelper.getEPSG( coordinateSystem );

      /* Request the .prj file from the server. */
      final IPath stateLocation = KalypsoDeegreePlugin.getDefault().getStateLocation();
      final File stateDir = stateLocation.toFile();
      final File cacheDir = new File( stateDir, PRJ_CACHE_DIR );
      final File cachedPrjFile = new File( cacheDir, code + PRJ_EXTENSION );

      if( !cachedPrjFile.exists() )
      {
        cacheDir.mkdirs();
        final URL sourceUrl = new URL( "http://spatialreference.org/ref/epsg/" + code + "/prj/" ); //$NON-NLS-1$ //$NON-NLS-2$
        monitor.subTask( String.format( "Accessing %s", sourceUrl.toString() ) );
        HttpClientUtilities.requestFileFromServer( sourceUrl, cachedPrjFile );
      }

      FileUtils.copyFile( cachedPrjFile, destination );
    }
    catch( final CoreException e )
    {
      throw throwFetchPrj( coordinateSystem, e );
    }
    catch( final MalformedURLException e )
    {
      // should never happen...
      throw throwFetchPrj( coordinateSystem, e );
    }
    catch( final IOException e )
    {
      throw throwFetchPrj( coordinateSystem, e );
    }
    finally
    {
      monitor.done();
    }
  }

  private static CoreException throwFetchPrj( final String coordinateSystem, final Throwable e )
  {
    final String msg = String.format( "Failed to retreive .prj file for coordinate system %s", coordinateSystem );
    final Status status = new Status( IStatus.WARNING, KalypsoDeegreePlugin.getID(), msg, e );
    return new CoreException( status );
  }
}