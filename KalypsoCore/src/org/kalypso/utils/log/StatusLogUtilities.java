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
package org.kalypso.utils.log;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;

/**
 * This class contains functions for dealing with status logs.
 * 
 * @author Holger Albert
 */
public class StatusLogUtilities
{
  /**
   * The constructor.
   */
  private StatusLogUtilities( )
  {
  }

  /**
   * This function writes the stati in the status collector as geo status log to a file.
   * 
   * @param log
   *          The status collector.
   * @param file
   *          The file to write to.
   */
  public static void writeStatusLog( final IStatusCollector log, final File file ) throws CoreException
  {
    final GeoStatusLog statusLog = new GeoStatusLog( file );
    final IStatus[] allStati = log.getAllStati();
    for( final IStatus status : allStati )
      statusLog.log( status );

    statusLog.serialize();
  }

  /**
   * This function writes the stati in the status collector as geo status log to a file. It will not throw an exception.
   * 
   * @param log
   *          The status collector.
   * @param file
   *          The file to write to.
   */
  public static void writeStatusLogQuietly( final IStatusCollector log, final File file )
  {
    try
    {
      writeStatusLog( log, file );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }
  }
}