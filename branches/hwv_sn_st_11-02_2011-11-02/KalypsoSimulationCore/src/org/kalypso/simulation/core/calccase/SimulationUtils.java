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
package org.kalypso.simulation.core.calccase;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.ClearAfterCalc;

/**
 * @author Gernot Belger
 *
 */
public final class SimulationUtils
{
  private SimulationUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  public static void clearResultsAfterCalculation( final Modeldata modelspec, final IContainer calcCaseFolder, final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final IProject project = calcCaseFolder.getProject();

      final List<ClearAfterCalc> clearList = modelspec.getClearAfterCalc();
      monitor.beginTask( "Alte Ergebnisse werden gelöscht", clearList.size() ); //$NON-NLS-1$

      for( final ClearAfterCalc clearAfterCalc : clearList )
      {
        final Modeldata.ClearAfterCalc clearType = clearAfterCalc;

        final boolean relToCalc = clearType.isRelativeToCalcCase();
        final String path = clearType.getPath();
        final IResource resource = relToCalc ? calcCaseFolder.findMember( path ) : project.findMember( path );
        if( resource != null )
          resource.delete( false, new SubProgressMonitor( monitor, 1 ) );
      }
    }
    finally
    {
      monitor.done();
    }
  }
}
