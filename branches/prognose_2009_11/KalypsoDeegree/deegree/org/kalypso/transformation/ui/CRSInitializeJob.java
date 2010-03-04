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
package org.kalypso.transformation.ui;

import java.util.HashMap;
import java.util.List;

import org.deegree.model.crs.CoordinateSystem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.transformation.CRSHelper;

/**
 * This job initializes the coordinate systems.
 * 
 * @author Holger Albert
 */
public class CRSInitializeJob extends Job
{
  /**
   * The names of the coordinate systems, to initialize.
   */
  private List<String> m_names;

  /**
   * A hash of the coordinate systems.
   */
  private HashMap<String, CoordinateSystem> m_coordHash;

  /**
   * The constructor.
   * 
   * @param name
   *          The name of the job.
   * @param names
   *          The names of the coordinate systems, to initialize.
   */
  public CRSInitializeJob( String name, List<String> names )
  {
    super( name );

    m_names = names;
    m_coordHash = null;
  }

  /**
   * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected IStatus run( IProgressMonitor monitor )
  {
    try
    {
      /* If no monitor is given, create a null progress monitor. */
      if( monitor == null )
        monitor = new NullProgressMonitor();

      /* Monitor. */
      monitor.beginTask( "Initialisiere die Koordinaten-Systeme ...", 100 );
      monitor.subTask( "Initialisiere ..." );

      /* This function may take a long time, because it is calling internally another long running function. */
      HashMap<String, CoordinateSystem> coordHash = CRSHelper.getCoordHash( m_names );

      /* Monitor. */
      monitor.worked( 50 );
      monitor.subTask( "Initialisierung abgeschlossen ..." );

      /* Set the result. */
      m_coordHash = coordHash;

      /* Monitor. */
      monitor.worked( 50 );

      return Status.OK_STATUS;
    }
    catch( Exception ex )
    {
      return StatusUtilities.statusFromThrowable( ex );
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }

  /**
   * This function returns the names used, to initialize the coordinate systems.
   * 
   * @return The names used, to initialize the coordinate systems.
   */
  public List<String> getNames( )
  {
    return m_names;
  }

  /**
   * This function returns the hash of the coordinate systems.
   * 
   * @return The hash of the coordinate systems.
   */
  public HashMap<String, CoordinateSystem> getCoordHash( )
  {
    return m_coordHash;
  }
}